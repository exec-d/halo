package dev.levilainpetit.wux.widgets

import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.text.format.DateFormat
import android.util.SizeF
import android.view.View
import android.widget.RemoteViews
import dev.levilainpetit.wux.MainActivity
import dev.levilainpetit.wux.R
import dev.levilainpetit.wux.sky.Moon
import dev.levilainpetit.wux.sky.MoonPainter
import dev.levilainpetit.wux.sky.Phase
import dev.levilainpetit.wux.weather.Weather
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale
import kotlin.math.roundToInt

/**
 * Lune : la Lune dessinée dans sa phase du moment, le nom de la phase, le
 * compte à rebours jusqu'à la pleine lune (ou la nouvelle), son lever et son
 * coucher au lieu de Météo, et les quatre phases principales à venir. Tout
 * est calculé sur le téléphone (sky/Moon.kt). Sans lieu, ni lever ni coucher.
 *
 * Le nom de la classe vient du widget Ciel de ce soir qu'il remplace : les
 * widgets déjà posés restent en place.
 */
class SkyWidget : NeonWidget() {

    override val refreshActions = DATE_ACTIONS

    override fun build(context: Context, size: SizeF, sample: Boolean): RemoteViews {
        val small = size.width < 220f
        val views = RemoteViews(context.packageName, if (small) R.layout.widget_sky_small else R.layout.widget_sky)
        views.setOnClickPendingIntent(R.id.sky_root, activity(context, Intent(context, MainActivity::class.java), 65))
        val place = if (sample) SampleData.place else Weather.place(context)
        val now = if (sample) ZonedDateTime.of(2026, 9, 30, 18, 0, 0, 0, ZoneId.of("Europe/Paris")) else ZonedDateTime.now()
        val day = Moon.day(now, place?.latitude, place?.longitude)
        val southern = (place?.latitude ?: 45.0) < 0
        val density = context.resources.displayMetrics.density

        val moonDp = if (small) minOf(size.width - 48, size.height - 60).coerceIn(40f, 120f) else 84f
        views.setImageViewBitmap(R.id.sky_moon, MoonPainter.draw(day.now.angle, (moonDp * density).roundToInt(), southern))
        views.setTextViewText(R.id.sky_phase, context.resources.getStringArray(R.array.moon_phases)[day.now.phase.ordinal])
        val percent = (day.now.fraction * 100).roundToInt()
        views.setTextViewText(
            R.id.sky_age,
            if (small) context.getString(R.string.moon_illumination, percent) else context.getString(R.string.moon_age, day.now.age.toInt(), percent),
        )
        if (small) return views

        views.setTextViewText(R.id.sky_countdown, countdown(context, now, day.now.phase, day.next))
        val format = DateTimeFormatter.ofPattern(if (DateFormat.is24HourFormat(context)) "HH:mm" else "h:mm a", Locale.getDefault())
        fun time(t: ZonedDateTime, from: ZonedDateTime) =
            if (t.toLocalDate().isAfter(from.toLocalDate())) context.getString(R.string.moon_tomorrow, format.format(t)) else format.format(t)
        val rise = day.rise
        val set = day.set
        val times = when {
            place == null -> context.getString(R.string.weather_pick_place)
            day.up && set != null -> context.getString(R.string.moon_up, time(set, now))
            day.up -> context.getString(R.string.moon_up_only)
            rise != null && set != null -> context.getString(R.string.moon_rise_set, time(rise, now), time(set, rise))
            rise != null -> context.getString(R.string.moon_rise_only, time(rise, now))
            else -> ""
        }
        views.setTextViewText(R.id.sky_times, times)

        val showNext = size.height >= 140f
        views.setViewVisibility(R.id.sky_next, if (showNext) View.VISIBLE else View.GONE)
        if (showNext) {
            val names = context.resources.getStringArray(R.array.moon_principal)
            val dates = DateTimeFormatter.ofPattern("EEE d MMM", Locale.getDefault())
            val icon = (18 * density).roundToInt()
            NEXT.forEachIndexed { i, (iconId, nameId, dateId) ->
                val (phase, at) = day.next.getOrNull(i) ?: return@forEachIndexed
                val index = PRINCIPALS.indexOf(phase)
                views.setImageViewBitmap(iconId, MoonPainter.draw(index * 90.0, icon, southern, outline = true))
                views.setTextViewText(nameId, names[index])
                views.setTextViewText(dateId, dates.format(at))
            }
        }
        return views
    }

    override fun onRendered(context: Context) {
        // Le lever, le coucher et la phase avancent : on redessine toutes les demi-heures.
        val next = System.currentTimeMillis() + 30 * 60_000L
        context.getSystemService(AlarmManager::class.java)?.set(AlarmManager.RTC, next, refreshIntent(context, 2))
    }

    /** « Pleine lune dans 26 jours » ; le jour de la pleine lune, la nouvelle lune. */
    private fun countdown(context: Context, now: ZonedDateTime, phase: Phase, next: List<Pair<Phase, ZonedDateTime>>): String {
        val target = if (phase == Phase.FULL) Phase.NEW else Phase.FULL
        val at = next.firstOrNull { it.first == target }?.second ?: return ""
        val days = ChronoUnit.DAYS.between(now.toLocalDate(), at.toLocalDate()).toInt()
        val full = target == Phase.FULL
        return when (days) {
            0 -> context.getString(if (full) R.string.moon_full_today else R.string.moon_new_today)
            1 -> context.getString(if (full) R.string.moon_full_tomorrow else R.string.moon_new_tomorrow)
            else -> context.getString(if (full) R.string.moon_full_in else R.string.moon_new_in, days)
        }
    }

    private companion object {
        /** Les phases principales, dans l'ordre de moon_principal (un quart de tour chacune). */
        val PRINCIPALS = listOf(Phase.NEW, Phase.FIRST_QUARTER, Phase.FULL, Phase.LAST_QUARTER)

        /** Chaque phase à venir : l'icône, le nom, la date. */
        val NEXT = listOf(
            Triple(R.id.next_icon_0, R.id.next_name_0, R.id.next_date_0),
            Triple(R.id.next_icon_1, R.id.next_name_1, R.id.next_date_1),
            Triple(R.id.next_icon_2, R.id.next_name_2, R.id.next_date_2),
            Triple(R.id.next_icon_3, R.id.next_name_3, R.id.next_date_3),
        )
    }
}
