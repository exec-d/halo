package dev.levilainpetit.wux.wallpaper.blueprint

import android.graphics.Paint
import kotlin.math.cos
import kotlin.math.sin

/**
 * Tron : l'héritage — la moto de lumière. Profil et vue de dessus ; les
 * roues sans moyeu tournent, le liseré pulse, un ruban de lumière part de
 * l'arrière.
 */
class LightCycle : Subject {
    override val title = "LIGHT CYCLE"
    override val reference = "TRON: LEGACY (2010) · ENCOM 786"
    override val scale = "1:25"
    override val sheet = 1
    override val frame = 50L

    override fun draw(pen: Pen, width: Float, top: Float, bottom: Float, time: Float, tiltX: Float, tiltY: Float) {
        val d = pen.density
        val zone = bottom - top
        val shift = tiltX * 8f * d
        // Le profil, tourné, sur toute la hauteur ; à côté, la vue de dessus
        // et la vue de face, qui glissent dans l'autre sens.
        pen.fit(3700f, 1500f, 22 * d + shift, top, width * 0.6f + shift, bottom, turn = true)
        side(pen, time)
        pen.fit(3700f, 900f, width * 0.62f - shift, top, width - 22 * d - shift, top + zone * 0.7f, turn = true)
        plan(pen, time)
        pen.fit(1100f, 1450f, width * 0.62f - shift, top + zone * 0.72f, width - 22 * d - shift, bottom)
        front(pen, time)
    }

    private fun side(pen: Pen, time: Float) {
        val ground = 1180f
        pen.line(-80f, ground, 3480f, ground, Weight.THIN)
        for (x in 0..34) pen.line(-60f + x * 100f, ground, -120f + x * 100f, ground + 50f, Weight.HAIR)
        val front = 650f
        val rear = 2760f
        val axle = 660f
        val tyre = 520f

        // La coque : l'arche avant autour de la roue, le dessus qui plonge
        // jusqu'à l'assise surélevée, la haute écope arrière, le dessous courbe.
        val shell = floatArrayOf(
            110f, 900f, 90f, 700f, 130f, 470f, 260f, 290f, 450f, 170f, 650f, 125f, 900f, 150f,
            1150f, 250f, 1400f, 390f, 1600f, 420f, 1780f, 330f, 1950f, 300f, 2150f, 250f,
            2380f, 120f, 2620f, 60f, 2860f, 70f, 3080f, 160f, 3260f, 360f, 3330f, 620f, 3290f, 860f,
            3150f, 1020f, 2950f, 1110f, 2350f, 1080f, 1750f, 1110f, 1150f, 1080f, 650f, 1120f, 300f, 1060f,
        )
        pen.shade(shell, 14)
        pen.poly(shell, closed = true, weight = Weight.THICK)
        // L'assise, la bulle de l'écope, le carter du moteur.
        pen.poly(floatArrayOf(1420f, 400f, 1560f, 330f, 1780f, 300f, 1880f, 330f), weight = Weight.MAIN)
        pen.poly(floatArrayOf(2200f, 260f, 2420f, 170f, 2700f, 150f, 2900f, 200f, 2700f, 260f, 2400f, 280f), closed = true, weight = Weight.THIN)
        pen.poly(floatArrayOf(1750f, 560f, 2150f, 520f, 2250f, 640f, 2200f, 820f, 1800f, 860f, 1700f, 720f), closed = true, weight = Weight.MAIN)
        for (i in 0 until 4) pen.line(1800f + i * 100f, 600f, 1760f + i * 100f, 820f, Weight.HAIR)
        pen.poly(floatArrayOf(1150f, 250f, 1300f, 560f, 1600f, 700f), weight = Weight.HAIR)

        // Les roues : un pneu, une bande lumineuse, et la coque qui passe au centre.
        for (cx in floatArrayOf(front, rear)) {
            pen.circle(cx, axle, tyre, Weight.THICK)
            pen.circle(cx, axle, tyre - 70f, Weight.MAIN)
            pen.circle(cx, axle, 330f, Weight.MAIN)
            pen.axis(cx - tyre - 80f, axle, cx + tyre + 80f, axle)
            pen.axis(cx, axle - tyre - 80f, cx, axle + tyre + 80f)
            val spin = time * 140f
            for (s in 0 until 12) pen.glowArc(cx, axle, tyre - 35f, spin + s * 30f, 16f, 0.9f, 1.4f)
        }
        // Le passage de roue avant, arrondi, éclairé ; l'hexagone de la roue arrière.
        pen.glowArc(front, axle, 330f, 0f, 360f, 0.55f + 0.25f * sin(time * 2f), 1.2f)
        val hex = FloatArray(12) { i ->
            val a = Math.toRadians(i / 2 * 60.0 + 30.0)
            if (i % 2 == 0) rear + cos(a).toFloat() * 230f else axle + sin(a).toFloat() * 230f
        }
        pen.poly(hex, closed = true, weight = Weight.MAIN)
        pen.circle(rear, axle, 90f, Weight.THIN)

        // Le liseré, qui pulse d'avant en arrière le long du dessous.
        val strip = floatArrayOf(
            160f, 860f, 150f, 600f, 280f, 360f, 500f, 230f, 900f, 230f, 1200f, 360f, 1450f, 560f,
            1750f, 960f, 2350f, 980f, 2950f, 1010f, 3200f, 860f, 3270f, 620f,
        )
        for (i in 0 until strip.size / 2 - 1) {
            val level = 0.55f + 0.45f * sin(time * 3.2f - i * 0.7f)
            pen.glowLine(strip[i * 2], strip[i * 2 + 1], strip[i * 2 + 2], strip[i * 2 + 3], level, 1.8f)
        }
        // Le ruban de lumière, qui se dépose derrière la moto.
        val ripple = 10f * sin(time * 5f)
        pen.glowPoly(floatArrayOf(3290f, 860f, 3480f, 880f + ripple, 3720f, 875f - ripple), level = 0.8f, width = 2.4f)

        pen.dim(90f, ground, 3330f, ground, -150f, "3 240")
        pen.dim(front, axle, rear, axle, -700f, "2 110")
        pen.dim(3420f, ground, 3420f, 60f, -60f, "1 120")
        pen.callout(1650f, 400f, 1500f, 20f, 1)
        pen.callout(front + 230f, axle - 240f, 1000f, -60f, 2)
        pen.callout(rear + 160f, axle + 90f, 3150f, 1300f, 3)
        pen.callout(2000f, 700f, 2050f, 1320f, 4)
        pen.callout(2700f, 90f, 2950f, -80f, 5)
        pen.caption("SIDE ELEVATION", "1 RAISED SEAT  2 WHEEL WELL  3 HEX HUB  4 DRIVE  5 SCOOP")
    }

    private fun plan(pen: Pen, time: Float) {
        val cy = 400f
        // Vue de dessus : l'avant étroit et arrondi, l'arrière plus large.
        val right = floatArrayOf(
            90f, 0f, 150f, 150f, 400f, 210f, 900f, 200f, 1300f, 260f, 1700f, 340f, 2200f, 350f,
            2700f, 340f, 3100f, 280f, 3300f, 160f, 3340f, 0f,
        )
        val outline = FloatArray(right.size * 2)
        for (i in 0 until right.size / 2) {
            outline[i * 2] = right[i * 2]
            outline[i * 2 + 1] = cy - right[i * 2 + 1]
        }
        val n = right.size / 2
        for (i in 0 until n) {
            outline[(n + i) * 2] = right[(n - 1 - i) * 2]
            outline[(n + i) * 2 + 1] = cy + right[(n - 1 - i) * 2 + 1]
        }
        pen.shade(outline, 12)
        pen.poly(outline, closed = true, weight = Weight.THICK)
        pen.axis(-80f, cy, 3480f, cy)
        for (x in floatArrayOf(130f, 2240f)) pen.rect(x, cy - 90f, x + 1040f, cy + 90f, Weight.MAIN)
        pen.poly(floatArrayOf(1400f, cy - 180f, 1900f, cy - 200f, 1900f, cy + 200f, 1400f, cy + 180f), closed = true, weight = Weight.THIN)
        pen.poly(floatArrayOf(2250f, cy - 240f, 2900f, cy - 220f, 2900f, cy + 220f, 2250f, cy + 240f), closed = true, weight = Weight.HAIR)
        val level = 0.7f + 0.3f * sin(time * 2f)
        pen.glowPoly(floatArrayOf(400f, cy - 170f, 1300f, cy - 220f, 2200f, cy - 300f, 3050f, cy - 240f), level = level, width = 1.4f)
        pen.glowPoly(floatArrayOf(400f, cy + 170f, 1300f, cy + 220f, 2200f, cy + 300f, 3050f, cy + 240f), level = level, width = 1.4f)
        pen.dim(90f, cy + 350f, 3340f, cy + 350f, -120f, "3 250")
        pen.dim(3420f, cy + 350f, 3420f, cy - 350f, -40f, "700")
        pen.caption("PLAN VIEW")
    }

    private fun front(pen: Pen, time: Float) {
        val cx = 550f
        val ground = 1180f
        pen.line(0f, ground, 1100f, ground, Weight.THIN)
        // De face : la haute écope bombée, les flancs qui s'élargissent, la roue dessous.
        val shell = floatArrayOf(
            cx - 260f, 1000f, cx - 350f, 760f, cx - 360f, 480f, cx - 290f, 230f, cx - 150f, 90f, cx, 60f,
            cx + 150f, 90f, cx + 290f, 230f, cx + 360f, 480f, cx + 350f, 760f, cx + 260f, 1000f,
        )
        pen.shade(shell, 14)
        pen.poly(shell, closed = true, weight = Weight.THICK)
        pen.poly(floatArrayOf(cx - 200f, 330f, cx - 120f, 180f, cx + 120f, 180f, cx + 200f, 330f, cx + 150f, 560f, cx - 150f, 560f), closed = true, weight = Weight.THIN)
        pen.rect(cx - 95f, 600f, cx + 95f, ground, Weight.MAIN)
        pen.axis(cx, -20f, cx, ground + 60f)
        pen.glowDot(cx, 760f, 30f, 0.7f + 0.3f * sin(time * 2.4f))
        pen.glowPoly(floatArrayOf(cx - 330f, 560f, cx - 300f, 850f, cx - 230f, 980f), level = 0.8f)
        pen.glowPoly(floatArrayOf(cx + 330f, 560f, cx + 300f, 850f, cx + 230f, 980f), level = 0.8f)
        pen.dim(cx - 360f, ground, cx + 360f, ground, -110f, "720")
        pen.caption("FRONT VIEW")
    }
}
