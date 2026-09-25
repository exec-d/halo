package dev.levilainpetit.wux.wallpaper.blueprint

import android.graphics.Paint
import kotlin.math.sin

/**
 * Terminator — le crâne de l'endosquelette T-800. De face, et le détail d'un
 * capteur optique ; les yeux s'allument, un réticule balaie la vue.
 */
class Endoskeleton : Subject {
    override val title = "T-800 ENDOSKELETON"
    override val reference = "THE TERMINATOR (1984) · CYBERDYNE SYSTEMS 101"
    override val scale = "1:2"
    override val sheet = 9

    override fun draw(pen: Pen, width: Float, top: Float, bottom: Float, time: Float, tiltX: Float, tiltY: Float) {
        val d = pen.density
        val zone = bottom - top
        val shift = tiltX * 8f * d
        pen.fit(1000f, 1700f, 24 * d + shift, top, width * 0.64f + shift, bottom)
        skull(pen, time)
        pen.fit(600f, 900f, width * 0.66f - shift, top + zone * 0.1f, width - 24 * d - shift, top + zone * 0.55f)
        sensor(pen, time)
    }

    private fun skull(pen: Pen, time: Float) {
        val cx = 500f
        pen.axis(cx, 20f, cx, 1680f)
        // Le crâne, la mâchoire, en miroir autour de l'axe.
        pen.mirror(floatArrayOf(cx, 80f, 330f, 110f, 200f, 220f, 130f, 420f, 120f, 640f, 150f, 820f, 210f, 930f), cx, weight = Weight.THICK)
        pen.mirror(floatArrayOf(210f, 930f, 230f, 1100f, 290f, 1260f, 380f, 1400f, cx, 1440f), cx, weight = Weight.THICK)
        // Les plaques des tempes et du front.
        pen.mirror(floatArrayOf(250f, 250f, 400f, 200f, 440f, 380f, 220f, 440f), cx, weight = Weight.HAIR)
        pen.line(cx - 30f, 90f, cx - 30f, 520f, Weight.HAIR)
        pen.line(cx + 30f, 90f, cx + 30f, 520f, Weight.HAIR)
        // Les orbites, et les yeux qui s'allument.
        val flicker = if (sin(time * 13f) > 0.93f) 0.4f else 1f
        for (side in floatArrayOf(-1f, 1f)) {
            val ex = cx + side * 150f
            pen.poly(floatArrayOf(ex - 110f, 560f, ex + 110f, 560f, ex + 120f, 700f, ex - 100f, 720f), closed = true, weight = Weight.MAIN)
            pen.circle(ex, 640f, 48f, Weight.THIN)
            pen.glowDot(ex, 640f, 22f, 0.85f * flicker)
            // Les pommettes et les vérins de la mâchoire.
            pen.poly(floatArrayOf(cx + side * 60f, 760f, cx + side * 250f, 820f, cx + side * 300f, 900f), weight = Weight.MAIN)
            pen.line(cx + side * 290f, 930f, cx + side * 250f, 1250f, Weight.THICK)
            pen.circle(cx + side * 290f, 930f, 30f, Weight.MAIN)
            pen.circle(cx + side * 250f, 1250f, 30f, Weight.MAIN)
        }
        // Le nez, les dents.
        pen.poly(floatArrayOf(cx, 740f, cx - 55f, 900f, cx + 55f, 900f), closed = true, weight = Weight.MAIN)
        for (row in 0..1) {
            val y = 1010f + row * 120f
            for (t in -4..3) pen.rect(cx + t * 50f + 2f, y, cx + t * 50f + 48f, y + 100f, Weight.THIN)
            pen.line(cx - 210f, y + (if (row == 0) 100f else 0f), cx + 210f, y + (if (row == 0) 100f else 0f), Weight.MAIN)
        }
        // Le cou : les câbles et la colonne.
        pen.rect(cx - 70f, 1440f, cx + 70f, 1680f, Weight.MAIN)
        for (k in 1..3) {
            pen.line(cx - 70f - k * 45f, 1400f, cx - 70f - k * 45f, 1680f, Weight.THIN)
            pen.line(cx + 70f + k * 45f, 1400f, cx + 70f + k * 45f, 1680f, Weight.THIN)
        }
        pen.dim(120f, 1500f, 880f, 1500f, -130f, "196")
        pen.callout(cx + 150f, 640f, 900f, 400f, 1)
        pen.callout(cx + 250f, 1250f, 920f, 1250f, 2)
        pen.text(0f, 1690f, "FRONT VIEW · 1 OPTICAL SENSOR  2 JAW SERVO", 7f, bold = true)
    }

    /** Le capteur optique, et le réticule qui balaie. */
    private fun sensor(pen: Pen, time: Float) {
        val c = 300f
        pen.circle(c, c, 260f, Weight.THICK)
        pen.circle(c, c, 180f, Weight.MAIN)
        pen.circle(c, c, 80f, Weight.THIN)
        pen.glowDot(c, c, 40f, 0.9f)
        pen.axis(c - 290f, c, c + 290f, c)
        pen.axis(c, c - 290f, c, c + 290f)
        val sweep = time * 90f
        pen.glowArc(c, c, 220f, sweep, 40f, 0.8f, 1.2f)
        pen.text(0f, 680f, "DETAIL B", 7.5f, bold = true)
        pen.text(0f, 760f, "ANALYSIS: ACTIVE", 6f, alpha = 160)
        pen.text(0f, 830f, "THREAT: %03d".format(((time * 7).toInt()) % 1000), 6f, alpha = 160)
    }
}
