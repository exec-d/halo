package dev.levilainpetit.wux.widgets

import android.app.AlarmManager
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.provider.AlarmClock
import android.view.View
import android.widget.RemoteViews
import dev.levilainpetit.wux.R

/**
 * Widget horloge : heure, date et prochaine alarme.
 *
 * En `RemoteViews` plutôt qu'en Glance : `TextClock` se met à jour seul chaque
 * minute sans réveiller l'application, ce que Glance ne sait pas faire. La
 * prochaine alarme, elle, est redessinée quand le système annonce qu'elle a
 * changé (voir le `<receiver>` dans `AndroidManifest.xml`).
 */
class ClockWidgetProvider : AppWidgetProvider() {

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action in REFRESH_ACTIONS) {
            val manager = AppWidgetManager.getInstance(context)
            val ids = manager.getAppWidgetIds(ComponentName(context, ClockWidgetProvider::class.java))
            if (ids.isNotEmpty()) onUpdate(context, manager, ids)
        }
    }

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
        val alarm = NextAlarm.label(context)
        val views = RemoteViews(context.packageName, R.layout.widget_clock).apply {
            setOnClickPendingIntent(R.id.clock_root, openClock)
            setViewVisibility(R.id.clock_alarm, if (alarm == null) View.GONE else View.VISIBLE)
            setTextViewText(R.id.clock_alarm_text, alarm.orEmpty())
        }
        appWidgetManager.updateAppWidget(appWidgetIds, views)
    }

    private companion object {
        val REFRESH_ACTIONS = setOf(
            AlarmManager.ACTION_NEXT_ALARM_CLOCK_CHANGED,
            Intent.ACTION_TIME_CHANGED,
            Intent.ACTION_TIMEZONE_CHANGED,
            Intent.ACTION_LOCALE_CHANGED,
        )
    }
}
