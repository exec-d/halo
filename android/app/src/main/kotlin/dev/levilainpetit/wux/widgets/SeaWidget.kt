package dev.levilainpetit.wux.widgets

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.text.format.DateFormat
import android.util.SizeF
import android.view.View
import android.widget.RemoteViews
import dev.levilainpetit.wux.MainActivity
import dev.levilainpetit.wux.R
import dev.levilainpetit.wux.weather.Sea
import dev.levilainpetit.wux.weather.SeaState
import dev.levilainpetit.wux.weather.Weather
import dev.levilainpetit.wux.weather.WeatherRefresh
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.roundToInt

/**
 * Mer et vagues : au plus près du lieu de Météo, la hauteur, la période et
 * la direction des vagues, la température de l'eau, et les vagues des
 * 24 prochaines heures.
 */
class SeaWidget : NeonWidget() {

    override fun build(context: Context, size: SizeF, sample: Boolean): RemoteViews {
        val views = RemoteViews(context.packageName, R.layout.widget_sea)
        views.setOnClickPendingIntent(R.id.sea_root, activity(context, Intent(context, MainActivity::class.java), 64))
        val place = if (sample) SampleData.place else Weather.place(context)
        val sea = if (sample) SampleData.sea() else Sea.state(context)
        val city = place?.name?.substringBefore(',').orEmpty()
        views.setTextViewText(R.id.sea_place, context.getString(R.string.sea_title, city))
        val showChart = size.height >= 110f
        if (sea == null) {
            views.setTextViewText(R.id.sea_height, "--")
            views.setTextViewText(R.id.sea_water, "")
            views.setTextViewText(
                R.id.sea_detail,
                context.getString(
                    when {
                        place == null -> R.string.weather_pick_place
                        Sea.noSea(context) -> R.string.sea_none
                        else -> R.string.weather_loading
                    },
                ),
            )
            views.setViewVisibility(R.id.sea_chart, View.GONE)
            return views
        }
        views.setTextViewText(R.id.sea_height, context.getString(R.string.sea_height, String.format(Locale.getDefault(), "%.1f", sea.waveHeight)))
        views.setTextViewText(R.id.sea_water, sea.water?.let { "${it.roundToInt()}°" } ?: "—")
        val parts = listOfNotNull(
            sea.waveDirection?.let { context.getString(R.string.sea_swell, context.resources.getStringArray(R.array.cardinals)[sector(it)]) },
            sea.wavePeriod?.let { context.getString(R.string.sea_period, it.roundToInt()) },
            sea.distanceKm.takeIf { it >= 3 }?.let { context.getString(R.string.sea_distance, it) },
        )
        views.setTextViewText(R.id.sea_detail, parts.joinToString(" · "))
        views.setViewVisibility(R.id.sea_chart, if (showChart && sea.hours.size >= 2) View.VISIBLE else View.GONE)
        if (showChart && sea.hours.size >= 2) {
            val density = context.resources.displayMetrics.density
            val format = DateTimeFormatter.ofPattern(if (DateFormat.is24HourFormat(context)) "HH'h'" else "ha", Locale.getDefault())
            views.setImageViewBitmap(
                R.id.sea_chart,
                chart(sea, format, ((size.width - 24) * density).roundToInt(), (60 * density).roundToInt(), density),
            )
        }
        return views
    }

    override fun onSystemUpdate(context: Context, goAsync: () -> PendingResult) =
        WeatherRefresh.refreshIfStale(context, goAsync)

    override fun onRendered(context: Context) {
        WeatherRefresh.schedule(context)
        // Point de mer pas encore cherché : la tâche de fond s'en charge.
        if (Sea.wanted(context)) WeatherRefresh.now(context)
    }

    /** La courbe des vagues sur 24 h : aire, trait, maximum, heures sous l'axe (en blanc, teinté par la mise en page). */
    private fun chart(sea: SeaState, format: DateTimeFormatter, width: Int, height: Int, density: Float): Bitmap {
        val bitmap = Bitmap.createBitmap(width.coerceAtLeast(1), height.coerceAtLeast(1), Bitmap.Config.ALPHA_8)
        val canvas = Canvas(bitmap)
        val label = 9 * density
        val top = label + 4 * density
        val base = height - label - 5 * density
        val values = sea.hours.map { it.second }
        val max = (values.maxOrNull() ?: 1.0).coerceAtLeast(0.3)
        fun x(i: Int) = i * (width - 1f) / (values.size - 1)
        fun y(v: Double) = (base - (base - top) * v / max).toFloat()
        val line = Path().apply {
            values.forEachIndexed { i, v -> if (i == 0) moveTo(x(i), y(v)) else lineTo(x(i), y(v)) }
        }
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        paint.alpha = 40
        canvas.drawPath(Path(line).apply { lineTo(x(values.size - 1), base); lineTo(0f, base); close() }, paint)
        paint.alpha = 255
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1.6f * density
        paint.strokeJoin = Paint.Join.ROUND
        canvas.drawPath(line, paint)
        paint.strokeWidth = 1f * density
        paint.alpha = 90
        canvas.drawLine(0f, base, width.toFloat(), base, paint)
        paint.style = Paint.Style.FILL
        paint.alpha = 255
        canvas.drawCircle(x(0), y(values[0]), 3 * density, paint)
        // Le maximum, écrit au-dessus de son point.
        val peak = values.indices.maxByOrNull { values[it] } ?: 0
        paint.textSize = label
        paint.textAlign = Paint.Align.CENTER
        val peakX = x(peak).coerceIn(14 * density, width - 14 * density)
        canvas.drawText(String.format(Locale.getDefault(), "%.1f m", values[peak]), peakX, y(values[peak]) - 4 * density, paint)
        // Une heure toutes les six heures.
        paint.alpha = 200
        sea.hours.forEachIndexed { i, (time, _) ->
            if (i % 6 != 0) return@forEachIndexed
            paint.textAlign = when (i) {
                0 -> Paint.Align.LEFT
                values.size - 1 -> Paint.Align.RIGHT
                else -> Paint.Align.CENTER
            }
            canvas.drawText(format.format(time), x(i), height - 2 * density, paint)
        }
        return bitmap
    }

    private companion object {
        fun sector(degrees: Int) = (((degrees % 360) + 360) % 360 + 22) / 45 % 8
    }
}
