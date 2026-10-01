package dev.levilainpetit.wux.system

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.wifi.WifiManager
import android.os.Build
import android.os.PowerManager
import android.os.SystemClock
import android.telephony.TelephonyManager
import dev.levilainpetit.wux.widgets.SystemStatus
import es.antonborri.home_widget.HomeWidgetPlugin
import java.net.HttpURLConnection
import java.net.Inet4Address
import java.net.InetSocketAddress
import java.net.Socket
import java.net.URL

/** Le réseau en cours, tel que le widget Réseau l'affiche. */
data class Link(
    val kind: Kind,
    /** Nom du Wi-Fi (s'il est permis de le lire) ou de l'opérateur. */
    val name: String?,
    /** Bande du Wi-Fi (« 5 GHz »). */
    val band: String?,
    /** Puissance reçue (dBm) et niveau 0 à 4. */
    val rssi: Int?,
    val level: Int?,
    /** Adresse IPv4 locale. */
    val address: String?,
) {
    enum class Kind { WIFI, MOBILE, OTHER, NONE }
}

/** Le dernier test de débit (Mb/s), et quand. */
data class SpeedTest(val down: Double?, val up: Double?, val at: Long)

/**
 * Réseau : le lien en cours (lu à chaque dessin, sans réseau), le ping
 * (mesuré en fond, une connexion TCP) et le test de débit (lancé à la main,
 * ~12 s et quelques dizaines de Mo, par les serveurs de test de Cloudflare).
 */
object NetworkStatus {

    private const val PING = "net.ping"
    private const val PING_AT = "net.ping_at"
    private const val DOWN = "net.down"
    private const val UP = "net.up"
    private const val TESTED = "net.tested_at"
    private const val TESTING = "net.testing_since"

    /** Le nom du Wi-Fi demande la position précise (Android 10+). */
    fun canReadName(context: Context) =
        context.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED

    fun link(context: Context): Link {
        val connectivity = context.getSystemService(ConnectivityManager::class.java)
        val network = connectivity?.activeNetwork
        val capabilities = network?.let { connectivity.getNetworkCapabilities(it) }
            ?: return Link(Link.Kind.NONE, null, null, null, null, null)
        val address = runCatching {
            connectivity.getLinkProperties(network)?.linkAddresses?.map { it.address }?.firstOrNull { it is Inet4Address }?.hostAddress
        }.getOrNull()
        val level = SystemStatus.signalLevel(context)
        return when {
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> {
                val wifi = context.applicationContext.getSystemService(WifiManager::class.java)
                @Suppress("DEPRECATION")
                val info = wifi?.connectionInfo
                val name = info?.ssid?.removeSurrounding("\"")?.takeIf { it.isNotBlank() && it != WifiManager.UNKNOWN_SSID && it != "<unknown ssid>" }
                val band = info?.frequency?.let {
                    when {
                        it >= 5925 -> "6 GHz"
                        it >= 4900 -> "5 GHz"
                        it > 0 -> "2,4 GHz"
                        else -> null
                    }
                }
                val rssi = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    capabilities.signalStrength.takeIf { it != NetworkCapabilities.SIGNAL_STRENGTH_UNSPECIFIED }
                } else {
                    info?.rssi
                }
                Link(Link.Kind.WIFI, name, band, rssi, level, address)
            }
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> {
                val operator = context.getSystemService(TelephonyManager::class.java)?.networkOperatorName?.takeIf { it.isNotBlank() }
                Link(Link.Kind.MOBILE, operator, null, null, level, address)
            }
            else -> Link(Link.Kind.OTHER, null, null, null, null, address)
        }
    }

    /** Le dernier ping (ms), s'il date de moins d'une heure. */
    fun ping(context: Context): Int? {
        val prefs = HomeWidgetPlugin.getData(context)
        if (System.currentTimeMillis() - prefs.getLong(PING_AT, 0) > 3_600_000) return null
        return prefs.getInt(PING, -1).takeIf { it >= 0 }
    }

    fun lastTest(context: Context): SpeedTest? {
        val prefs = HomeWidgetPlugin.getData(context)
        val at = prefs.getLong(TESTED, 0).takeIf { it > 0 } ?: return null
        fun value(key: String) = prefs.getFloat(key, -1f).takeIf { it >= 0f }?.toDouble()
        return SpeedTest(value(DOWN), value(UP), at)
    }

    /** Un test est en cours (depuis moins d'une minute). */
    fun testing(context: Context) = System.currentTimeMillis() - HomeWidgetPlugin.getData(context).getLong(TESTING, 0) < 60_000

    /** Mesure et garde le ping : le meilleur de trois connexions TCP. Réseau : hors du fil principal. */
    /**
     * Le ping, seulement s'il sert : écran allumé (écran éteint, personne ne
     * le lit, et chaque mesure réveille la radio) et pas déjà mesuré dans les
     * dix dernières minutes (le worker et la mise à jour du widget tombent
     * souvent ensemble).
     */
    fun measurePingIfUseful(context: Context) {
        if (context.getSystemService(PowerManager::class.java)?.isInteractive == false) return
        if (System.currentTimeMillis() - HomeWidgetPlugin.getData(context).getLong(PING_AT, 0) < 10 * 60_000L) return
        measurePing(context)
    }

    fun measurePing(context: Context): Int? {
        val best = (0 until 3).mapNotNull {
            runCatching {
                Socket().use { socket ->
                    val start = SystemClock.elapsedRealtime()
                    socket.connect(InetSocketAddress("1.1.1.1", 443), 2_000)
                    (SystemClock.elapsedRealtime() - start).toInt()
                }
            }.getOrNull()
        }.minOrNull()
        val editor = HomeWidgetPlugin.getData(context).edit().putLong(PING_AT, System.currentTimeMillis())
        editor.putInt(PING, best ?: -1).apply()
        return best
    }

    /** Le test de débit : environ 7 s de téléchargement, puis 5 s d'envoi. */
    fun runSpeedTest(context: Context) {
        val prefs = HomeWidgetPlugin.getData(context)
        prefs.edit().putLong(TESTING, System.currentTimeMillis()).apply()
        try {
            measurePing(context)
            val down = download()
            val up = upload()
            prefs.edit()
                .putFloat(DOWN, down?.toFloat() ?: -1f)
                .putFloat(UP, up?.toFloat() ?: -1f)
                .putLong(TESTED, System.currentTimeMillis())
                .apply()
        } finally {
            prefs.edit().remove(TESTING).apply()
        }
    }

    private fun download(): Double? = runCatching {
        val connection = URL("https://speed.cloudflare.com/__down?bytes=100000000").openConnection() as HttpURLConnection
        connection.connectTimeout = 3_000
        connection.readTimeout = 3_000
        try {
            val input = connection.inputStream
            val buffer = ByteArray(64 * 1024)
            val start = SystemClock.elapsedRealtime()
            var bytes = 0L
            while (SystemClock.elapsedRealtime() - start < 7_000) {
                val read = input.read(buffer)
                if (read < 0) break
                bytes += read
            }
            megabits(bytes, SystemClock.elapsedRealtime() - start)
        } finally {
            connection.disconnect()
        }
    }.getOrNull()

    private fun upload(): Double? = runCatching {
        val connection = URL("https://speed.cloudflare.com/__up").openConnection() as HttpURLConnection
        connection.connectTimeout = 3_000
        connection.readTimeout = 3_000
        connection.requestMethod = "POST"
        connection.doOutput = true
        connection.setChunkedStreamingMode(64 * 1024)
        try {
            val buffer = ByteArray(64 * 1024)
            val start = SystemClock.elapsedRealtime()
            var bytes = 0L
            connection.outputStream.use { out ->
                while (SystemClock.elapsedRealtime() - start < 5_000) {
                    out.write(buffer)
                    bytes += buffer.size
                }
            }
            val elapsed = SystemClock.elapsedRealtime() - start
            runCatching { connection.responseCode }
            megabits(bytes, elapsed)
        } finally {
            connection.disconnect()
        }
    }.getOrNull()

    private fun megabits(bytes: Long, millis: Long) = if (millis <= 0 || bytes <= 0) null else bytes * 8.0 / (millis / 1000.0) / 1_000_000.0
}
