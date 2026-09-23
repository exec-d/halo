package dev.levilainpetit.wux.widgets

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.provider.AlarmClock
import android.widget.RemoteViews
import dev.levilainpetit.wux.R

/**
 * Widget horloge : heure et date.
 *
 * En `RemoteViews` plutôt qu'en Glance : `TextClock` se met à jour seul chaque
 * minute sans réveiller l'application, ce que Glance ne sait pas faire.
 */
class ClockWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray,
    ) {
        val openClock = PendingIntent.getActivity(
            context,
            0,
            Intent(AlarmClock.ACTION_SHOW_ALARMS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )
        val views = RemoteViews(context.packageName, R.layout.widget_clock).apply {
            setOnClickPendingIntent(R.id.clock_root, openClock)
        }
        appWidgetManager.updateAppWidget(appWidgetIds, views)
    }
}
