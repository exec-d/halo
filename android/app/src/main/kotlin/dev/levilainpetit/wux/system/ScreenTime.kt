package dev.levilainpetit.wux.system

import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import java.time.LocalDate
import java.time.ZoneId

/** Une appli et son temps au premier plan aujourd'hui. */
data class AppUsage(val packageName: String, val label: String, val millis: Long, val icon: Drawable?)

/** Le temps d'écran du jour, pour le widget du même nom. */
data class ScreenTime(
    /** Périodes écran allumé depuis minuit : (début, fin) en ms. */
    val sessions: List<Pair<Long, Long>>,
    val totalMillis: Long,
    val unlocks: Int,
    /** Les plus utilisées, dans l'ordre. */
    val apps: List<AppUsage>,
    val dayStart: Long,
) {
    companion object {
        /** `null` sans l'accès aux données d'utilisation. */
        fun today(context: Context, now: Long = System.currentTimeMillis()): ScreenTime? {
            if (!UsageAccess.granted(context)) return null
            val manager = context.getSystemService(UsageStatsManager::class.java) ?: return null
            val start = LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
            val events = manager.queryEvents(start, now) ?: return null

            val sessions = mutableListOf<Pair<Long, Long>>()
            var screenOn: Long? = null
            var sawScreenEvent = false
            var unlocks = 0
            val resumed = mutableMapOf<String, Long>()
            val perApp = mutableMapOf<String, Long>()
            val ignored = ignoredPackages(context)
            val event = UsageEvents.Event()
            while (events.hasNextEvent()) {
                events.getNextEvent(event)
                when (event.eventType) {
                    UsageEvents.Event.SCREEN_INTERACTIVE -> {
                        sawScreenEvent = true
                        screenOn = event.timeStamp
                    }
                    UsageEvents.Event.SCREEN_NON_INTERACTIVE -> {
                        // Écran déjà allumé à minuit : la séance commence à minuit.
                        val from = screenOn ?: if (!sawScreenEvent) start else null
                        if (from != null) sessions += from to event.timeStamp
                        sawScreenEvent = true
                        screenOn = null
                    }
                    UsageEvents.Event.KEYGUARD_HIDDEN -> unlocks++
                    UsageEvents.Event.ACTIVITY_RESUMED -> resumed[event.packageName] = event.timeStamp
                    UsageEvents.Event.ACTIVITY_PAUSED -> {
                        val since = resumed.remove(event.packageName) ?: continue
                        if (event.packageName !in ignored) {
                            perApp[event.packageName] = (perApp[event.packageName] ?: 0L) + (event.timeStamp - since)
                        }
                    }
                }
            }
            screenOn?.let { sessions += it to now }
            resumed.forEach { (pkg, since) -> if (pkg !in ignored) perApp[pkg] = (perApp[pkg] ?: 0L) + (now - since) }

            val pm = context.packageManager
            val apps = perApp.entries
                .filter { it.value >= 60_000L }
                .sortedByDescending { it.value }
                .take(3)
                .map { (pkg, millis) ->
                    val info = runCatching { pm.getApplicationInfo(pkg, 0) }.getOrNull()
                    AppUsage(
                        packageName = pkg,
                        label = info?.let { pm.getApplicationLabel(it).toString() } ?: pkg,
                        millis = millis,
                        icon = info?.let { runCatching { pm.getApplicationIcon(it) }.getOrNull() },
                    )
                }
            return ScreenTime(
                sessions = sessions,
                totalMillis = sessions.sumOf { it.second - it.first },
                unlocks = unlocks,
                apps = apps,
                dayStart = start,
            )
        }

        fun sample(now: Long = System.currentTimeMillis()): ScreenTime {
            val start = LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
            val hour = 3_600_000L
            val sessions = listOf(
                7.1 to 7.6, 8.2 to 8.5, 9.9 to 10.3, 12.4 to 13.2, 15.0 to 15.2, 18.3 to 19.4, 20.8 to 21.9,
            ).map { (a, b) -> start + (a * hour).toLong() to start + (b * hour).toLong() }
                .filter { it.first < now }
                .map { it.first to minOf(it.second, now) }
            return ScreenTime(
                sessions = sessions,
                totalMillis = 3 * hour + 12 * 60_000L,
                unlocks = 42,
                apps = listOf(
                    AppUsage("chrome", "Chrome", 72 * 60_000L, null),
                    AppUsage("messages", "Messages", 41 * 60_000L, null),
                    AppUsage("maps", "Maps", 23 * 60_000L, null),
                ),
                dayStart = start,
            )
        }

        /** L'écran d'accueil et l'interface système ne comptent pas comme des applis. */
        private fun ignoredPackages(context: Context): Set<String> {
            val home = context.packageManager
                .resolveActivity(Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME), PackageManager.MATCH_DEFAULT_ONLY)
                ?.activityInfo?.packageName
            return setOfNotNull(home, "com.android.systemui")
        }
    }
}
