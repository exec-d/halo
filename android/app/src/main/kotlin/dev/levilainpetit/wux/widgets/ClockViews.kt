package dev.levilainpetit.wux.widgets

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.provider.AlarmClock
import android.view.View
import android.widget.RemoteViews
import dev.levilainpetit.wux.R

/**
 * Remplit le bloc horloge (`clock_root`, `clock_alarm`, `clock_alarm_text`)
 * des deux mises en page du widget horloge.
 */
object ClockViews {

    /** Diffusions après lesquelles la prochaine alarme affichée peut changer. */
    val REFRESH_ACTIONS = setOf(
        AlarmManager.ACTION_NEXT_ALARM_CLOCK_CHANGED,
        Intent.ACTION_TIME_CHANGED,
        Intent.ACTION_TIMEZONE_CHANGED,
        Intent.ACTION_LOCALE_CHANGED,
    )

    fun bind(context: Context, views: RemoteViews) {
        val openClock = PendingIntent.getActivity(
            context,
            0,
            Intent(AlarmClock.ACTION_SHOW_ALARMS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )
        val alarm = NextAlarm.label(context)
        views.setOnClickPendingIntent(R.id.clock_root, openClock)
        views.setViewVisibility(R.id.clock_alarm, if (alarm == null) View.GONE else View.VISIBLE)
        views.setTextViewText(R.id.clock_alarm_text, alarm.orEmpty())
    }
}
