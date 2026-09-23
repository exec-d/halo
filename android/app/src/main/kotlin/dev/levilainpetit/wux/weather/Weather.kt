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

/** Les prévisions telles que le widget les affiche. */
data class Forecast(
    val fetchedAt: Long,
    val time: LocalDateTime,
    val temperature: Double,
    val apparent: Double,
    val code: Int,
    val isDay: Boolean,
    val wind: Double,
    val hours: List<Hour>,
    val days: List<Day>,
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
            "&current=temperature_2m,apparent_temperature,weather_code,is_day,wind_speed_10m" +
            "&hourly=temperature_2m,weather_code,is_day" +
            "&daily=weather_code,temperature_2m_max,temperature_2m_min,sunrise,sunset" +
            "&timezone=auto&forecast_days=4"
        val body = get(url) ?: return false
        HomeWidgetPlugin.getData(context).edit()
            .putString(CACHE, body)
            .putLong(FETCHED, System.currentTimeMillis())
            .apply()
        return true
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
            hours = hours,
            days = days,
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
