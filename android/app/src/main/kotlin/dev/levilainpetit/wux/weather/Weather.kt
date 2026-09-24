package dev.levilainpetit.wux.weather

import android.content.Context
import dev.levilainpetit.wux.R
import es.antonborri.home_widget.HomeWidgetPlugin
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

/** Un lieu choisi dans l'application. */
data class Place(val name: String, val latitude: Double, val longitude: Double)

/** Une heure de prévision. */
data class Hour(val time: LocalDateTime, val temperature: Double, val code: Int, val isDay: Boolean)

/** Un jour de prévision. */
data class Day(val date: LocalDate, val code: Int, val max: Double, val min: Double, val sunrise: LocalTime?, val sunset: LocalTime?)

/** Un point de la courbe des 24 heures. */
data class CurvePoint(val time: LocalDateTime, val temperature: Double, val rainProbability: Int, val isDay: Boolean)

/** Une heure de pluie : probabilité (%) et cumul (mm). */
data class RainHour(val time: LocalDateTime, val probability: Int, val millimeters: Double)

/** Pollens (grains/m³) et indice européen de qualité de l'air, par Open-Meteo. */
data class Air(val aqi: Int?, val pollens: Map<Pollen, Double>)

/** Les pollens suivis, avec leurs seuils (grains/m³) faible, moyen, élevé, très élevé. */
enum class Pollen(val key: String, val label: Int, val thresholds: DoubleArray) {
    GRASS("grass_pollen", R.string.pollen_grass, doubleArrayOf(1.0, 5.0, 25.0, 100.0)),
    BIRCH("birch_pollen", R.string.pollen_birch, doubleArrayOf(1.0, 10.0, 50.0, 500.0)),
    ALDER("alder_pollen", R.string.pollen_alder, doubleArrayOf(1.0, 10.0, 50.0, 500.0)),
    OLIVE("olive_pollen", R.string.pollen_olive, doubleArrayOf(1.0, 10.0, 50.0, 200.0)),
    MUGWORT("mugwort_pollen", R.string.pollen_mugwort, doubleArrayOf(1.0, 10.0, 30.0, 100.0)),
    RAGWEED("ragweed_pollen", R.string.pollen_ragweed, doubleArrayOf(1.0, 5.0, 20.0, 50.0)),
    ;

    /** Niveau 0 (aucun) à 4 (très élevé). */
    fun level(value: Double) = thresholds.count { value >= it }
}

/** Les prévisions telles que le widget les affiche. */
data class Forecast(
    val fetchedAt: Long,
    val time: LocalDateTime,
    val temperature: Double,
    val apparent: Double,
    val code: Int,
    val isDay: Boolean,
    val wind: Double,
    /** D'où vient le vent, en degrés (0 : nord). */
    val windDirection: Int = 0,
    val humidity: Int = 0,
    val uv: Double? = null,
    /** Les 24 prochaines heures, pour la courbe. */
    val curve: List<CurvePoint> = emptyList(),
    val hours: List<Hour>,
    val days: List<Day>,
    /** Les 12 prochaines heures. */
    val rain: List<RainHour> = emptyList(),
    /** Les 8 prochains quarts d'heure (mm), quand la zone en a. */
    val rainSoon: List<Double> = emptyList(),
)

/**
 * Météo par Open-Meteo (gratuit, sans clé ni compte). Le lieu et la dernière
 * réponse sont gardés dans les réglages partagés avec Flutter : `weather.place`
 * (nom|lat|lon) et `weather.cache` (JSON brut).
 */
object Weather {

    private const val PLACE = "weather.place"
    private const val CACHE = "weather.cache"
    private const val FETCHED = "weather.fetched_at"
    private const val AIR = "weather.air"

    fun place(context: Context): Place? {
        val parts = HomeWidgetPlugin.getData(context).getString(PLACE, null)?.split('|') ?: return null
        if (parts.size != 3) return null
        return Place(parts[0], parts[1].toDoubleOrNull() ?: return null, parts[2].toDoubleOrNull() ?: return null)
    }

    fun setPlace(context: Context, place: Place) {
        HomeWidgetPlugin.getData(context).edit()
            .putString(PLACE, "${place.name.replace('|', ' ')}|${place.latitude}|${place.longitude}")
            .remove(CACHE)
            .apply()
    }

    /** Télécharge et garde les prévisions du lieu choisi. Réseau : hors du fil principal. */
    fun refresh(context: Context): Boolean {
        val place = place(context) ?: return false
        val url = "https://api.open-meteo.com/v1/forecast?latitude=${place.latitude}&longitude=${place.longitude}" +
            "&current=temperature_2m,apparent_temperature,weather_code,is_day,wind_speed_10m," +
            "wind_direction_10m,relative_humidity_2m,uv_index" +
            "&hourly=temperature_2m,weather_code,is_day,precipitation_probability,precipitation" +
            "&minutely_15=precipitation&forecast_minutely_15=12" +
            "&daily=weather_code,temperature_2m_max,temperature_2m_min,sunrise,sunset" +
            "&timezone=auto&forecast_days=4"
        val body = get(url) ?: return false
        // Pollens et qualité de l'air : service distinct, facultatif (hors
        // Europe, pas de pollens).
        val air = get(
            "https://air-quality-api.open-meteo.com/v1/air-quality?latitude=${place.latitude}" +
                "&longitude=${place.longitude}&timezone=auto" +
                "&current=european_aqi," + Pollen.entries.joinToString(",") { it.key },
        )
        val editor = HomeWidgetPlugin.getData(context).edit()
            .putString(CACHE, body)
            .putLong(FETCHED, System.currentTimeMillis())
        if (air != null) editor.putString(AIR, air)
        editor.apply()
        return true
    }

    fun air(context: Context): Air? {
        val json = HomeWidgetPlugin.getData(context).getString(AIR, null) ?: return null
        return runCatching {
            val current = JSONObject(json).getJSONObject("current")
            Air(
                aqi = if (current.isNull("european_aqi")) null else current.getDouble("european_aqi").toInt(),
                pollens = Pollen.entries.filter { !current.isNull(it.key) }.associateWith { current.getDouble(it.key) },
            )
        }.getOrNull()
    }

    fun forecast(context: Context): Forecast? {
        val prefs = HomeWidgetPlugin.getData(context)
        val json = prefs.getString(CACHE, null) ?: return null
        return runCatching { parse(JSONObject(json), prefs.getLong(FETCHED, 0)) }.getOrNull()
    }

    /** Recherche de villes par leur nom (géocodage Open-Meteo). */
    fun search(query: String): List<Place> {
        val url = "https://geocoding-api.open-meteo.com/v1/search?count=8&language=fr&name=" +
            URLEncoder.encode(query, "UTF-8")
        val results = get(url)?.let { JSONObject(it).optJSONArray("results") } ?: return emptyList()
        return List(results.length()) { i ->
            val r = results.getJSONObject(i)
            val detail = listOf(r.optString("admin1"), r.optString("country")).filter { it.isNotBlank() }
            Place(
                name = (listOf(r.getString("name")) + detail).joinToString(", "),
                latitude = r.getDouble("latitude"),
                longitude = r.getDouble("longitude"),
            )
        }
    }

    private fun parse(root: JSONObject, fetchedAt: Long): Forecast {
        val current = root.getJSONObject("current")
        val now = LocalDateTime.parse(current.getString("time"))
        val hourly = root.getJSONObject("hourly")
        val times = hourly.getJSONArray("time")
        val hours = (0 until times.length()).map { i ->
            Hour(
                LocalDateTime.parse(times.getString(i)),
                hourly.getJSONArray("temperature_2m").getDouble(i),
                hourly.getJSONArray("weather_code").getInt(i),
                hourly.getJSONArray("is_day").getInt(i) == 1,
            )
        }.filter { it.time.isAfter(now) }.take(6)
        val probabilities = hourly.optJSONArray("precipitation_probability")
        val amounts = hourly.optJSONArray("precipitation")
        val rain = (0 until times.length()).mapNotNull { i ->
            val time = LocalDateTime.parse(times.getString(i))
            // L'heure en cours compte : elle commence avant « maintenant ».
            if (time.isBefore(now.minusHours(1)) || probabilities == null || amounts == null) return@mapNotNull null
            RainHour(time, probabilities.optInt(i, 0), amounts.optDouble(i, 0.0))
        }.filter { !it.time.plusHours(1).isBefore(now) }.take(12)
        val curve = (0 until times.length()).mapNotNull { i ->
            val time = LocalDateTime.parse(times.getString(i))
            if (time.plusHours(1).isBefore(now)) return@mapNotNull null
            CurvePoint(
                time,
                hourly.getJSONArray("temperature_2m").getDouble(i),
                probabilities?.optInt(i, 0) ?: 0,
                hourly.getJSONArray("is_day").getInt(i) == 1,
            )
        }.take(24)
        val quarters = root.optJSONObject("minutely_15")
        val rainSoon = quarters?.let { q ->
            val qTimes = q.getJSONArray("time")
            val qRain = q.getJSONArray("precipitation")
            (0 until qTimes.length())
                .filter { !LocalDateTime.parse(qTimes.getString(it)).plusMinutes(15).isBefore(now) }
                .map { qRain.optDouble(it, 0.0) }
                .take(8)
        }.orEmpty()
        val daily = root.getJSONObject("daily")
        val dates = daily.getJSONArray("time")
        val days = (0 until dates.length()).map { i ->
            Day(
                LocalDate.parse(dates.getString(i)),
                daily.getJSONArray("weather_code").getInt(i),
                daily.getJSONArray("temperature_2m_max").getDouble(i),
                daily.getJSONArray("temperature_2m_min").getDouble(i),
                runCatching { LocalDateTime.parse(daily.getJSONArray("sunrise").getString(i)).toLocalTime() }.getOrNull(),
                runCatching { LocalDateTime.parse(daily.getJSONArray("sunset").getString(i)).toLocalTime() }.getOrNull(),
            )
        }
        return Forecast(
            fetchedAt = fetchedAt,
            time = now,
            temperature = current.getDouble("temperature_2m"),
            apparent = current.getDouble("apparent_temperature"),
            code = current.getInt("weather_code"),
            isDay = current.getInt("is_day") == 1,
            wind = current.getDouble("wind_speed_10m"),
            windDirection = current.optInt("wind_direction_10m", 0),
            humidity = current.optInt("relative_humidity_2m", 0),
            uv = if (current.isNull("uv_index")) null else current.optDouble("uv_index"),
            curve = curve,
            hours = hours,
            days = days,
            rain = rain,
            rainSoon = rainSoon,
        )
    }

    private fun get(url: String): String? = runCatching {
        val connection = URL(url).openConnection() as HttpURLConnection
        connection.connectTimeout = 10_000
        connection.readTimeout = 10_000
        try {
            if (connection.responseCode != 200) null else connection.inputStream.bufferedReader().use { it.readText() }
        } finally {
            connection.disconnect()
        }
    }.getOrNull()

    /** Une phrase : pluie en cours, dans combien de temps, ou pas de pluie. */
    fun rainSummary(context: Context, forecast: Forecast): String {
        val wetQuarter = forecast.rainSoon.indexOfFirst { it >= 0.1 }
        if (wetQuarter == 0) return context.getString(R.string.rain_now)
        if (wetQuarter > 0) return context.getString(R.string.rain_in_minutes, wetQuarter * 15)
        val likely = forecast.rain.firstOrNull { it.probability >= 50 }
            ?: return context.getString(R.string.rain_none, forecast.rain.size.coerceAtLeast(1))
        val pattern = if (android.text.format.DateFormat.is24HourFormat(context)) "HH'h'" else "h a"
        val hour = java.time.format.DateTimeFormatter.ofPattern(pattern, java.util.Locale.getDefault()).format(likely.time)
        return context.getString(R.string.rain_at, hour, likely.probability)
    }

    /** Libellé du code météo WMO. */
    fun label(code: Int): Int = when (code) {
        0 -> R.string.weather_clear
        1, 2 -> R.string.weather_partly
        3 -> R.string.weather_cloudy
        45, 48 -> R.string.weather_fog
        51, 53, 55, 56, 57 -> R.string.weather_drizzle
        61, 63, 65, 66, 67 -> R.string.weather_rain
        71, 73, 75, 77 -> R.string.weather_snow
        80, 81, 82 -> R.string.weather_showers
        85, 86 -> R.string.weather_snow_showers
        95, 96, 99 -> R.string.weather_storm
        else -> R.string.weather_unknown
    }

    /** Icône lumineuse du code météo WMO. */
    fun icon(code: Int, isDay: Boolean): Int = when (code) {
        0 -> if (isDay) R.drawable.icon_sunny else R.drawable.icon_night
        1, 2 -> if (isDay) R.drawable.icon_partly_cloudy else R.drawable.icon_night
        3 -> R.drawable.icon_cloud
        45, 48 -> R.drawable.icon_fog
        51, 53, 55, 56, 57 -> R.drawable.icon_drizzle
        61, 63, 65, 66, 67 -> R.drawable.icon_rain
        71, 73, 75, 77, 85, 86 -> R.drawable.icon_snow
        80, 81, 82 -> R.drawable.icon_showers
        95, 96, 99 -> R.drawable.icon_storm
        else -> R.drawable.icon_cloud
    }
}
