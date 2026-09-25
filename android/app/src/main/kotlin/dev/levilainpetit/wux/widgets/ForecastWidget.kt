package dev.levilainpetit.wux.widgets

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.util.SizeF
import android.widget.RemoteViews
import dev.levilainpetit.wux.MainActivity
import dev.levilainpetit.wux.R
import dev.levilainpetit.wux.weather.Weather
import dev.levilainpetit.wux.weather.WeatherRefresh
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.max
import kotlin.math.roundToInt

/**
 * Prévisions sur 5 jours : pour chacun, l'icône, le maximum et le minimum,
 * et une capsule qui place l'écart du jour sur l'échelle de toute la
 * période — on voit d'un coup d'œil les jours plus chauds ou plus frais.
 */
class ForecastWidget : NeonWidget() {

    override val refreshActions = DATE_ACTIONS

    override fun build(context: Context, size: SizeF, sample: Boolean): RemoteViews {
        val views = RemoteViews(context.packageName, R.layout.widget_forecast)
        views.setOnClickPendingIntent(R.id.forecast_root, activity(context, Intent(context, MainActivity::class.java), 81))
        views.removeAllViews(R.id.forecast_row)
        val forecast = if (sample) SampleData.forecast() else Weather.forecast(context)
        val place = if (sample) "Lyon" else Weather.place(context)?.name
        views.setTextViewText(R.id.forecast_place, place?.let { context.getString(R.string.forecast_title, it) } ?: context.getString(R.string.sun_pick_place))
        val today = LocalDate.now()
        val days = forecast?.days.orEmpty().filter { !it.date.isBefore(today) }
        // Aussi étroit, moins de jours tiennent.
        val count = (size.width / 60f).toInt().coerceIn(3, 5)
        val shown = days.take(count)
        if (shown.isEmpty()) return views

        val low = shown.minOf { it.min }
        val high = shown.maxOf { it.max }
        val d = context.resources.displayMetrics.density
        val rangeHeight = max(18f, size.height - 16 - 104 * context.resources.configuration.fontScale)
        val name = DateTimeFormatter.ofPattern("EEE", Locale.getDefault())
        shown.forEach { day ->
            val column = RemoteViews(context.packageName, R.layout.forecast_day)
            column.setTextViewText(
                R.id.forecast_day_name,
                if (day.date == today) context.getString(R.string.forecast_today) else name.format(day.date).trimEnd('.'),
            )
            column.setImageViewResource(R.id.forecast_day_icon, Weather.icon(day.code, true))
            column.setTextViewText(R.id.forecast_day_max, WeatherWidget.degrees(day.max))
            column.setTextViewText(R.id.forecast_day_min, WeatherWidget.degrees(day.min))
            column.setImageViewBitmap(
                R.id.forecast_day_range,
                range(day.min, day.max, low, high, (8 * d).roundToInt(), (rangeHeight * d).roundToInt(), d),
            )
            views.addView(R.id.forecast_row, column)
        }
        return views
    }

    override fun onSystemUpdate(context: Context, goAsync: () -> PendingResult) =
        WeatherRefresh.refreshIfStale(context, goAsync)

    override fun onRendered(context: Context) {
        refreshAfterMidnight(context)
        WeatherRefresh.schedule(context)
    }

    /** Une piste verticale de [low] à [high], allumée de [min] à [max]. */
    private fun range(min: Double, max: Double, low: Double, high: Double, width: Int, height: Int, density: Float): Bitmap {
        val bitmap = Bitmap.createBitmap(width.coerceAtLeast(1), height.coerceAtLeast(1), Bitmap.Config.ALPHA_8)
        val canvas = Canvas(bitmap)
        val radius = width / 2f
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { alpha = 50 }
        canvas.drawRoundRect(RectF(0f, 0f, width.toFloat(), height.toFloat()), radius, radius, paint)
        val span = (high - low).takeIf { it > 0.5 } ?: 1.0
        fun y(t: Double) = (height - (t - low) / span * (height - 2 * radius) - radius).toFloat()
        paint.alpha = 255
        canvas.drawRoundRect(RectF(0f, y(max) - radius, width.toFloat(), y(min) + radius), radius, radius, paint)
        return bitmap
    }
}
