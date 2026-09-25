package dev.levilainpetit.wux.wallpaper.blueprint

import android.graphics.Paint
import java.time.LocalDateTime
import kotlin.math.sin

/**
 * Retour vers le futur — le convecteur temporel, et les circuits temporels
 * de la DeLorean. Les impulsions courent vers le centre du Y ; la charge
 * monte jusqu'à 1,21 gigowatt ; l'heure présente est la vraie.
 */
class FluxCapacitor : Subject {
    override val title = "FLUX CAPACITOR"
    override val reference = "BACK TO THE FUTURE (1985) · DMC-12"
    override val scale = "1:3"
    override val sheet = 5
    override val frame = 50L

    override fun draw(pen: Pen, width: Float, top: Float, bottom: Float, time: Float, tiltX: Float, tiltY: Float) {
        val d = pen.density
        val zone = bottom - top
        val shift = tiltX * 8f * d
        pen.fit(1500f, 1900f, 24 * d + shift, top, width - 24 * d + shift, top + zone * 0.64f)
        capacitor(pen, time)
        pen.fit(2400f, 1000f, 24 * d - shift, top + zone * 0.68f, width - 24 * d - shift, bottom)
        circuits(pen, time)
    }

    private fun capacitor(pen: Pen, time: Float) {
        // Le boîtier et sa vitre.
        pen.rect(100f, 100f, 1400f, 1700f, Weight.THICK)
        pen.rect(200f, 200f, 1300f, 1450f, Weight.MAIN)
        pen.rect(220f, 220f, 1280f, 1430f, Weight.HAIR)
        pen.rect(400f, 1500f, 1100f, 1640f, Weight.THIN)
        pen.text(750f, 1590f, "FLUX CAPACITOR", 7f, Paint.Align.CENTER, bold = true)
        val cx = 750f
        val cy = 800f
        val arms = listOf(floatArrayOf(330f, 330f), floatArrayOf(1170f, 330f), floatArrayOf(750f, 1330f))
        // Les trois tubes, leurs électrodes, et les impulsions qui courent.
        val charge = (time % 6f) / 6f
        for ((i, end) in arms.withIndex()) {
            val ex = end[0]
            val ey = end[1]
            val len = kotlin.math.hypot(ex - cx, ey - cy)
            val nx = -(ey - cy) / len * 38f
            val ny = (ex - cx) / len * 38f
            pen.line(cx + nx, cy + ny, ex + nx, ey + ny, Weight.MAIN)
            pen.line(cx - nx, cy - ny, ex - nx, ey - ny, Weight.MAIN)
            pen.circle(ex, ey, 70f, Weight.MAIN)
            for (k in 1..3) {
                val t = k / 4f
                pen.circle(cx + (ex - cx) * t, cy + (ey - cy) * t, 30f, Weight.THIN)
            }
            pen.glowLine(ex, ey, cx, cy, 0.25f + 0.3f * charge, 1f)
            for (p in 0 until 3) {
                val t = 1f - ((time * 1.4f + p / 3f + i * 0.11f) % 1f)
                pen.glowDot(cx + (ex - cx) * t, cy + (ey - cy) * t, 16f, 0.9f)
            }
        }
        pen.circle(cx, cy, 90f, Weight.THICK)
        pen.glowDot(cx, cy, 40f + 30f * charge, 0.6f + 0.4f * charge)
        pen.axis(cx, 150f, cx, 1500f)
        pen.text(750f, 280f, "DISCONNECT CAPACITOR DRIVE BEFORE OPENING", 5.5f, Paint.Align.CENTER, alpha = 160)
        pen.dim(100f, 1700f, 1400f, 1700f, -110f, "380")
        pen.dim(1400f, 1700f, 1400f, 100f, -60f, "470")
        pen.callout(330f, 330f, 60f, 60f, 1)
        pen.callout(cx + 60f, cy + 60f, 1250f, 1100f, 2)
        pen.text(100f, 1860f, "FRONT VIEW · 1 ELECTRODE  2 CORE", 7f, bold = true)
    }

    /** Les circuits temporels : destination, présent (l'heure vraie), départ. */
    private fun circuits(pen: Pen, time: Float) {
        val now = LocalDateTime.now()
        val months = listOf("JAN", "FEB", "MAR", "APR", "MAY", "JUN", "JUL", "AUG", "SEP", "OCT", "NOV", "DEC")
        val rows = listOf(
            "DESTINATION TIME" to "OCT 21 2015  04:29",
            "PRESENT TIME" to "%s %02d %d  %02d:%02d".format(months[now.monthValue - 1], now.dayOfMonth, now.year, now.hour, now.minute),
            "LAST TIME DEPARTED" to "NOV 12 1955  06:38",
        )
        for ((i, row) in rows.withIndex()) {
            val y = 80f + i * 280f
            pen.rect(100f, y, 2300f, y + 220f, Weight.MAIN)
            pen.rect(160f, y + 40f, 1900f, y + 150f, Weight.THIN)
            pen.text(1030f, y + 125f, row.second, 9f, Paint.Align.CENTER, bold = true)
            pen.text(1030f, y + 205f, row.first, 5.5f, Paint.Align.CENTER, alpha = 160)
            pen.glowDot(2100f, y + 95f, 18f, if (i == 1 && sin(time * 4f) > 0f) 1f else 0.3f)
        }
        // La jauge de charge, jusqu'à 1,21 GW.
        val charge = (time % 6f) / 6f
        pen.text(100f, 960f, "1.21 GW", 7f, bold = true)
        pen.rect(700f, 900f, 2300f, 960f, Weight.THIN)
        pen.glowLine(710f, 930f, 710f + (2290f - 710f) * charge, 930f, 0.9f, 2f)
    }
}
