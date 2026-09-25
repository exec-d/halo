package dev.levilainpetit.wux.wallpaper.blueprint

import android.graphics.Paint
import kotlin.math.sin

/**
 * Matrix — le Nebuchadnezzar, l'aéroglisseur de Morpheus. Vue de dessus et
 * profil ; ses propulseurs électromagnétiques s'allument l'un après l'autre.
 */
class Hovercraft : Subject {
    override val title = "NEBUCHADNEZZAR"
    override val reference = "THE MATRIX (1999) · MARK III No. 11"
    override val scale = "1:250"
    override val sheet = 3

    override fun draw(pen: Pen, width: Float, top: Float, bottom: Float, time: Float, tiltX: Float, tiltY: Float) {
        val d = pen.density
        val zone = bottom - top
        val shift = tiltX * 8f * d
        pen.fit(1800f, 5400f, 24 * d + shift, top, width * 0.56f + shift, bottom)
        plan(pen, time)
        pen.fit(1500f, 5400f, width * 0.6f - shift, top, width - 24 * d - shift, bottom)
        side(pen, time)
    }

    /** Vue de dessus, la proue en haut. */
    private fun plan(pen: Pen, time: Float) {
        val cx = 900f
        val hull = floatArrayOf(
            cx, 100f, cx + 260f, 500f, cx + 430f, 1200f, cx + 470f, 2600f, cx + 440f, 4300f, cx + 330f, 4900f, cx + 160f, 5100f,
            cx - 160f, 5100f, cx - 330f, 4900f, cx - 440f, 4300f, cx - 470f, 2600f, cx - 430f, 1200f, cx - 260f, 500f,
        )
        pen.shade(hull, 14)
        pen.poly(hull, closed = true, weight = Weight.THICK)
        pen.axis(cx, 0f, cx, 5300f)
        // Le pont, les cloisons, la dorsale.
        pen.poly(floatArrayOf(cx - 150f, 600f, cx + 150f, 600f, cx + 200f, 1100f, cx - 200f, 1100f), closed = true, weight = Weight.MAIN)
        for (y in floatArrayOf(1300f, 2100f, 2900f, 3700f, 4500f)) pen.line(cx - 380f, y, cx + 380f, y, Weight.HAIR)
        pen.rect(cx - 90f, 1200f, cx + 90f, 4800f, Weight.THIN)
        // Les propulseurs, par paires, qui s'allument à tour de rôle.
        val lit = ((time * 2.5f) % 12).toInt()
        for (i in 0 until 5) {
            val y = 1500f + i * 750f
            for (side in floatArrayOf(-1f, 1f)) {
                val x = cx + side * 610f
                pen.arc(x, y, 190f, if (side < 0) 90f else -90f, 180f, Weight.MAIN)
                pen.circle(x, y, 110f, Weight.THIN)
                pen.line(cx + side * 470f, y - 190f, x, y - 190f, Weight.HAIR)
                pen.line(cx + side * 470f, y + 190f, x, y + 190f, Weight.HAIR)
                val on = i * 2 + (if (side < 0) 0 else 1) == lit || lit >= 10
                pen.glowDot(x, y, 50f, if (on) 1f else 0.25f)
            }
        }
        // L'antenne de poupe.
        pen.line(cx, 5100f, cx, 5300f, Weight.MAIN)
        pen.dim(cx - 470f, 5100f, cx - 470f, 100f, 260f, "85 m")
        pen.callout(cx + 150f, 850f, cx + 700f, 500f, 1)
        pen.callout(cx + 610f, 3000f, cx + 820f, 3400f, 2)
        pen.caption("PLAN VIEW", "1 BRIDGE  2 EMP THRUSTER")
    }

    /** Profil, la proue en haut, flottant au-dessus du sol des égouts. */
    private fun side(pen: Pen, time: Float) {
        val floor = 1300f
        pen.line(floor, 0f, floor, 5300f, Weight.THIN)
        for (y in 0..52) pen.line(floor, y * 100f, floor + 50f, y * 100f - 60f, Weight.HAIR)
        val hull = floatArrayOf(
            600f, 100f, 800f, 500f, 1000f, 1100f, 1000f, 4800f, 900f, 5100f, 450f, 5100f,
            300f, 4700f, 250f, 1300f, 380f, 700f,
        )
        pen.shade(hull, 14)
        pen.poly(hull, closed = true, weight = Weight.THICK)
        pen.poly(floatArrayOf(380f, 700f, 180f, 900f, 180f, 1200f, 250f, 1300f), weight = Weight.MAIN)
        for (y in floatArrayOf(1300f, 2100f, 2900f, 3700f, 4500f)) pen.line(250f, y, 1000f, y, Weight.HAIR)
        // Les propulseurs sous la coque, et leur champ vers le sol.
        val pulse = 0.5f + 0.4f * sin(time * 4f)
        for (i in 0 until 5) {
            val y = 1500f + i * 750f
            pen.rect(1000f, y - 180f, 1120f, y + 180f, Weight.MAIN)
            pen.glowLine(1130f, y - 120f, floor - 20f, y - 120f, pulse * 0.6f, 1f)
            pen.glowLine(1130f, y + 120f, floor - 20f, y + 120f, pulse * 0.6f, 1f)
        }
        pen.caption("SIDE", "EMP READY")
    }
}
