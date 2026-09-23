package dev.levilainpetit.wux.widgets

import android.app.AlarmManager
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.AlarmClock
import android.util.SizeF
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
 *
 * Deux mises en page : en colonne (`widget_clock`) et, quand le widget est
 * réduit à une rangée, en ligne (`widget_clock_compact`).
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
        appWidgetIds.forEach { id ->
            appWidgetManager.updateAppWidget(id, views(context, appWidgetManager.getAppWidgetOptions(id)))
        }
    }

    override fun onAppWidgetOptionsChanged(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int,
        newOptions: Bundle,
    ) {
        // Avant Android 12, c'est ici que l'on apprend le redimensionnement.
        appWidgetManager.updateAppWidget(appWidgetId, views(context, newOptions))
    }

    private fun views(context: Context, options: Bundle): RemoteViews {
        val compact = build(context, R.layout.widget_clock_compact)
        val full = build(context, R.layout.widget_clock)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            // Le lanceur choisit lui-même la plus grande qui tient.
            return RemoteViews(mapOf(SizeF(100f, 40f) to compact, SizeF(160f, FULL_MIN_HEIGHT) to full))
        }
        val height = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT, FULL_MIN_HEIGHT.toInt())
        return if (height < FULL_MIN_HEIGHT) compact else full
    }

    private fun build(context: Context, layout: Int): RemoteViews {
        val openClock = PendingIntent.getActivity(
            context,
            0,
            Intent(AlarmClock.ACTION_SHOW_ALARMS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )
        val alarm = NextAlarm.label(context)
        return RemoteViews(context.packageName, layout).apply {
            setOnClickPendingIntent(R.id.clock_root, openClock)
            setViewVisibility(R.id.clock_alarm, if (alarm == null) View.GONE else View.VISIBLE)
            setTextViewText(R.id.clock_alarm_text, alarm.orEmpty())
        }
    }

    private companion object {
        /** Hauteur (dp) à partir de laquelle l'heure, la date et l'alarme s'empilent. */
        const val FULL_MIN_HEIGHT = 100f

        val REFRESH_ACTIONS = setOf(
            AlarmManager.ACTION_NEXT_ALARM_CLOCK_CHANGED,
            Intent.ACTION_TIME_CHANGED,
            Intent.ACTION_TIMEZONE_CHANGED,
            Intent.ACTION_LOCALE_CHANGED,
        )
    }
}
