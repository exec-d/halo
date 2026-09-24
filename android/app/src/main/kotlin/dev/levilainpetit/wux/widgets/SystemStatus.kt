package dev.levilainpetit.wux.widgets

import android.app.ActivityManager
import android.app.NotificationManager
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.location.LocationManager
import android.media.AudioManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.wifi.WifiInfo
import android.net.wifi.WifiManager
import android.os.BatteryManager
import android.os.Build
import android.os.Environment
import android.os.StatFs
import android.provider.Settings
import android.telephony.TelephonyManager
import android.text.format.Formatter
import dev.levilainpetit.wux.R

/**
 * Une case des widgets système : titre, valeur, précision, jauge (0-100)
 * éventuelle, et le réglage qu'elle ouvre.
 */
data class SystemTile(
    val label: String,
    val value: String,
    val detail: String,
    val progress: Int?,
    val action: Intent,
)

/**
 * État du téléphone affiché par les widgets système. Rien ici ne demande
 * d'autorisation à l'utilisateur : ni le nom du Wi-Fi (localisation), ni les
 * appareils Bluetooth connectés, ni l'état détaillé du téléphone.
 */
object SystemStatus {

    /** Les cases du widget système simple. */
    fun basic(context: Context) = listOf(battery(context), network(context), storage(context))

    /** Les deux rangées du widget système avancé. */
    fun advanced(context: Context) = listOf(
        listOf(mobile(context), wifi(context), bluetooth(context), battery(context, withTemperature = true)),
        listOf(memory(context), storage(context), location(context), sound(context)),
    )

    fun battery(context: Context, withTemperature: Boolean = false): SystemTile {
        val battery = context.getSystemService(BatteryManager::class.java)
        val level = battery?.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)?.coerceIn(0, 100) ?: 0
        val charging = battery?.isCharging == true
        var detail = context.getString(if (charging) R.string.system_charging else R.string.system_on_battery)
        if (withTemperature) {
            // Diffusion « collante » : lue sans s'y abonner.
            val sticky = context.applicationContext.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
            val tenths = sticky?.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, Int.MIN_VALUE) ?: Int.MIN_VALUE
            if (tenths != Int.MIN_VALUE) detail += " · ${tenths / 10} °C"
        }
        return SystemTile(
            context.getString(R.string.system_battery),
            "$level %",
            detail,
            level,
            Intent(Intent.ACTION_POWER_USAGE_SUMMARY),
        )
    }

    fun network(context: Context): SystemTile {
        val label = context.getString(R.string.system_network)
        val action = internetPanel()
        val capabilities = capabilities(context)
        if (capabilities == null) {
            val value = if (airplane(context)) R.string.system_airplane else R.string.system_offline
            return SystemTile(label, context.getString(value), context.getString(R.string.system_no_network), null, action)
        }
        val vpn = if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_VPN)) " · VPN" else ""
        return when {
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> {
                val level = wifiLevel(capabilities)
                SystemTile(
                    label,
                    context.getString(R.string.system_wifi),
                    (level?.let { context.getString(R.string.system_signal, it) }
                        ?: context.getString(R.string.system_connected)) + vpn,
                    level?.times(25),
                    action,
                )
            }
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> SystemTile(
                label,
                context.getString(R.string.system_mobile),
                context.getString(R.string.system_mobile_data) + vpn,
                null,
                action,
            )
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> SystemTile(
                label,
                context.getString(R.string.system_ethernet),
                context.getString(R.string.system_connected) + vpn,
                null,
                action,
            )
            else -> SystemTile(label, context.getString(R.string.system_connected), vpn.removePrefix(" · "), null, action)
        }
    }

    fun storage(context: Context): SystemTile {
        val stats = StatFs(Environment.getDataDirectory().path)
        val free = stats.availableBytes
        val total = stats.totalBytes
        return SystemTile(
            context.getString(R.string.system_storage),
            Formatter.formatShortFileSize(context, free),
            context.getString(R.string.system_free_of, Formatter.formatShortFileSize(context, total)),
            percentUsed(total - free, total),
            Intent(Settings.ACTION_INTERNAL_STORAGE_SETTINGS),
        )
    }

    fun mobile(context: Context): SystemTile {
        val label = context.getString(R.string.system_cellular)
        val action = internetPanel()
        if (airplane(context)) {
            return SystemTile(label, context.getString(R.string.system_airplane), "", null, action)
        }
        val telephony = context.getSystemService(TelephonyManager::class.java)
        if (telephony == null || telephony.simState != TelephonyManager.SIM_STATE_READY) {
            return SystemTile(label, context.getString(R.string.system_no_sim), "", null, action)
        }
        val operator = telephony.networkOperatorName.ifBlank { context.getString(R.string.system_mobile) }
        val level = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            runCatching { telephony.signalStrength?.level }.getOrNull()
        } else {
            null
        }
        return SystemTile(
            label,
            operator,
            level?.let { context.getString(R.string.system_signal, it) } ?: "",
            level?.times(25),
            action,
        )
    }

    fun wifi(context: Context): SystemTile {
        val label = context.getString(R.string.system_wifi_label)
        val action = Intent(Settings.ACTION_WIFI_SETTINGS)
        val wifi = context.applicationContext.getSystemService(WifiManager::class.java)
        if (wifi?.isWifiEnabled != true) {
            return SystemTile(label, context.getString(R.string.system_off), "", null, action)
        }
        val capabilities = capabilities(context)
        if (capabilities?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) != true) {
            return SystemTile(label, context.getString(R.string.system_on), context.getString(R.string.system_not_connected), null, action)
        }
        val info = wifiInfo(capabilities, wifi)
        val band = info?.frequency?.let {
            when {
                it >= 5925 -> "6 GHz"
                it >= 4900 -> "5 GHz"
                it > 0 -> "2,4 GHz"
                else -> null
            }
        }
        val speed = info?.linkSpeed?.takeIf { it > 0 }?.let { "$it Mb/s" }
        return SystemTile(
            label,
            context.getString(R.string.system_connected),
            listOfNotNull(band, speed).joinToString(" · "),
            wifiLevel(capabilities)?.times(25),
            action,
        )
    }

    fun bluetooth(context: Context): SystemTile {
        val adapter = context.getSystemService(BluetoothManager::class.java)?.adapter
        val enabled = runCatching { adapter?.isEnabled }.getOrNull()
        return SystemTile(
            context.getString(R.string.system_bluetooth),
            context.getString(
                when (enabled) {
                    true -> R.string.system_on
                    false -> R.string.system_off
                    null -> R.string.system_unavailable
                },
            ),
            "",
            null,
            Intent(Settings.ACTION_BLUETOOTH_SETTINGS),
        )
    }

    fun memory(context: Context): SystemTile {
        val info = ActivityManager.MemoryInfo()
        context.getSystemService(ActivityManager::class.java)?.getMemoryInfo(info)
        return SystemTile(
            context.getString(R.string.system_memory),
            Formatter.formatShortFileSize(context, info.availMem),
            context.getString(R.string.system_free_of, Formatter.formatShortFileSize(context, info.totalMem)),
            percentUsed(info.totalMem - info.availMem, info.totalMem),
            Intent(Settings.ACTION_DEVICE_INFO_SETTINGS),
        )
    }

    fun location(context: Context): SystemTile {
        val enabled = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            context.getSystemService(LocationManager::class.java)?.isLocationEnabled == true
        } else {
            @Suppress("DEPRECATION")
            Settings.Secure.getInt(context.contentResolver, Settings.Secure.LOCATION_MODE, 0) != 0
        }
        return SystemTile(
            context.getString(R.string.system_location),
            context.getString(if (enabled) R.string.system_on else R.string.system_off),
            "",
            null,
            Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS),
        )
    }

    fun sound(context: Context): SystemTile {
        val audio = context.getSystemService(AudioManager::class.java)
        val mode = when (audio?.ringerMode) {
            AudioManager.RINGER_MODE_SILENT -> R.string.system_silent
            AudioManager.RINGER_MODE_VIBRATE -> R.string.system_vibrate
            else -> R.string.system_ring
        }
        val dnd = context.getSystemService(NotificationManager::class.java)?.currentInterruptionFilter
            ?.let { it != NotificationManager.INTERRUPTION_FILTER_ALL && it != NotificationManager.INTERRUPTION_FILTER_UNKNOWN } == true
        val volume = audio?.let {
            val max = it.getStreamMaxVolume(AudioManager.STREAM_RING)
            if (max > 0) it.getStreamVolume(AudioManager.STREAM_RING) * 100 / max else null
        }
        val action = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            Intent(Settings.Panel.ACTION_VOLUME)
        } else {
            Intent(Settings.ACTION_SOUND_SETTINGS)
        }
        return SystemTile(
            context.getString(R.string.system_sound),
            context.getString(mode),
            if (dnd) context.getString(R.string.system_dnd) else "",
            volume,
            action,
        )
    }

    /**
     * Force du réseau utilisé, de 0 à 4 : le Wi-Fi s'il est connecté, sinon le
     * réseau mobile ; `null` si inconnue ou sans réseau.
     */
    fun signalLevel(context: Context): Int? {
        val capabilities = capabilities(context) ?: return null
        if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) return wifiLevel(capabilities)
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) return null
        val telephony = context.getSystemService(TelephonyManager::class.java) ?: return null
        return runCatching { telephony.signalStrength?.level }.getOrNull()
    }

    private fun capabilities(context: Context): NetworkCapabilities? {
        val connectivity = context.getSystemService(ConnectivityManager::class.java) ?: return null
        return connectivity.getNetworkCapabilities(connectivity.activeNetwork)
    }

    private fun airplane(context: Context) =
        Settings.Global.getInt(context.contentResolver, Settings.Global.AIRPLANE_MODE_ON, 0) == 1

    private fun internetPanel() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        Intent(Settings.Panel.ACTION_INTERNET_CONNECTIVITY)
    } else {
        Intent(Settings.ACTION_WIRELESS_SETTINGS)
    }

    private fun wifiInfo(capabilities: NetworkCapabilities, wifi: WifiManager): WifiInfo? =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            capabilities.transportInfo as? WifiInfo
        } else {
            @Suppress("DEPRECATION")
            wifi.connectionInfo
        }

    private fun percentUsed(used: Long, total: Long) = if (total > 0) (used * 100 / total).toInt() else 0

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
