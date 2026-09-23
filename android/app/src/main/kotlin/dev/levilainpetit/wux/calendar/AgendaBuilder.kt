package dev.levilainpetit.wux.calendar

import android.content.Context
import android.text.format.DateFormat
import dev.levilainpetit.wux.R
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

/** Une ligne d'événement, prête à afficher. */
data class AgendaLine(
    val eventId: Long,
    val begin: Long,
    val end: Long,
    val time: String,
    val title: String,
    val location: String,
    val color: Int,
) {
    /** Seconde ligne affichée : l'horaire, puis le lieu s'il y en a un. */
    val detail: String
        get() = if (location.isBlank()) time else "$time · $location"
}

/** Un jour de l'agenda : son titre et ses événements. */
data class AgendaDay(
    val date: LocalDate,
    val label: String,
    val lines: List<AgendaLine>,
)

/**
 * Regroupe les événements par jour et produit les textes affichés.
 *
 * Toute la mise en forme vit ici, pour que le widget et l'aperçu de
 * l'application ne puissent pas diverger.
 */
object AgendaBuilder {

    /**
     * Heure à partir de laquelle une journée sans plus aucun événement à venir
     * est considérée comme finie : l'agenda passe alors au lendemain.
     */
    const val END_OF_DAY_HOUR = 18

    fun build(
        context: Context,
        repository: CalendarRepository,
        days: Int,
        calendarIds: Set<Long>?,
        now: ZonedDateTime = ZonedDateTime.now(),
    ): List<AgendaDay> {
        val zone = now.zone
        val nowMillis = now.toInstant().toEpochMilli()
        val today = now.toLocalDate()
        val start = today.atStartOfDay(zone)
        // Un jour de plus que demandé, pour le décalage de fin de journée.
        val end = today.plusDays(days + 1L).atStartOfDay(zone)
        // Les événements « toute la journée » sont stockés en UTC : on élargit
        // la requête d'un jour de chaque côté puis on les range par date UTC.
        val events = repository.events(
            start.minusDays(1).toInstant().toEpochMilli(),
            end.plusDays(1).toInstant().toEpochMilli(),
            calendarIds,
        )
        val timeFormat = DateTimeFormatter.ofPattern(
            if (DateFormat.is24HourFormat(context)) "HH:mm" else "h:mm a",
            Locale.getDefault(),
        )

        // En fin de journée, s'il ne reste plus rien à venir aujourd'hui, la
        // fenêtre commence au lendemain.
        val todayEnd = today.plusDays(1).atStartOfDay(zone)
        val remainingToday = events.any {
            !it.allDay && it.occursOn(today, start, todayEnd) && it.end > nowMillis
        }
        val first = if (now.hour >= END_OF_DAY_HOUR && !remainingToday) 1 else 0

        return (first until first + days).map { offset ->
            val date = today.plusDays(offset.toLong())
            val dayStart = date.atStartOfDay(zone)
            val dayEnd = date.plusDays(1).atStartOfDay(zone)
            val lines = events
                .filter { it.occursOn(date, dayStart, dayEnd) }
                // Sur le jour même, un événement terminé n'a plus d'intérêt.
                .filter { offset > 0 || it.allDay || it.end > nowMillis }
                .sortedWith(compareByDescending<CalendarEvent> { it.allDay }.thenBy { it.begin })
                .map { it.toLine(context, dayStart, dayEnd, zone, timeFormat) }
            AgendaDay(date, dayLabel(context, offset, date), lines)
        }
    }

    /**
     * Prochain instant où l'agenda affiché change sans que le calendrier ne
     * bouge : la fin d'un événement du jour, ou le passage en fin de journée.
     * Tous agendas confondus, ce qui peut réveiller le widget pour rien, jamais
     * trop tard.
     */
    fun nextChange(repository: CalendarRepository, now: ZonedDateTime = ZonedDateTime.now()): ZonedDateTime? {
        val nowMillis = now.toInstant().toEpochMilli()
        val todayEnd = now.toLocalDate().plusDays(1).atStartOfDay(now.zone)
        val ends = repository.events(nowMillis, todayEnd.toInstant().toEpochMilli(), null)
            .filter { !it.allDay && it.end > nowMillis && it.end < todayEnd.toInstant().toEpochMilli() }
            .map { Instant.ofEpochMilli(it.end).atZone(now.zone) }
        val endOfDay = now.toLocalDate().atTime(END_OF_DAY_HOUR, 0).atZone(now.zone)
        return (ends + listOfNotNull(endOfDay.takeIf { it.isAfter(now) })).minOrNull()
    }

    private fun CalendarEvent.occursOn(
        date: LocalDate,
        dayStart: ZonedDateTime,
        dayEnd: ZonedDateTime,
    ): Boolean {
        if (allDay) {
            val first = Instant.ofEpochMilli(begin).atOffset(ZoneOffset.UTC).toLocalDate()
            val last = Instant.ofEpochMilli(end).atOffset(ZoneOffset.UTC).toLocalDate()
            return !date.isBefore(first) && date.isBefore(last)
        }
        val from = dayStart.toInstant().toEpochMilli()
        val to = dayEnd.toInstant().toEpochMilli()
        // Un événement de durée nulle compte le jour de son début.
        return begin < to && (end > from || (begin == end && begin >= from))
    }

    private fun CalendarEvent.toLine(
        context: Context,
        dayStart: ZonedDateTime,
        dayEnd: ZonedDateTime,
        zone: ZoneId,
        timeFormat: DateTimeFormatter,
    ): AgendaLine {
        val from = dayStart.toInstant().toEpochMilli()
        val to = dayEnd.toInstant().toEpochMilli()
        fun format(millis: Long) = timeFormat.format(Instant.ofEpochMilli(millis).atZone(zone))
        val startsBefore = begin < from
        val endsAfter = end > to
        val time = when {
            allDay || (startsBefore && endsAfter) -> context.getString(R.string.agenda_all_day)
            startsBefore -> context.getString(R.string.agenda_until, format(end))
            endsAfter -> context.getString(R.string.agenda_from, format(begin))
            begin == end -> format(begin)
            else -> context.getString(R.string.agenda_range, format(begin), format(end))
        }
        return AgendaLine(
            eventId = eventId,
            begin = begin,
            end = end,
            time = time,
            title = title.ifBlank { context.getString(R.string.agenda_untitled) },
            location = location,
            color = color,
        )
    }

    private fun dayLabel(context: Context, offset: Int, date: LocalDate): String =
        when (offset) {
            0 -> context.getString(R.string.agenda_today)
            1 -> context.getString(R.string.agenda_tomorrow)
            else -> DateTimeFormatter.ofPattern("EEE d MMM", Locale.getDefault()).format(date)
        }.uppercase(Locale.getDefault())
}
