package dev.levilainpetit.wux.widgets

import android.content.Context
import android.content.Intent
import android.text.format.DateFormat
import android.util.SizeF
import android.widget.RemoteViews
import dev.levilainpetit.wux.MainActivity
import dev.levilainpetit.wux.R
import dev.levilainpetit.wux.sky.Moon
import dev.levilainpetit.wux.weather.Weather
import dev.levilainpetit.wux.weather.WeatherRefresh
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.roundToInt

/**
 * Soleil et Lune : lever et coucher du soleil au lieu de la météo, durée du
 * jour, et phase de la lune (calculée sur le téléphone, sans réseau, par sky/Moon.kt).
 */
class SunMoonWidget : NeonWidget() {

    override val refreshActions = DATE_ACTIONS

    override fun build(context: Context, size: SizeF, sample: Boolean): RemoteViews {
        val views = RemoteViews(context.packageName, R.layout.widget_sun_moon)
        views.setOnClickPendingIntent(R.id.sun_moon_root, activity(context, Intent(context, MainActivity::class.java), 61))
        val forecast = if (sample) SampleData.forecast() else Weather.forecast(context)
        val today = forecast?.days?.firstOrNull { it.date == LocalDate.now() }
        val format = DateTimeFormatter.ofPattern(if (DateFormat.is24HourFormat(context)) "HH:mm" else "h:mm a", Locale.getDefault())
        val sunrise = today?.sunrise
        val sunset = today?.sunset
        views.setTextViewText(R.id.sunrise_time, sunrise?.let(format::format) ?: "--:--")
        views.setTextViewText(R.id.sunset_time, sunset?.let(format::format) ?: "--:--")
        views.setTextViewText(
            R.id.day_length,
            if (sunrise != null && sunset != null) {
                val length = Duration.between(sunrise, sunset)
                context.getString(R.string.sun_day_length, length.toHours(), length.toMinutes() % 60)
            } else {
                context.getString(R.string.sun_pick_place)
            },
        )

        val (phase, illumination) = moon(Instant.now())
        views.setImageViewResource(R.id.moon_icon, MOON_ICONS[phase])
        views.setTextViewText(R.id.moon_phase, context.resources.getStringArray(R.array.moon_phases)[phase])
        views.setTextViewText(R.id.moon_light, context.getString(R.string.moon_illumination, (illumination * 100).roundToInt()))
        return views
    }

    override fun onSystemUpdate(context: Context, goAsync: () -> PendingResult) =
        WeatherRefresh.refreshIfStale(context, goAsync)

    override fun onRendered(context: Context) {
        refreshAfterMidnight(context)
        WeatherRefresh.schedule(context)
    }

    companion object {
        private val MOON_ICONS = intArrayOf(
            R.drawable.moon_0, R.drawable.moon_1, R.drawable.moon_2, R.drawable.moon_3,
            R.drawable.moon_4, R.drawable.moon_5, R.drawable.moon_6, R.drawable.moon_7,
        )

        /** Phase (0 à 7, 0 : nouvelle lune) et fraction éclairée (0 à 1), comme le widget Lune. */
        fun moon(at: Instant): Pair<Int, Double> = Moon.at(at).let { it.phase.ordinal to it.fraction }
    }
}
