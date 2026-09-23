package dev.levilainpetit.wux.widgets

import android.app.AlarmManager
import android.content.Context
import android.text.format.DateFormat

/** Prochaine alarme réglée sur le téléphone, toutes applications confondues. */
object NextAlarm {

    /** « mer. 07:00 », ou `null` si aucune alarme n'est programmée. */
    fun label(context: Context): String? {
        val alarm = context.getSystemService(AlarmManager::class.java)?.nextAlarmClock ?: return null
        val pattern = if (DateFormat.is24HourFormat(context)) "EEE HH:mm" else "EEE h:mm a"
        return DateFormat.format(pattern, alarm.triggerTime).toString()
    }
}
