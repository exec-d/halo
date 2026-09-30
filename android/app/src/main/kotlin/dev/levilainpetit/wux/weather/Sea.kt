package dev.levilainpetit.wux.weather

import android.content.Context
import es.antonborri.home_widget.HomeWidgetPlugin
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.time.LocalDateTime
import kotlin.math.PI
import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt

/** L'état de la mer, tel que le widget Mer et vagues l'affiche. */
data class SeaState(
    /** Hauteur des vagues (m), leur période (s), et d'où elles viennent (°). */
    val waveHeight: Double,
    val wavePeriod: Double?,
    val waveDirection: Int?,
    /** Température de l'eau (°C). */
    val water: Double?,
    /** Distance (km) entre le lieu de Météo et le point de mer suivi. */
    val distanceKm: Int,
    /** Hauteur des vagues heure par heure, sur 24 heures. */
    val hours: List<Pair<LocalDateTime, Double>>,
)

/**
 * La mer au plus près du lieu de Météo, par l'API marine d'Open-Meteo
 * (gratuite, sans clé). Le lieu est souvent à terre : on cherche une fois le
 * point de mer le plus proche (jusqu'à ~55 km), puis on ne télécharge que lui.
 */
object Sea {

    private const val POINT = "sea.point"
    private const val CACHE = "sea.cache"
    private const val REQUESTED = "sea.requested_at"

    /** Distances essayées, en degrés, et directions (8 points cardinaux). */
    private val RINGS = doubleArrayOf(0.0, 0.1, 0.2, 0.35, 0.5)

    fun state(context: Context): SeaState? {
        val prefs = HomeWidgetPlugin.getData(context)
        val json = prefs.getString(CACHE, null) ?: return null
        val point = point(context) ?: return null
        return runCatching { parse(JSONObject(json), point.third) }.getOrNull()
    }

    /**
     * Vrai s'il faut lancer la tâche de fond qui cherche la mer : rien de
     * gardé pour ce lieu, et pas déjà demandé ces dix dernières minutes (le
     * widget se redessine à la fin de la tâche, même si elle échoue).
     */
    fun wanted(context: Context): Boolean {
        if (Weather.place(context) == null || state(context) != null || noSea(context)) return false
        val prefs = HomeWidgetPlugin.getData(context)
        val now = System.currentTimeMillis()
        if (now - prefs.getLong(REQUESTED, 0) < 10 * 60_000) return false
        prefs.edit().putLong(REQUESTED, now).apply()
        return true
    }

    /** Vrai si le lieu de Météo n'a pas de mer à portée (déjà cherché). */
    fun noSea(context: Context): Boolean =
        HomeWidgetPlugin.getData(context).getString(POINT, null)?.let { it.startsWith("none|") && it.endsWith(key(context)) } == true

    /**
     * Télécharge l'état de la mer. [search] : chercher le point de mer s'il
     * n'est pas encore connu (plusieurs requêtes, à faire dans une tâche de
     * fond, pas dans un récepteur).
     */
    fun refresh(context: Context, search: Boolean) {
        val place = Weather.place(context) ?: return
        val prefs = HomeWidgetPlugin.getData(context)
        var point = point(context)
        if (point == null) {
            if (!search || noSea(context)) return
            point = find(place)
            prefs.edit().putString(POINT, point?.let { "${it.first}|${it.second}|${it.third}|${key(context)}" } ?: "none|${key(context)}").apply()
            if (point == null) return
        }
        val body = get(url(point.first, point.second, full = true)) ?: return
        prefs.edit().putString(CACHE, body).apply()
    }

    /** Le point suivi (lat, lon, distance en km), s'il a été trouvé pour le lieu actuel. */
    private fun point(context: Context): Triple<Double, Double, Int>? {
        val parts = HomeWidgetPlugin.getData(context).getString(POINT, null)?.split('|') ?: return null
        if (parts.size != 4 || parts[0] == "none" || parts[3] != key(context)) return null
        return Triple(parts[0].toDoubleOrNull() ?: return null, parts[1].toDoubleOrNull() ?: return null, parts[2].toIntOrNull() ?: 0)
    }

    private fun key(context: Context) = Weather.place(context)?.let { "${it.latitude},${it.longitude}" }.orEmpty()

    private fun find(place: Place): Triple<Double, Double, Int>? {
        for (ring in RINGS) {
            val directions = if (ring == 0.0) listOf(0.0) else List(8) { it * 45.0 }
            for (angle in directions) {
                val a = angle * PI / 180
                val lat = place.latitude + ring * cos(a)
                val lon = place.longitude + ring * sin(a) / cos(place.latitude * PI / 180)
                val body = get(url(lat, lon, full = false)) ?: continue
                val current = runCatching { JSONObject(body).getJSONObject("current") }.getOrNull() ?: continue
                if (!current.isNull("wave_height")) return Triple(lat, lon, distance(place.latitude, place.longitude, lat, lon))
            }
        }
        return null
    }

    private fun url(lat: Double, lon: Double, full: Boolean) =
        "https://marine-api.open-meteo.com/v1/marine?latitude=$lat&longitude=$lon&timezone=auto" +
            if (full) "&current=wave_height,wave_direction,wave_period,sea_surface_temperature&hourly=wave_height&forecast_days=2" else "&current=wave_height"

    private fun parse(root: JSONObject, distanceKm: Int): SeaState? {
        val current = root.getJSONObject("current")
        if (current.isNull("wave_height")) return null
        val hourly = root.getJSONObject("hourly")
        val times = hourly.getJSONArray("time")
        val heights = hourly.getJSONArray("wave_height")
        val now = LocalDateTime.now().withMinute(0).withSecond(0).withNano(0)
        val hours = (0 until times.length()).mapNotNull { i ->
            val time = LocalDateTime.parse(times.getString(i))
            if (time.isBefore(now) || heights.isNull(i)) null else time to heights.getDouble(i)
        }.take(25)
        fun optional(key: String) = if (current.isNull(key)) null else current.getDouble(key)
        return SeaState(
            waveHeight = current.getDouble("wave_height"),
            wavePeriod = optional("wave_period"),
            waveDirection = optional("wave_direction")?.roundToInt(),
            water = optional("sea_surface_temperature"),
            distanceKm = distanceKm,
            hours = hours,
        )
    }

    private fun distance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Int {
        val r = PI / 180
        val h = sin((lat2 - lat1) * r / 2).let { it * it } + cos(lat1 * r) * cos(lat2 * r) * sin((lon2 - lon1) * r / 2).let { it * it }
        return (2 * 6371 * asin(sqrt(h))).roundToInt()
    }

    private fun get(url: String): String? = runCatching {
        val connection = URL(url).openConnection() as HttpURLConnection
        connection.connectTimeout = 4_000
        connection.readTimeout = 4_000
        try {
            if (connection.responseCode != 200) null else connection.inputStream.bufferedReader().use { it.readText() }
        } finally {
            connection.disconnect()
        }
    }.getOrNull()
}
