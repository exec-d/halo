package dev.levilainpetit.wux.system

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Build

/** Un relevé de batterie : instant (ms), niveau (0–100), en charge ou non. */
data class BatterySample(val time: Long, val level: Int, val charging: Boolean)

/** L'état de la batterie à un instant, et ce qu'on peut en prévoir. */
data class BatteryInfo(
    val level: Int,
    val charging: Boolean,
    /** Dixièmes de degré, ou `null` si inconnue. */
    val temperatureTenths: Int?,
    val millivolts: Int?,
    /** Cycles de charge (Android 14+), ou `null`. */
    val cycles: Int?,
    /** Une valeur de [BatteryManager.BATTERY_HEALTH_GOOD] et suivantes. */
    val health: Int,
    /**
     * Minutes avant la charge complète (en charge) ou avant d'être vide (sur
     * batterie), ou `null` si on ne peut pas encore l'estimer.
     */
    val minutesLeft: Int?,
    /** Les 24 dernières heures, du plus ancien au plus récent. */
    val history: List<BatterySample>,
)

/**
 * Relevés de batterie gardés sur 24 heures, pour la courbe du widget
 * Batterie. Un relevé est ajouté à chaque mise à jour des widgets système
 * (toutes les 15 minutes environ) et, quand le fond Circuit est affiché, à
 * chaque changement de niveau.
 */
object BatteryHistory {
    private const val PREFS = "halo_battery"
    private const val SAMPLES = "samples"
    private const val KEEP_MILLIS = 26 * 3_600_000L
    private const val MIN_GAP_MILLIS = 4 * 60_000L

    fun read(context: Context): BatteryInfo {
        val sticky = context.applicationContext.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        val manager = context.getSystemService(BatteryManager::class.java)
        val level = manager?.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)?.takeIf { it in 0..100 }
            ?: sticky?.let {
                val raw = it.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
                val scale = it.getIntExtra(BatteryManager.EXTRA_SCALE, 100)
                if (raw >= 0 && scale > 0) raw * 100 / scale else null
            } ?: 0
        val charging = (sticky?.getIntExtra(BatteryManager.EXTRA_PLUGGED, 0) ?: 0) != 0
        record(context, level, charging)
        val history = samples(context)

        val minutes = if (charging) {
            if (level >= 100) {
                0
            } else {
                val remaining = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) manager?.computeChargeTimeRemaining() ?: -1L else -1L
                if (remaining > 0) (remaining / 60_000).toInt() else rateMinutes(history, level, charging = true)
            }
        } else {
            rateMinutes(history, level, charging = false)
        }

        return BatteryInfo(
            level = level,
            charging = charging,
            temperatureTenths = sticky?.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, Int.MIN_VALUE)?.takeIf { it != Int.MIN_VALUE },
            millivolts = sticky?.getIntExtra(BatteryManager.EXTRA_VOLTAGE, -1)?.takeIf { it > 0 },
            cycles = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                sticky?.getIntExtra(BatteryManager.EXTRA_CYCLE_COUNT, -1)?.takeIf { it >= 0 }
            } else {
                null
            },
            health = sticky?.getIntExtra(BatteryManager.EXTRA_HEALTH, BatteryManager.BATTERY_HEALTH_UNKNOWN)
                ?: BatteryManager.BATTERY_HEALTH_UNKNOWN,
            minutesLeft = minutes,
            history = history,
        )
    }

    /** Ajoute un relevé, sauf si le précédent est trop récent et identique. */
    fun record(context: Context, level: Int, charging: Boolean, now: Long = System.currentTimeMillis()) {
        val list = samples(context).toMutableList()
        val last = list.lastOrNull()
        val changed = last == null || last.charging != charging || last.level != level
        if (last != null && now - last.time < MIN_GAP_MILLIS && !changed) return
        if (last != null && now - last.time < 60_000L) list.removeAt(list.lastIndex)
        list += BatterySample(now, level, charging)
        val kept = list.filter { now - it.time <= KEEP_MILLIS }
        prefs(context).edit()
            .putString(SAMPLES, kept.joinToString(";") { "${it.time},${it.level},${if (it.charging) 1 else 0}" })
            .apply()
    }

    fun samples(context: Context): List<BatterySample> =
        prefs(context).getString(SAMPLES, null).orEmpty().split(';').mapNotNull { entry ->
            val parts = entry.split(',')
            if (parts.size != 3) return@mapNotNull null
            val time = parts[0].toLongOrNull() ?: return@mapNotNull null
            val level = parts[1].toIntOrNull() ?: return@mapNotNull null
            BatterySample(time, level, parts[2] == "1")
        }

    /**
     * Minutes restantes au rythme des dernières heures passées dans le même
     * état (en charge ou non), ou `null` si ce rythme n'est pas encore connu.
     */
    private fun rateMinutes(history: List<BatterySample>, level: Int, charging: Boolean): Int? {
        // La dernière période continue dans cet état, sur 6 heures au plus.
        val run = history.takeLastWhile { it.charging == charging }
        val last = run.lastOrNull() ?: return null
        val first = run.firstOrNull { last.time - it.time <= 6 * 3_600_000L } ?: return null
        val hours = (last.time - first.time) / 3_600_000.0
        val delta = if (charging) last.level - first.level else first.level - last.level
        if (hours < 0.5 || delta < 2) return null
        val perHour = delta / hours
        val remaining = if (charging) 100 - level else level
        return (remaining / perHour * 60).toInt()
    }

    private fun prefs(context: Context) = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
}
