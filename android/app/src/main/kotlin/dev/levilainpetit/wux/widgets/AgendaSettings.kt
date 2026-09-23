package dev.levilainpetit.wux.widgets

import android.content.SharedPreferences

/**
 * Réglages d'un widget agenda, écrits par l'application Flutter via
 * `HomeWidget.saveWidgetData` sous la forme `<id>.<nom>`.
 */
data class AgendaSettings(
    /** `null` : tous les agendas visibles. */
    val calendarIds: Set<Long>?,
    /** 1 : aujourd'hui ; 2 : aujourd'hui et demain (par défaut). */
    val days: Int,
) {
    companion object {
        fun read(prefs: SharedPreferences, widgetId: String): AgendaSettings {
            val calendars = prefs.getString("$widgetId.calendars", null)
            return AgendaSettings(
                calendarIds = calendars?.split(',')?.mapNotNull { it.trim().toLongOrNull() }?.toSet(),
                days = if (prefs.getString("$widgetId.days", null) == "1") 1 else 2,
            )
        }
    }
}
