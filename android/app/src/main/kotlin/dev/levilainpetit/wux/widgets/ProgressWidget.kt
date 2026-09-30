package dev.levilainpetit.wux.widgets

import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.provider.CalendarContract
import android.util.SizeF
import android.view.View
import android.widget.RemoteViews
import dev.levilainpetit.wux.R
import java.time.DayOfWeek
import java.time.Duration
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.time.temporal.TemporalAdjusters
import java.util.Locale
import kotlin.math.roundToInt

/**
 * Progression : la part écoulée du jour, de la semaine (lundi → dimanche), du
 * mois et de l'année, en quatre barres. Redessiné tous les quarts d'heure.
 */
class ProgressWidget : NeonWidget() {

    override val refreshActions = DATE_ACTIONS

    override fun build(context: Context, size: SizeF, sample: Boolean): RemoteViews {
        val views = RemoteViews(context.packageName, R.layout.widget_progress)
        views.setOnClickPendingIntent(
            R.id.progress_root,
            activity(context, Intent(Intent.ACTION_VIEW, CalendarContract.CONTENT_URI.buildUpon().appendPath("time").build()), 84),
        )
        val now = if (sample) LocalDateTime.of(2026, 9, 30, 7, 38) else LocalDateTime.now()
        views.setTextViewText(
            R.id.progress_now,
            DateTimeFormatter.ofPattern(context.getString(R.string.progress_now_pattern), Locale.getDefault()).format(now),
        )
        val day = now.toLocalDate().atStartOfDay()
        val week = day.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        val month = day.withDayOfMonth(1)
        val year = day.withDayOfYear(1)
        val rows = listOf(
            Triple(R.id.progress_day_bar, R.id.progress_day_value, share(now, day, day.plusDays(1))),
            Triple(R.id.progress_week_bar, R.id.progress_week_value, share(now, week, week.plusWeeks(1))),
            Triple(R.id.progress_month_bar, R.id.progress_month_value, share(now, month, month.plusMonths(1))),
            Triple(R.id.progress_year_bar, R.id.progress_year_value, share(now, year, year.plusYears(1))),
        )
        for ((bar, value, part) in rows) {
            views.setProgressBar(bar, 1000, (part * 1000).roundToInt(), false)
            views.setTextViewText(value, context.getString(R.string.progress_percent, (part * 100).toInt()))
        }
        val length = now.toLocalDate().lengthOfYear()
        val left = ChronoUnit.DAYS.between(now.toLocalDate(), year.plusYears(1).toLocalDate()).toInt()
        views.setTextViewText(
            R.id.progress_footer,
            context.getString(R.string.progress_footer, now.year, now.dayOfYear, length, left),
        )
        // Sur une seule rangée : le jour et l'année seulement.
        val compact = size.height < 100f
        for (id in intArrayOf(R.id.progress_week, R.id.progress_month, R.id.progress_footer)) {
            views.setViewVisibility(id, if (compact) View.GONE else View.VISIBLE)
        }
        return views
    }

    override fun onRendered(context: Context) {
        // La barre du jour gagne 1 % toutes les 14 minutes et demie.
        val next = System.currentTimeMillis() + REFRESH.toMillis()
        context.getSystemService(AlarmManager::class.java)?.set(AlarmManager.RTC, next, refreshIntent(context, 1))
    }

    private companion object {
        val REFRESH: Duration = Duration.ofMinutes(15)

        /** La part (0–1) de [from] → [to] déjà écoulée à [now]. */
        fun share(now: LocalDateTime, from: LocalDateTime, to: LocalDateTime): Float {
            val total = Duration.between(from, to).seconds.toFloat()
            return (Duration.between(from, now).seconds / total).coerceIn(0f, 1f)
        }
    }
}
