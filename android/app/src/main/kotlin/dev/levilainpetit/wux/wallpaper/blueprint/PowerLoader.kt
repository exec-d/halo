package dev.levilainpetit.wux.wallpaper.blueprint

import android.graphics.Paint
import kotlin.math.sin

/**
 * Aliens — le chargeur P-5000, l'exosquelette de manutention. De face : la
 * cage, les bras hydrauliques qui montent et descendent, les pinces.
 */
class PowerLoader : Subject {
    override val title = "P-5000 POWER LOADER"
    override val reference = "ALIENS (1986) · CATERPILLAR / WEYLAND-YUTANI"
    override val scale = "1:20"
    override val sheet = 7

    override fun draw(pen: Pen, width: Float, top: Float, bottom: Float, time: Float, tiltX: Float, tiltY: Float) {
        val d = pen.density
        val zone = bottom - top
        val shift = tiltX * 8f * d
        pen.fit(2400f, 2600f, 22 * d + shift, top, width - 22 * d + shift, top + zone * 0.74f)
        front(pen, time)
        pen.fit(1400f, 900f, 22 * d - shift, top + zone * 0.74f, width - 22 * d - shift, bottom)
        clamp(pen, time)
    }

    private fun front(pen: Pen, time: Float) {
        val cx = 1200f
        val ground = 2350f
        pen.line(100f, ground, 2300f, ground, Weight.THIN)
        for (x in 0..21) pen.line(150f + x * 100f, ground, 90f + x * 100f, ground + 50f, Weight.HAIR)
        // La cage du pilote, ses arceaux.
        pen.rect(cx - 330f, 450f, cx + 330f, 1450f, Weight.THICK)
        pen.arc(cx, 450f, 330f, 180f, 180f, Weight.MAIN)
        pen.line(cx - 330f, 800f, cx + 330f, 800f, Weight.THIN)
        pen.line(cx - 330f, 1150f, cx + 330f, 1150f, Weight.THIN)
        pen.line(cx, 120f, cx, 450f, Weight.THIN)
        pen.axis(cx, 60f, cx, ground + 60f)
        // Le bloc moteur et les jambes.
        pen.rect(cx - 420f, 1450f, cx + 420f, 1650f, Weight.MAIN)
        pen.hatch(cx - 420f, 1450f, cx + 420f, 1650f, 8f)
        for (side in floatArrayOf(-1f, 1f)) {
            val hip = cx + side * 300f
            val foot = cx + side * 420f
            pen.poly(floatArrayOf(hip - 90f, 1650f, hip + 90f, 1650f, foot + 80f, 2150f, foot - 80f, 2150f), closed = true, weight = Weight.MAIN)
            pen.rect(foot - 220f, 2150f, foot + 220f, ground, Weight.THICK)
            pen.hatch(foot - 220f, 2150f, foot + 220f, ground, 10f)
        }
        // Les bras : l'épaule, le vérin qui coulisse, l'avant-bras, la pince.
        val lift = sin(time * 0.9f) * 120f
        for (side in floatArrayOf(-1f, 1f)) {
            val sx = cx + side * 380f
            val sy = 600f
            val ex = cx + side * 850f
            val ey = 1100f - lift
            val hx = cx + side * 780f
            val hy = 1650f - lift * 1.4f
            pen.circle(sx, sy, 70f, Weight.MAIN)
            pen.line(sx, sy, ex, ey, Weight.THICK)
            pen.circle(ex, ey, 60f, Weight.MAIN)
            pen.line(ex, ey, hx, hy, Weight.THICK)
            pen.line(ex + side * 60f, ey, hx + side * 60f, hy, Weight.MAIN)
            // Le vérin hydraulique, du coude à la cage.
            pen.line(cx + side * 330f, 900f, ex - side * 20f, ey + 80f, Weight.THIN)
            pen.line(cx + side * 330f, 930f, cx + side * 330f + (ex - cx - side * 350f) * 0.55f, 930f + (ey + 80f - 930f) * 0.55f, Weight.THICK)
            // Les rayures de danger sur l'avant-bras.
            val midx = (ex + hx) / 2
            val midy = (ey + hy) / 2
            pen.hatch(midx - 60f, midy - 100f, midx + 60f, midy + 100f, 12f)
            // La pince : deux mâchoires qui s'ouvrent un peu.
            val open = 40f + 30f * sin(time * 1.8f)
            pen.poly(floatArrayOf(hx - 90f, hy, hx - 90f - open, hy + 220f, hx - 30f, hy + 260f), weight = Weight.MAIN)
            pen.poly(floatArrayOf(hx + 90f, hy, hx + 90f + open, hy + 220f, hx + 30f, hy + 260f), weight = Weight.MAIN)
            pen.glowDot(sx, sy, 20f, 0.6f)
        }
        // Les gyrophares du toit.
        val blink = sin(time * 5f) > 0f
        pen.glowDot(cx - 200f, 150f, 30f, if (blink) 1f else 0.2f)
        pen.glowDot(cx + 200f, 150f, 30f, if (blink) 0.2f else 1f)
        pen.text(cx, 1560f, "CAUTION", 7f, Paint.Align.CENTER, bold = true)
        pen.dim(cx - 640f, ground, cx + 640f, ground, -130f, "2 400")
        pen.dim(2250f, ground, 2250f, 120f, -60f, "3 100")
        pen.callout(cx, 900f, cx + 600f, 300f, 1)
        pen.callout(cx - 850f, 1100f - lift, 250f, 700f, 2)
        pen.callout(cx + 780f, 1650f - lift * 1.4f, 2150f, 1900f, 3)
        pen.caption("FRONT VIEW", "1 CAGE  2 ARM  3 CLAMP")
    }

    /** Détail : la pince, son pivot et son vérin. */
    private fun clamp(pen: Pen, time: Float) {
        val open = 0.5f + 0.5f * sin(time * 1.8f)
        val px = 700f
        val py = 250f
        pen.rect(px - 250f, 60f, px + 250f, py, Weight.THICK)
        pen.hatch(px - 250f, 60f, px + 250f, py, 10f)
        pen.circle(px, py, 50f, Weight.MAIN)
        for (side in floatArrayOf(-1f, 1f)) {
            val tipX = px + side * (200f + 160f * open)
            pen.poly(floatArrayOf(px + side * 40f, py, px + side * 220f, py + 280f, tipX, py + 560f, tipX - side * 80f, py + 600f, px + side * 120f, py + 320f), closed = true, weight = Weight.MAIN)
            // Le vérin qui ouvre la mâchoire.
            pen.line(px + side * 250f, 120f, px + side * 180f, py + 250f, Weight.THIN)
            pen.line(px + side * 250f, 120f, px + side * (250f - 50f * open), 120f + (py + 130f - 120f), Weight.THICK)
        }
        pen.axis(px, 20f, px, 880f)
        pen.dim(px - 360f, 880f, px + 360f, 880f, -60f, "OPEN %d°".format((20 + 25 * open).toInt()))
        pen.caption("DETAIL D · CLAMP", "HYDRAULIC 3 000 PSI")
    }
}
