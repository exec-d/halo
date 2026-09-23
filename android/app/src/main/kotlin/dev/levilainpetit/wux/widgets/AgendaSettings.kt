package dev.levilainpetit.wux.widgets

import android.content.SharedPreferences

/**
 * Réglages d'un widget agenda, écrits par l'application Flutter via
 * `HomeWidget.saveWidgetData` sous la forme `<id>.<nom>`.
 */
data class AgendaSettings(
    /** `null` : tous les agendas visibles. */
    val calendarIds: Set<Long>?,
) {
    companion object {
        fun read(prefs: SharedPreferences, widgetId: String): AgendaSettings {
            val calendars = prefs.getString("$widgetId.calendars", null)
            return AgendaSettings(
                calendarIds = calendars?.split(',')?.mapNotNull { it.trim().toLongOrNull() }?.toSet(),
            )
        }
    }
}
