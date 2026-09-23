package dev.levilainpetit.wux.widgets

import android.content.Context
import android.content.Intent
import android.text.format.DateFormat
import android.util.SizeF
import android.view.View
import android.widget.RemoteViews
import dev.levilainpetit.wux.MainActivity
import dev.levilainpetit.wux.R
import dev.levilainpetit.wux.weather.Weather
import dev.levilainpetit.wux.weather.WeatherRefresh
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.roundToInt

/**
 * Météo : conditions actuelles, et, s'il y a la place, les six prochaines
 * heures. Toucher le widget ouvre ses réglages dans WUX.
 */
class WeatherWidget : NeonWidget() {

    override fun build(context: Context, size: SizeF, sample: Boolean): RemoteViews {
        val views = RemoteViews(context.packageName, R.layout.widget_weather)
        views.setOnClickPendingIntent(R.id.weather_root, activity(context, Intent(context, MainActivity::class.java), 60))
        val place = if (sample) SampleData.place else Weather.place(context)
        val forecast = if (sample) SampleData.forecast() else Weather.forecast(context)
        if (place == null || forecast == null) {
            views.setImageViewResource(R.id.weather_icon, R.drawable.icon_cloud)
            views.setTextViewText(R.id.weather_temperature, "--°")
            views.setTextViewText(
                R.id.weather_condition,
                context.getString(if (place == null) R.string.weather_pick_place else R.string.weather_loading),
            )
            views.setTextViewText(R.id.weather_place, place?.name.orEmpty())
            views.setTextViewText(R.id.weather_detail, "")
            views.setTextViewText(R.id.weather_range, "")
            views.setViewVisibility(R.id.weather_hours, View.GONE)
            return views
        }
        val today = forecast.days.firstOrNull()
        views.setImageViewResource(R.id.weather_icon, Weather.icon(forecast.code, forecast.isDay))
        views.setTextViewText(R.id.weather_temperature, degrees(forecast.temperature))
        views.setTextViewText(R.id.weather_condition, context.getString(Weather.label(forecast.code)))
        views.setTextViewText(R.id.weather_place, place.name.substringBefore(','))
        views.setTextViewText(
            R.id.weather_detail,
            context.getString(R.string.weather_detail, degrees(forecast.apparent), forecast.wind.roundToInt()),
        )
        views.setTextViewText(
            R.id.weather_range,
            today?.let { "${degrees(it.max)} / ${degrees(it.min)}" }.orEmpty(),
        )

        // Les heures seulement si le widget a plus d'une rangée.
        val showHours = size.height >= 100f && forecast.hours.isNotEmpty()
        views.setViewVisibility(R.id.weather_hours, if (showHours) View.VISIBLE else View.GONE)
        views.removeAllViews(R.id.weather_hours)
        if (showHours) {
            val pattern = if (DateFormat.is24HourFormat(context)) "HH'h'" else "h a"
            val format = DateTimeFormatter.ofPattern(pattern, Locale.getDefault())
            forecast.hours.forEach { hour ->
                views.addView(
                    R.id.weather_hours,
                    RemoteViews(context.packageName, R.layout.weather_hour).apply {
                        setTextViewText(R.id.weather_hour_time, format.format(hour.time))
                        setImageViewResource(R.id.weather_hour_icon, Weather.icon(hour.code, hour.isDay))
                        setTextViewText(R.id.weather_hour_temperature, degrees(hour.temperature))
                    },
                )
            }
        }
        return views
    }

    override fun onRendered(context: Context) {
        WeatherRefresh.schedule(context)
        if (Weather.forecast(context) == null) WeatherRefresh.now(context)
    }

    companion object {
        fun degrees(value: Double) = "${value.roundToInt()}°"
    }
}
