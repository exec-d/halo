package dev.levilainpetit.wux.widgets

import android.content.Context
import android.content.Intent
import android.util.SizeF
import android.widget.RemoteViews
import dev.levilainpetit.wux.MainActivity
import dev.levilainpetit.wux.R
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale

/**
 * Compte à rebours : les jours jusqu'à une date. Réglages `countdown.title`
 * et `countdown.date` (AAAA-MM-JJ). Redessiné chaque jour après minuit.
 */
class CountdownWidget : NeonWidget() {

    override val refreshActions = DATE_ACTIONS

    override fun build(context: Context, size: SizeF, sample: Boolean): RemoteViews {
        val prefs = settings(context)
        var title = prefs.getString("countdown.title", null)?.ifBlank { null }
            ?: context.getString(R.string.countdown_default_title)
        var target = prefs.getString("countdown.date", null)?.let { runCatching { LocalDate.parse(it) }.getOrNull() }
        if (sample) {
            title = context.getString(R.string.sample_countdown)
            target = LocalDate.now().plusDays(42)
        }
        val views = RemoteViews(context.packageName, R.layout.widget_countdown)
        views.setTextViewText(R.id.countdown_title, title)
        if (target == null) {
            views.setTextViewText(R.id.countdown_days, "—")
            views.setTextViewText(R.id.countdown_unit, context.getString(R.string.countdown_pick_date))
            views.setTextViewText(R.id.countdown_date, "")
        } else {
            val days = ChronoUnit.DAYS.between(LocalDate.now(), target)
            views.setTextViewText(R.id.countdown_days, "${kotlin.math.abs(days)}")
            views.setTextViewText(
                R.id.countdown_unit,
                context.resources.getQuantityString(
                    if (days >= 0) R.plurals.countdown_days_left else R.plurals.countdown_days_ago,
                    kotlin.math.abs(days).toInt(),
                ),
            )
            views.setTextViewText(
                R.id.countdown_date,
                DateTimeFormatter.ofPattern("EEEE d MMMM yyyy", Locale.getDefault()).format(target),
            )
        }
        views.setOnClickPendingIntent(R.id.countdown_root, activity(context, Intent(context, MainActivity::class.java), 31))
        return views
    }

    override fun onRendered(context: Context) = refreshAfterMidnight(context)
}
