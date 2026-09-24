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
    /** Occupe toute la journée affichée (événement « toute la journée » ou sur plusieurs jours). */
    val allDay: Boolean = false,
    /** Commencé et pas encore terminé : mis en avant. */
    val ongoing: Boolean = false,
    /** Suit un événement en cours le même jour : légèrement estompé. */
    val later: Boolean = false,
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
        showAllDay: Boolean = true,
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
                .map { it.toLine(context, dayStart, dayEnd, zone, timeFormat, nowMillis.takeIf { offset == 0 }) }
                .filter { showAllDay || !it.allDay }
                .let { lines ->
                    // Estomper n'a de sens qu'à côté d'un événement mis en avant.
                    if (lines.none { it.ongoing }) lines else lines.map { it.copy(later = !it.ongoing) }
                }
            AgendaDay(date, dayLabel(context, offset, date), lines)
        }
    }

    /**
     * Prochain instant où l'agenda affiché change sans que le calendrier ne
     * bouge : le début d'un événement du jour (il passe « en cours »), sa fin,
     * ou le passage en fin de journée.
     * Tous agendas confondus, ce qui peut réveiller le widget pour rien, jamais
     * trop tard.
     */
    fun nextChange(repository: CalendarRepository, now: ZonedDateTime = ZonedDateTime.now()): ZonedDateTime? {
        val nowMillis = now.toInstant().toEpochMilli()
        val todayEnd = now.toLocalDate().plusDays(1).atStartOfDay(now.zone)
        val limit = todayEnd.toInstant().toEpochMilli()
        val changes = repository.events(nowMillis, limit, null)
            .filter { !it.allDay }
            .flatMap { listOf(it.begin, it.end) }
            .filter { it in (nowMillis + 1) until limit }
            .map { Instant.ofEpochMilli(it).atZone(now.zone) }
        val endOfDay = now.toLocalDate().atTime(END_OF_DAY_HOUR, 0).atZone(now.zone)
        return (changes + listOfNotNull(endOfDay.takeIf { it.isAfter(now) })).minOrNull()
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
        /** `null` hors du jour même : rien n'y est « en cours ». */
        nowMillis: Long?,
    ): AgendaLine {
        val from = dayStart.toInstant().toEpochMilli()
        val to = dayEnd.toInstant().toEpochMilli()
        fun format(millis: Long) = timeFormat.format(Instant.ofEpochMilli(millis).atZone(zone))
        val startsBefore = begin < from
        val endsAfter = end > to
        val wholeDay = allDay || (startsBefore && endsAfter)
        val time = when {
            wholeDay -> context.getString(R.string.agenda_all_day)
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
            allDay = wholeDay,
            ongoing = !wholeDay && nowMillis != null && begin <= nowMillis && nowMillis < end,
        )
    }

    private fun dayLabel(context: Context, offset: Int, date: LocalDate): String =
        when (offset) {
            0 -> context.getString(R.string.agenda_today)
            1 -> context.getString(R.string.agenda_tomorrow)
            else -> DateTimeFormatter.ofPattern("EEE d MMM", Locale.getDefault()).format(date)
        }.uppercase(Locale.getDefault())
}
