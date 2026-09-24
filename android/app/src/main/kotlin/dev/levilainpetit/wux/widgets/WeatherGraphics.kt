package dev.levilainpetit.wux.widgets

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import dev.levilainpetit.wux.weather.CurvePoint
import kotlin.math.roundToInt

/**
 * Dessins du widget Météo, en `ALPHA_8` : blancs, teints par la mise en page
 * aux couleurs du téléphone. Les nuances passent par l'opacité.
 */
object WeatherGraphics {

    /** Règle graduée du minimum au maximum du jour, curseur à [current]. */
    fun gauge(min: Double, max: Double, current: Double, widthPx: Int, density: Float): Bitmap {
        val height = (14 * density).roundToInt()
        val bitmap = Bitmap.createBitmap(widthPx.coerceAtLeast(1), height, Bitmap.Config.ALPHA_8)
        val canvas = Canvas(bitmap)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { strokeWidth = 1.5f * density }
        val y = height - 3 * density
        canvas.drawLine(0f, y, widthPx.toFloat(), y, paint)
        for (i in 0..10) {
            val x = (widthPx - paint.strokeWidth) * i / 10f + paint.strokeWidth / 2
            canvas.drawLine(x, y - 3 * density, x, y + 2 * density, paint)
        }
        val span = (max - min).takeIf { it > 0 } ?: 1.0
        val x = (((current - min) / span).coerceIn(0.0, 1.0) * widthPx).toFloat()
        val marker = Path().apply {
            moveTo(x, y - 4 * density)
            lineTo(x - 5 * density, 0f)
            lineTo(x + 5 * density, 0f)
            close()
        }
        canvas.drawPath(marker, paint.apply { style = Paint.Style.FILL })
        return bitmap
    }

    /**
     * Les 24 heures en 12 barres, une toutes les deux heures : la hauteur suit
     * la température (écrite au-dessus), l'heure est écrite dessous, les
     * barres de nuit sont estompées, et une marque sous la base signale une
     * pluie probable (au moins 30 %), d'autant plus longue qu'elle est sûre.
     */
    fun bars(points: List<CurvePoint>, hourLabels: List<String>, widthPx: Int, heightPx: Int, density: Float): Bitmap {
        val bitmap = Bitmap.createBitmap(widthPx.coerceAtLeast(1), heightPx.coerceAtLeast(1), Bitmap.Config.ALPHA_8)
        if (points.isEmpty()) return bitmap
        val canvas = Canvas(bitmap)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { textAlign = Paint.Align.CENTER }
        val labelSize = 9 * density
        val top = labelSize + 3 * density
        val hoursBand = labelSize + 3 * density
        val rainBand = 5 * density
        val base = heightPx - hoursBand - rainBand
        val slot = widthPx / points.size.toFloat()
        val barWidth = slot * 0.55f
        val min = points.minOf { it.temperature }
        val max = points.maxOf { it.temperature }
        val span = (max - min).takeIf { it > 0.5 } ?: 1.0
        paint.textSize = labelSize
        points.forEachIndexed { i, point ->
            val cx = slot * i + slot / 2
            // Au moins un quart de hauteur, pour que la plus froide se voie.
            val ratio = 0.25 + 0.75 * (point.temperature - min) / span
            val barTop = (base - (base - top) * ratio).toFloat()
            paint.alpha = if (point.isDay) 255 else 110
            canvas.drawRoundRect(RectF(cx - barWidth / 2, barTop, cx + barWidth / 2, base), 2 * density, 2 * density, paint)
            paint.alpha = 255
            canvas.drawText("${point.temperature.roundToInt()}°", cx, barTop - 3 * density, paint)
            if (point.rainProbability >= 30) {
                val length = barWidth * point.rainProbability / 100f
                canvas.drawRect(RectF(cx - length / 2, base + 2 * density, cx + length / 2, base + 4 * density), paint)
            }
            paint.alpha = 200
            hourLabels.getOrNull(i)?.let { canvas.drawText(it, cx, heightPx - 2 * density, paint) }
        }
        return bitmap
    }
}
