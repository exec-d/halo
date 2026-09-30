package dev.levilainpetit.wux.sky

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import java.time.Duration
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit

/**
 * La frise de la soirée : une ligne par astre, l'heure en abscisse. Chaque
 * ligne montre quand l'astre est visible et à quelle hauteur (la courbe
 * monte avec lui, son sommet est pointé). Dessinée en blanc sur un calque
 * alpha, teintée par la mise en page.
 */
object SkyChart {

    fun draw(
        night: Night,
        latitude: Double,
        longitude: Double,
        sightings: List<Sighting>,
        name: (Body) -> String,
        hour: (ZonedDateTime) -> String,
        now: ZonedDateTime,
        width: Int,
        height: Int,
        density: Float,
    ): Bitmap {
        val bitmap = Bitmap.createBitmap(width.coerceAtLeast(1), height.coerceAtLeast(1), Bitmap.Config.ALPHA_8)
        if (sightings.isEmpty()) return bitmap
        val canvas = Canvas(bitmap)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        val text = 9f * density
        paint.textSize = text
        val axis = height - text - 3 * density
        val lane = axis / sightings.size
        val labels = sightings.map { name(it.body) }
        val left = (labels.maxOf { paint.measureText(it) } + 8 * density).coerceAtMost(width * 0.4f)
        val right = width - 2 * density
        val span = Duration.between(night.from, night.until).toMinutes().coerceAtLeast(1).toFloat()
        fun x(t: ZonedDateTime) = left + (right - left) * (Duration.between(night.from, t).toMinutes() / span).coerceIn(0f, 1f)
        // Toutes les courbes à la même échelle : plus haute, plus facile à voir.
        val ceiling = maxOf(40.0, sightings.maxOf { it.highest })

        // Les heures pleines : un trait sur toute la hauteur, l'heure en dessous.
        var tick = night.from.truncatedTo(ChronoUnit.HOURS).plusHours(1)
        val every = if ((right - left) / (span / 60f) < 26 * density) 2L else 1L
        if (every == 2L && tick.hour % 2 == 1) tick = tick.plusHours(1)
        paint.textAlign = Paint.Align.CENTER
        paint.strokeWidth = 1f * density
        var lastLabel = -Float.MAX_VALUE
        while (tick.isBefore(night.until)) {
            val px = x(tick)
            paint.alpha = 60
            canvas.drawLine(px, 0f, px, axis, paint)
            val label = hour(tick)
            val half = paint.measureText(label) / 2
            if (px - half > lastLabel + 3 * density && px + half <= width) {
                paint.alpha = 190
                canvas.drawText(label, px, height - 1.5f * density, paint)
                lastLabel = px + half
            }
            tick = tick.plusHours(every)
        }

        sightings.forEachIndexed { i, s ->
            val bottom = lane * (i + 1) - 2 * density
            val top = lane * i + 2 * density
            // L'horizon de la ligne, sur toute la soirée.
            paint.style = Paint.Style.FILL
            paint.alpha = 80
            paint.strokeWidth = 1f * density
            canvas.drawLine(left, bottom, right, bottom, paint)
            // La courbe de hauteur, pendant la visibilité.
            val outline = Path()
            var peak = s.from
            var peakAlt = -90.0
            var t = s.from
            while (true) {
                val alt = Sky.horizontal(s.body, t.toInstant(), latitude, longitude).altitude
                val px = x(t)
                val py = (bottom - (bottom - top) * (alt.coerceIn(0.0, ceiling) / ceiling)).toFloat()
                if (t == s.from) outline.moveTo(px, py) else outline.lineTo(px, py)
                if (alt > peakAlt) {
                    peakAlt = alt
                    peak = t
                }
                if (!t.isBefore(s.until)) break
                t = minOf(t.plusMinutes(5), s.until)
            }
            val area = Path(outline).apply {
                lineTo(x(s.until), bottom)
                lineTo(x(s.from), bottom)
                close()
            }
            paint.alpha = 70
            canvas.drawPath(area, paint)
            paint.style = Paint.Style.STROKE
            paint.alpha = 255
            paint.strokeWidth = 1.6f * density
            paint.strokeJoin = Paint.Join.ROUND
            canvas.drawPath(outline, paint)
            // Le sommet : l'astre lui-même, là où il est le plus haut.
            paint.style = Paint.Style.FILL
            val py = (bottom - (bottom - top) * (peakAlt.coerceIn(0.0, ceiling) / ceiling)).toFloat()
            canvas.drawCircle(x(peak), py, (if (s.body == Body.MOON) 3.2f else 2.4f) * density, paint)
            // Le nom, à gauche de sa ligne.
            paint.textAlign = Paint.Align.LEFT
            paint.alpha = 230
            canvas.drawText(labels[i], 0f, (top + bottom) / 2 + text * 0.35f, paint)
        }

        // Maintenant, si la soirée a commencé.
        if (now.isAfter(night.from) && now.isBefore(night.until)) {
            val px = x(now)
            paint.alpha = 255
            paint.strokeWidth = 1f * density
            var y = 0f
            while (y < axis) {
                canvas.drawLine(px, y, px, minOf(y + 2 * density, axis), paint)
                y += 4 * density
            }
        }
        return bitmap
    }
}
