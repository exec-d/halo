package dev.levilainpetit.wux.widgets

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
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
 * Pluie : si elle arrive et quand, puis un histogramme des probabilités des
 * 12 prochaines heures (dessiné en blanc, teint par la mise en page).
 */
class RainWidget : NeonWidget() {

    override fun build(context: Context, size: SizeF, sample: Boolean): RemoteViews {
        val views = RemoteViews(context.packageName, R.layout.widget_rain)
        views.setOnClickPendingIntent(R.id.rain_root, activity(context, Intent(context, MainActivity::class.java), 62))
        val forecast = if (sample) SampleData.forecast() else Weather.forecast(context)
        val place = if (sample) SampleData.place else Weather.place(context)
        val showChart = size.height >= 100f
        views.setViewVisibility(R.id.rain_chart, if (showChart) View.VISIBLE else View.GONE)
        views.setViewVisibility(R.id.rain_axis, if (showChart) View.VISIBLE else View.GONE)
        if (forecast == null || forecast.rain.isEmpty()) {
            views.setTextViewText(R.id.rain_summary, "--")
            views.setTextViewText(
                R.id.rain_detail,
                context.getString(if (place == null) R.string.weather_pick_place else R.string.weather_loading),
            )
            views.setViewVisibility(R.id.rain_chart, View.GONE)
            views.setViewVisibility(R.id.rain_axis, View.GONE)
            return views
        }
        views.setTextViewText(R.id.rain_summary, Weather.rainSummary(context, forecast))
        val total = forecast.rain.sumOf { it.millimeters }
        val peak = forecast.rain.maxOf { it.probability }
        views.setTextViewText(
            R.id.rain_detail,
            context.getString(R.string.rain_detail, String.format(Locale.getDefault(), "%.1f", total), peak),
        )
        if (showChart) {
            val density = context.resources.displayMetrics.density
            views.setImageViewBitmap(
                R.id.rain_chart,
                chart(forecast.rain.map { it.probability }, (size.width * density).roundToInt(), (48 * density).roundToInt(), density),
            )
            val format = DateTimeFormatter.ofPattern(if (DateFormat.is24HourFormat(context)) "HH'h'" else "h a", Locale.getDefault())
            val hours = forecast.rain
            views.setTextViewText(R.id.rain_axis_start, format.format(hours.first().time))
            views.setTextViewText(R.id.rain_axis_middle, format.format(hours[hours.size / 2].time))
            views.setTextViewText(R.id.rain_axis_end, format.format(hours.last().time))
        }
        return views
    }

    override fun onRendered(context: Context) {
        WeatherRefresh.schedule(context)
    }

    /** Histogramme : une barre par heure, hauteur = probabilité, base visible. */
    private fun chart(probabilities: List<Int>, width: Int, height: Int, density: Float): Bitmap {
        val bitmap = Bitmap.createBitmap(width.coerceAtLeast(1), height.coerceAtLeast(1), Bitmap.Config.ALPHA_8)
        val canvas = Canvas(bitmap)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        val slot = width / probabilities.size.toFloat()
        val gap = slot * 0.25f
        val base = 2 * density
        probabilities.forEachIndexed { i, p ->
            val top = height - base - (height - base) * p / 100f
            paint.alpha = 255
            canvas.drawRoundRect(
                RectF(i * slot + gap / 2, top, (i + 1) * slot - gap / 2, height.toFloat()),
                2 * density,
                2 * density,
                paint,
            )
        }
        return bitmap
    }
}
