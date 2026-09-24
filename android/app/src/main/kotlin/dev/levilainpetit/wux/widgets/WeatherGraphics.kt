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
     * Les 24 heures : la nuit en hachures au pied, la pluie en barres
     * estompées, la température en courbe, un point sur « maintenant » et la
     * valeur la plus haute écrite au-dessus de son sommet.
     */
    fun curve(points: List<CurvePoint>, widthPx: Int, heightPx: Int, density: Float): Bitmap {
        val bitmap = Bitmap.createBitmap(widthPx.coerceAtLeast(1), heightPx.coerceAtLeast(1), Bitmap.Config.ALPHA_8)
        if (points.size < 2) return bitmap
        val canvas = Canvas(bitmap)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        val pad = 6 * density
        val labelSpace = 14 * density
        val bottom = heightPx - 2 * density
        val top = labelSpace
        val left = pad
        val right = widthPx - pad
        val step = (right - left) / (points.size - 1)
        fun x(i: Int) = left + step * i

        // Nuit : hachures au pied du graphique.
        paint.alpha = 110
        paint.strokeWidth = density
        points.forEachIndexed { i, point ->
            if (point.isDay) return@forEachIndexed
            var hx = x(i) - step / 2
            while (hx < x(i) + step / 2) {
                canvas.drawLine(hx, bottom, hx, bottom - 4 * density, paint)
                hx += 3 * density
            }
        }
        // Pluie : barres estompées, jusqu'à 60 % de la hauteur.
        paint.alpha = 120
        points.forEachIndexed { i, point ->
            if (point.rainProbability <= 0) return@forEachIndexed
            val h = (bottom - top) * 0.6f * point.rainProbability / 100f
            canvas.drawRect(RectF(x(i) - step * 0.3f, bottom - h, x(i) + step * 0.3f, bottom), paint)
        }
        // Base.
        paint.alpha = 255
        canvas.drawLine(left, bottom, right, bottom, paint)

        // Température.
        val min = points.minOf { it.temperature }
        val max = points.maxOf { it.temperature }
        val span = (max - min).takeIf { it > 0.5 } ?: 1.0
        fun y(t: Double) = (bottom - 6 * density - (bottom - 6 * density - top) * ((t - min) / span)).toFloat()
        val path = Path()
        points.forEachIndexed { i, p -> if (i == 0) path.moveTo(x(i), y(p.temperature)) else path.lineTo(x(i), y(p.temperature)) }
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 2 * density
        paint.strokeJoin = Paint.Join.ROUND
        canvas.drawPath(path, paint)

        paint.style = Paint.Style.FILL
        canvas.drawCircle(x(0), y(points[0].temperature), 4 * density, paint)
        val peak = points.indices.maxByOrNull { points[it].temperature } ?: 0
        paint.textSize = 10 * density
        paint.textAlign = Paint.Align.CENTER
        val label = "${points[peak].temperature.roundToInt()}°"
        val lx = x(peak).coerceIn(left + 10 * density, right - 10 * density)
        canvas.drawText(label, lx, (y(points[peak].temperature) - 5 * density).coerceAtLeast(10 * density), paint)
        return bitmap
    }
}
