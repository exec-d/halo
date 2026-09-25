package dev.levilainpetit.wux.widgets

import android.content.Context
import android.content.Intent
import android.text.format.DateFormat
import android.util.SizeF
import android.view.View
import android.widget.RemoteViews
import dev.levilainpetit.wux.MainActivity
import dev.levilainpetit.wux.R
import dev.levilainpetit.wux.weather.Forecast
import dev.levilainpetit.wux.weather.Weather
import dev.levilainpetit.wux.weather.WeatherRefresh
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.roundToInt

/**
 * Météo façon tableau de bord : la température et sa jauge du jour, des
 * relevés en colonne, puis la courbe des 24 heures avec la pluie et la nuit.
 * Sur une rangée, une seule ligne ; sur deux, sans la courbe.
 */
class WeatherWidget : NeonWidget() {

    override fun build(context: Context, size: SizeF, sample: Boolean): RemoteViews {
        val compact = size.height < 100f
        val views = RemoteViews(
            context.packageName,
            if (compact) R.layout.widget_weather_compact else R.layout.widget_weather,
        )
        views.setOnClickPendingIntent(R.id.weather_root, activity(context, Intent(context, MainActivity::class.java), 60))
        val place = if (sample) SampleData.place else Weather.place(context)
        val forecast = if (sample) SampleData.forecast() else Weather.forecast(context)
        if (place == null || forecast == null) {
            views.setTextViewText(R.id.weather_temperature, "--°")
            views.setTextViewText(
                R.id.weather_condition,
                context.getString(if (place == null) R.string.weather_pick_place else R.string.weather_loading),
            )
            views.setTextViewText(R.id.weather_place, place?.name?.substringBefore(',').orEmpty())
            if (!compact) {
                views.setViewVisibility(R.id.weather_readouts, View.GONE)
                views.setViewVisibility(R.id.weather_curve_block, View.GONE)
                views.setViewVisibility(R.id.weather_gauge_block, View.GONE)
            }
            return views
        }
        val today = forecast.days.firstOrNull()
        val condition = context.getString(Weather.label(forecast.code))
        views.setTextViewText(R.id.weather_temperature, degrees(forecast.temperature))
        views.setTextViewText(R.id.weather_condition, condition.uppercase(Locale.getDefault()))
        views.setTextViewText(
            R.id.weather_place,
            context.getString(R.string.weather_now_at, place.name.substringBefore(',')),
        )
        if (compact) {
            views.setTextViewText(
                R.id.weather_line,
                listOfNotNull(
                    today?.let { "${degrees(it.min)} / ${degrees(it.max)}" },
                    Weather.rainSummary(context, forecast),
                ).joinToString(" · "),
            )
            return views
        }

        val density = context.resources.displayMetrics.density
        if (today != null) {
            val gaugeWidth = ((size.width * 0.42f - 24) * density).roundToInt()
            views.setImageViewBitmap(
                R.id.weather_gauge,
                WeatherGraphics.gauge(today.min, today.max, forecast.temperature, gaugeWidth, density),
            )
            views.setTextViewText(R.id.weather_min, degrees(today.min))
            views.setTextViewText(R.id.weather_max, degrees(today.max))
        } else {
            views.setViewVisibility(R.id.weather_gauge_block, View.GONE)
        }

        views.removeAllViews(R.id.weather_readouts)
        readouts(context, forecast).forEach { (label, value) ->
            views.addView(
                R.id.weather_readouts,
                RemoteViews(context.packageName, R.layout.weather_readout).apply {
                    setTextViewText(R.id.weather_readout_label, label)
                    setTextViewText(R.id.weather_readout_value, value)
                },
            )
        }

        val showCurve = size.height >= 210f && forecast.curve.size >= 2
        views.setViewVisibility(R.id.weather_curve_block, if (showCurve) View.VISIBLE else View.GONE)
        if (showCurve) {
            // Une barre toutes les deux heures, sur 24 heures.
            val points = forecast.curve.filterIndexed { i, _ -> i % 2 == 0 }
            val format = DateTimeFormatter.ofPattern(if (DateFormat.is24HourFormat(context)) "HH'h'" else "ha", Locale.getDefault())
            // Le graphique prend toute la hauteur sous le bloc du haut, pour ne
            // pas laisser de vide en bas. S'il est un peu mal estimé, l'image
            // est mise à l'échelle sans déformation.
            val fontScale = context.resources.configuration.fontScale
            val chartHeight = ((size.height - TOP_BLOCK * fontScale).coerceAtLeast(48f) * density).roundToInt()
            views.setImageViewBitmap(
                R.id.weather_curve,
                WeatherGraphics.bars(
                    points,
                    points.map { format.format(it.time) },
                    ((size.width - 24) * density).roundToInt(),
                    chartHeight,
                    density,
                ),
            )
        }
        return views
    }

    /** Les relevés : ressenti, vent, humidité, UV, pluie. */
    private fun readouts(context: Context, forecast: Forecast): List<Pair<String, String>> {
        val uv = forecast.uv?.let { value ->
            val levels = context.resources.getStringArray(R.array.uv_levels)
            val level = when {
                value < 3 -> 0
                value < 6 -> 1
                value < 8 -> 2
                value < 11 -> 3
                else -> 4
            }
            "${value.roundToInt()} · ${levels[level]}"
        }
        return listOfNotNull(
            context.getString(R.string.readout_feels) to degrees(forecast.apparent),
            context.getString(R.string.readout_wind) to (
                "${forecast.wind.roundToInt()} km/h" +
                    forecast.windDirection.let { if (it == null) "" else " ${arrow(it)} ${cardinal(context, it)}" }
                ),
            context.getString(R.string.readout_humidity) to (forecast.humidity?.let { "$it %" } ?: "—"),
            uv?.let { context.getString(R.string.readout_uv) to it },
            context.getString(R.string.readout_rain) to Weather.rainSummary(context, forecast),
        )
    }

    override fun onSystemUpdate(context: Context, goAsync: () -> PendingResult) =
        WeatherRefresh.refreshIfStale(context, goAsync)

    override fun onRendered(context: Context) {
        WeatherRefresh.schedule(context)
        if (Weather.forecast(context) == null) WeatherRefresh.now(context)
    }

    companion object {
        /**
         * Hauteur (dp, texte à 100 %) du bloc du haut, marges du widget et du
         * graphique comprises : suivre widget_weather.
         */
        private const val TOP_BLOCK = 172f

        fun degrees(value: Double) = "${value.roundToInt()}°"

        private fun sector(degrees: Int) = (((degrees % 360) + 360) % 360 + 22) / 45 % 8

        /** Le vent vient de [degrees] : la flèche montre où il va. */
        private fun arrow(degrees: Int) = arrayOf("↓", "↙", "←", "↖", "↑", "↗", "→", "↘")[sector(degrees)]

        private fun cardinal(context: Context, degrees: Int) =
            context.resources.getStringArray(R.array.cardinals)[sector(degrees)]
    }
}
