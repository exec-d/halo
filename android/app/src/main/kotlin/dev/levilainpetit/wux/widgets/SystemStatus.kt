package dev.levilainpetit.wux.widgets

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.BatteryManager
import android.os.Build
import android.os.Environment
import android.os.StatFs
import android.provider.Settings
import android.text.format.Formatter
import dev.levilainpetit.wux.R

/** Une case du widget système : titre, valeur, précision, jauge (0-100) éventuelle. */
data class SystemTile(
    val label: String,
    val value: String,
    val detail: String,
    val progress: Int?,
)

/**
 * État du téléphone affiché par le widget système. Rien ici ne demande
 * d'autorisation : ni le nom du Wi-Fi (localisation), ni l'opérateur.
 */
object SystemStatus {

    fun battery(context: Context): SystemTile {
        val battery = context.getSystemService(BatteryManager::class.java)
        val level = battery?.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)?.coerceIn(0, 100) ?: 0
        val charging = battery?.isCharging == true
        return SystemTile(
            label = context.getString(R.string.system_battery),
            value = "$level %",
            detail = context.getString(if (charging) R.string.system_charging else R.string.system_on_battery),
            progress = level,
        )
    }

    fun network(context: Context): SystemTile {
        val label = context.getString(R.string.system_network)
        val connectivity = context.getSystemService(ConnectivityManager::class.java)
        val capabilities = connectivity?.getNetworkCapabilities(connectivity.activeNetwork)
        if (capabilities == null) {
            val airplane = Settings.Global.getInt(context.contentResolver, Settings.Global.AIRPLANE_MODE_ON, 0) == 1
            return SystemTile(
                label,
                context.getString(if (airplane) R.string.system_airplane else R.string.system_offline),
                context.getString(R.string.system_no_network),
                null,
            )
        }
        val vpn = if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_VPN)) " · VPN" else ""
        return when {
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> {
                val level = wifiLevel(capabilities)
                SystemTile(
                    label,
                    context.getString(R.string.system_wifi),
                    (level?.let { context.getString(R.string.system_signal, it) } ?: context.getString(R.string.system_connected)) + vpn,
                    level?.times(25),
                )
            }
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> SystemTile(
                label,
                context.getString(R.string.system_mobile),
                context.getString(R.string.system_mobile_data) + vpn,
                null,
            )
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> SystemTile(
                label,
                context.getString(R.string.system_ethernet),
                context.getString(R.string.system_connected) + vpn,
                null,
            )
            else -> SystemTile(label, context.getString(R.string.system_connected), vpn.removePrefix(" · "), null)
        }
    }

    fun storage(context: Context): SystemTile {
        val stats = StatFs(Environment.getDataDirectory().path)
        val free = stats.availableBytes
        val total = stats.totalBytes
        val used = if (total > 0) ((total - free) * 100 / total).toInt() else 0
        return SystemTile(
            label = context.getString(R.string.system_storage),
            value = Formatter.formatShortFileSize(context, free),
            detail = context.getString(R.string.system_storage_free, Formatter.formatShortFileSize(context, total)),
            progress = used,
        )
    }

    /** Niveau 0 à 4 d'après la puissance reçue, ou `null` si inconnue. */
    private fun wifiLevel(capabilities: NetworkCapabilities): Int? {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) return null
        val rssi = capabilities.signalStrength
        if (rssi == NetworkCapabilities.SIGNAL_STRENGTH_UNSPECIFIED) return null
        return when {
            rssi >= -55 -> 4
            rssi >= -66 -> 3
            rssi >= -77 -> 2
            rssi >= -88 -> 1
            else -> 0
        }
    }
}
