package dev.levilainpetit.wux.calendar

import android.Manifest
import android.content.ContentUris
import android.content.Context
import android.content.pm.PackageManager
import android.provider.CalendarContract.Calendars
import android.provider.CalendarContract.Instances

/** Un agenda du téléphone, tel que l'utilisateur peut le choisir. */
data class CalendarInfo(
    val id: Long,
    val name: String,
    val account: String,
    val color: Int,
)

/** Une occurrence d'événement (les événements récurrents sont déjà dépliés). */
data class CalendarEvent(
    val eventId: Long,
    val title: String,
    val location: String,
    val begin: Long,
    val end: Long,
    val allDay: Boolean,
    val color: Int,
)

/**
 * Seul point d'accès au fournisseur de calendrier d'Android.
 *
 * Utilisé à la fois par les widgets et par l'aperçu de l'application, pour que
 * les deux montrent exactement la même chose.
 */
class CalendarRepository(private val context: Context) {

    fun hasPermission(): Boolean =
        context.checkSelfPermission(Manifest.permission.READ_CALENDAR) ==
            PackageManager.PERMISSION_GRANTED

    fun calendars(): List<CalendarInfo> {
        if (!hasPermission()) return emptyList()
        val projection = arrayOf(
            Calendars._ID,
            Calendars.CALENDAR_DISPLAY_NAME,
            Calendars.ACCOUNT_NAME,
            Calendars.CALENDAR_COLOR,
        )
        val cursor = context.contentResolver.query(
            Calendars.CONTENT_URI,
            projection,
            "${Calendars.VISIBLE} = 1",
            null,
            "${Calendars.ACCOUNT_NAME}, ${Calendars.CALENDAR_DISPLAY_NAME}",
        ) ?: return emptyList()
        return cursor.use { c ->
            buildList {
                while (c.moveToNext()) {
                    add(
                        CalendarInfo(
                            id = c.getLong(0),
                            name = c.getString(1).orEmpty(),
                            account = c.getString(2).orEmpty(),
                            color = c.getInt(3),
                        ),
                    )
                }
            }
        }
    }

    /**
     * Occurrences qui chevauchent [from, to[, triées par début.
     *
     * [calendarIds] à `null` signifie « tous les agendas visibles ».
     */
    fun events(from: Long, to: Long, calendarIds: Set<Long>?): List<CalendarEvent> {
        if (!hasPermission()) return emptyList()
        if (calendarIds != null && calendarIds.isEmpty()) return emptyList()

        val uri = Instances.CONTENT_URI.buildUpon().also {
            ContentUris.appendId(it, from)
            ContentUris.appendId(it, to)
        }.build()
        val projection = arrayOf(
            Instances.EVENT_ID,
            Instances.TITLE,
            Instances.EVENT_LOCATION,
            Instances.BEGIN,
            Instances.END,
            Instances.ALL_DAY,
            Instances.DISPLAY_COLOR,
        )
        var selection = "${Instances.VISIBLE} = 1"
        var args: Array<String>? = null
        if (calendarIds != null) {
            selection += " AND ${Instances.CALENDAR_ID} IN (${calendarIds.joinToString { "?" }})"
            args = calendarIds.map { it.toString() }.toTypedArray()
        }
        val cursor = context.contentResolver.query(
            uri,
            projection,
            selection,
            args,
            "${Instances.BEGIN} ASC",
        ) ?: return emptyList()
        return cursor.use { c ->
            buildList {
                while (c.moveToNext()) {
                    add(
                        CalendarEvent(
                            eventId = c.getLong(0),
                            title = c.getString(1).orEmpty(),
                            location = c.getString(2).orEmpty(),
                            begin = c.getLong(3),
                            end = c.getLong(4),
                            allDay = c.getInt(5) == 1,
                            color = c.getInt(6),
                        ),
                    )
                }
            }
        }
    }
}
