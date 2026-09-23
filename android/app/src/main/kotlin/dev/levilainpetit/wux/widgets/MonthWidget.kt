package dev.levilainpetit.wux.widgets

import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.provider.CalendarContract
import android.util.SizeF
import android.view.View
import android.widget.RemoteViews
import dev.levilainpetit.wux.R
import dev.levilainpetit.wux.calendar.CalendarRepository
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.time.temporal.WeekFields
import java.util.Locale

/**
 * Mois : la grille du mois en cours, aujourd'hui mis en valeur et un point
 * sous chaque jour qui a un événement (tous agendas visibles). Toucher un
 * jour l'ouvre dans l'agenda.
 */
class MonthWidget : NeonWidget() {

    override val refreshActions = DATE_ACTIONS

    override fun build(context: Context, size: SizeF, sample: Boolean): RemoteViews {
        val zone = ZoneId.systemDefault()
        val today = LocalDate.now(zone)
        val month = YearMonth.from(today)
        val locale = Locale.getDefault()
        val firstDay = WeekFields.of(locale).firstDayOfWeek
        val busy = if (sample) SampleData.busyDays(month) else busyDays(context, month, zone)

        val views = RemoteViews(context.packageName, R.layout.widget_month)
        views.setTextViewText(
            R.id.month_title,
            DateTimeFormatter.ofPattern("LLLL yyyy", locale).format(today).uppercase(locale),
        )
        views.removeAllViews(R.id.month_grid)

        val header = RemoteViews(context.packageName, R.layout.month_week)
        for (i in 0 until 7) {
            val day = firstDay.plus(i.toLong())
            header.addView(
                R.id.month_week,
                RemoteViews(context.packageName, R.layout.month_weekday).apply {
                    setTextViewText(R.id.month_weekday, day.getDisplayName(TextStyle.NARROW, locale))
                },
            )
        }
        views.addView(R.id.month_grid, header)

        val offset = (month.atDay(1).dayOfWeek.value - firstDay.value + 7) % 7
        var date = month.atDay(1).minusDays(offset.toLong())
        repeat(6) { week ->
            if (week > 0 && date.month != month.month) return@repeat
            val row = RemoteViews(context.packageName, R.layout.month_week)
            repeat(7) {
                row.addView(R.id.month_week, dayView(context, date, month, today, busy, zone))
                date = date.plusDays(1)
            }
            views.addView(R.id.month_grid, row)
        }
        return views
    }

    private fun dayView(
        context: Context,
        date: LocalDate,
        month: YearMonth,
        today: LocalDate,
        busy: Set<LocalDate>,
        zone: ZoneId,
    ): RemoteViews {
        val layout = if (date == today) R.layout.month_day_today else R.layout.month_day
        return RemoteViews(context.packageName, layout).apply {
            setTextViewText(R.id.month_day, date.dayOfMonth.toString())
            if (date.month != month.month) setTextColor(R.id.month_day, context.getColor(R.color.clock_glow))
            setViewVisibility(R.id.month_dot, if (date in busy && date.month == month.month) View.VISIBLE else View.INVISIBLE)
            val millis = date.atStartOfDay(zone).toInstant().toEpochMilli()
            val open = Intent(
                Intent.ACTION_VIEW,
                CalendarContract.CONTENT_URI.buildUpon().appendPath("time").also { ContentUris.appendId(it, millis) }.build(),
            )
            setOnClickPendingIntent(R.id.month_cell, activity(context, open, 500 + date.dayOfYear))
        }
    }

    /** Jours du mois qui portent au moins un événement. */
    private fun busyDays(context: Context, month: YearMonth, zone: ZoneId): Set<LocalDate> {
        val repository = CalendarRepository(context)
        if (!repository.hasPermission()) return emptySet()
        val start = month.atDay(1).atStartOfDay(zone)
        val end = month.plusMonths(1).atDay(1).atStartOfDay(zone)
        val days = mutableSetOf<LocalDate>()
        repository.events(start.minusDays(1).toInstant().toEpochMilli(), end.plusDays(1).toInstant().toEpochMilli(), null)
            .forEach { event ->
                val (first, last) = if (event.allDay) {
                    Instant.ofEpochMilli(event.begin).atOffset(ZoneOffset.UTC).toLocalDate() to
                        Instant.ofEpochMilli(event.end).atOffset(ZoneOffset.UTC).toLocalDate().minusDays(1)
                } else {
                    Instant.ofEpochMilli(event.begin).atZone(zone).toLocalDate() to
                        Instant.ofEpochMilli(maxOf(event.begin, event.end - 1)).atZone(zone).toLocalDate()
                }
                var d = first
                while (!d.isAfter(last)) {
                    if (YearMonth.from(d) == month) days += d
                    d = d.plusDays(1)
                }
            }
        return days
    }

    override fun onRendered(context: Context) = refreshAfterMidnight(context)
}
