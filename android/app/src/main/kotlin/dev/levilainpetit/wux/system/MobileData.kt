package dev.levilainpetit.wux.system

import android.app.usage.NetworkStatsManager
import android.content.Context
import android.net.ConnectivityManager
import java.time.LocalDate
import java.time.ZoneId
import kotlin.math.min

/**
 * Les données mobiles de la période de facturation en cours, jour par jour,
 * pour le widget Données mobiles.
 */
data class MobileData(
    val start: LocalDate,
    /** Premier jour de la période suivante. */
    val end: LocalDate,
    /** Octets par jour, de [start] à aujourd'hui compris. */
    val days: List<Long>,
    /** Forfait en octets, ou `null` s'il n'est pas renseigné. */
    val quota: Long?,
) {
    val used: Long get() = days.sum()
    val today: Long get() = days.lastOrNull() ?: 0L
    val length: Int get() = (end.toEpochDay() - start.toEpochDay()).toInt()

    /** Au rythme moyen de la période, fin de période comprise. */
    val projected: Long
        get() = if (days.isEmpty()) 0L else used / days.size * length

    val daysLeft: Int get() = (end.toEpochDay() - LocalDate.now().toEpochDay()).toInt()

    companion object {
        const val GB = 1_000_000_000L

        /**
         * [cycleDay] : jour du mois où le forfait repart (1–28) ; [quotaGb] :
         * forfait en Go, 0 pour aucun. `null` sans l'accès aux données
         * d'utilisation.
         */
        fun current(context: Context, cycleDay: Int, quotaGb: Double): MobileData? {
            if (!UsageAccess.granted(context)) return null
            val manager = context.getSystemService(NetworkStatsManager::class.java) ?: return null
            val today = LocalDate.now()
            val (start, end) = period(today, cycleDay)
            val zone = ZoneId.systemDefault()
            val days = (0..(today.toEpochDay() - start.toEpochDay())).map { offset ->
                val day = start.plusDays(offset)
                val from = day.atStartOfDay(zone).toInstant().toEpochMilli()
                val to = min(day.plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli(), System.currentTimeMillis())
                runCatching {
                    @Suppress("DEPRECATION")
                    val bucket = manager.querySummaryForDevice(ConnectivityManager.TYPE_MOBILE, null, from, to)
                    bucket.rxBytes + bucket.txBytes
                }.getOrDefault(0L)
            }
            return MobileData(start, end, days, quota(quotaGb))
        }

        fun sample(): MobileData {
            val today = LocalDate.now()
            val (start, end) = period(today, 1)
            val pattern = longArrayOf(420, 310, 650, 280, 190, 820, 540, 360, 300, 710, 480, 260, 390, 580)
            val days = (0..(today.toEpochDay() - start.toEpochDay())).map { pattern[(it % pattern.size).toInt()] * 1_000_000L }
            return MobileData(start, end, days, 50 * GB)
        }

        /** La période qui contient [today], pour un forfait qui repart le [cycleDay]. */
        fun period(today: LocalDate, cycleDay: Int): Pair<LocalDate, LocalDate> {
            val day = cycleDay.coerceIn(1, 28)
            val thisMonth = today.withDayOfMonth(day)
            val start = if (today.dayOfMonth >= day) thisMonth else thisMonth.minusMonths(1)
            return start to start.plusMonths(1)
        }

        private fun quota(gb: Double): Long? = if (gb > 0) (gb * GB).toLong() else null
    }
}
