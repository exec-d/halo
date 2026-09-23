package dev.levilainpetit.wux.widgets

import android.content.SharedPreferences

/** Fond d'un widget agenda. */
enum class AgendaBackground(val key: String) {
    /** Texte clair posé directement sur le fond d'écran, comme la maquette. */
    TRANSPARENT("transparent"),

    /** Surface aux couleurs du fond d'écran (Material You, Android 12+). */
    SURFACE("surface"),
    ;

    companion object {
        fun fromKey(key: String?) = entries.firstOrNull { it.key == key } ?: TRANSPARENT
    }
}

/**
 * Réglages d'un widget agenda, écrits par l'application Flutter via
 * `HomeWidget.saveWidgetData` sous la forme `<id>.<nom>`.
 */
data class AgendaSettings(
    /** `null` : tous les agendas visibles. */
    val calendarIds: Set<Long>?,
    val background: AgendaBackground,
) {
    companion object {
        fun read(prefs: SharedPreferences, widgetId: String): AgendaSettings {
            val calendars = prefs.getString("$widgetId.calendars", null)
            return AgendaSettings(
                calendarIds = calendars?.split(',')?.mapNotNull { it.trim().toLongOrNull() }?.toSet(),
                background = AgendaBackground.fromKey(prefs.getString("$widgetId.background", null)),
            )
        }
    }
}
