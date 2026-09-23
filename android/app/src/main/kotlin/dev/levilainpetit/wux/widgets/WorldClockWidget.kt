package dev.levilainpetit.wux.widgets

import android.content.Context
import android.content.Intent
import android.provider.AlarmClock
import android.util.SizeF
import android.widget.RemoteViews
import dev.levilainpetit.wux.R

/**
 * Fuseaux horaires : jusqu'à trois villes, chacune un `TextClock` réglé sur
 * son fuseau, qui avance seul. Réglage `world_clock.zones` :
 * « Ville|Zone/Id » séparés par des virgules.
 */
class WorldClockWidget : NeonWidget() {

    override fun build(context: Context, size: SizeF, sample: Boolean): RemoteViews {
        val views = RemoteViews(context.packageName, R.layout.widget_world_clock)
        views.removeAllViews(R.id.world_row)
        zones(context).forEachIndexed { i, (city, zone) ->
            if (i > 0) views.addView(R.id.world_row, RemoteViews(context.packageName, R.layout.system_divider))
            views.addView(
                R.id.world_row,
                RemoteViews(context.packageName, R.layout.world_clock_city).apply {
                    setTextViewText(R.id.world_city, city)
                    setString(R.id.world_time, "setTimeZone", zone)
                    setString(R.id.world_day, "setTimeZone", zone)
                },
            )
        }
        views.setOnClickPendingIntent(
            R.id.world_row,
            activity(context, Intent(AlarmClock.ACTION_SHOW_ALARMS), 30),
        )
        return views
    }

    private fun zones(context: Context): List<Pair<String, String>> {
        val stored = settings(context).getString("world_clock.zones", null)
            ?: "Paris|Europe/Paris,New York|America/New_York,Tokyo|Asia/Tokyo"
        return stored.split(',').mapNotNull {
            val parts = it.split('|')
            if (parts.size == 2) parts[0] to parts[1] else null
        }.take(3)
    }
}
