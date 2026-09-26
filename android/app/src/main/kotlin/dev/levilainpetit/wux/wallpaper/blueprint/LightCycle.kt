package dev.levilainpetit.wux.wallpaper.blueprint

import kotlin.math.cos
import kotlin.math.sin

/**
 * Tron : l'héritage — la moto de lumière, d'après un plan de travail du film :
 * profil, dessus, face et arrière, avec les notes du dessinateur. Les roues
 * sans moyeu tournent, le liseré pulse, un ruban de lumière part de l'arrière.
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
        val column = width * 0.65f
        // Le profil, tourné, sur toute la hauteur ; à droite, le dessus, la
        // face et l'arrière, qui glissent dans l'autre sens.
        pen.fit(3800f, 1700f, 18 * d + shift, top, column + shift, bottom, turn = true, x0 = -100f, y0 = -250f)
        side(pen, time)
        pen.fit(3600f, 1200f, column + 4 * d - shift, top, width - 18 * d - shift, top + zone * 0.54f, turn = true, x0 = -100f, y0 = -200f)
        plan(pen, time)
        pen.fit(1300f, 1550f, column + 4 * d - shift, top + zone * 0.54f, width - 18 * d - shift, top + zone * 0.77f, x0 = -100f, y0 = -250f)
        front(pen, time)
        pen.fit(1300f, 1550f, column + 4 * d - shift, top + zone * 0.77f, width - 18 * d - shift, bottom, x0 = -100f, y0 = -250f)
        rear(pen, time)
    }

    /** Des points sur un arc : centre, rayon, de [from] à [to] degrés. */
    private fun arcPoints(cx: Float, cy: Float, r: Float, from: Float, to: Float, steps: Int = 14): FloatArray {
        val out = FloatArray((steps + 1) * 2)
        for (i in 0..steps) {
            val a = Math.toRadians((from + (to - from) * i / steps).toDouble())
            out[i * 2] = cx + cos(a).toFloat() * r
            out[i * 2 + 1] = cy + sin(a).toFloat() * r
        }
        return out
    }

    private fun side(pen: Pen, time: Float) {
        val ground = 1180f
        val front = 650f
        val rear = 2760f
        val axle = 660f
        val tyre = 520f
        pen.line(-80f, ground, 3600f, ground, Weight.THIN)
        for (x in 0..36) pen.line(-60f + x * 100f, ground, -100f + x * 100f, ground + 30f, Weight.HAIR)

        // La coque : l'arche avant, ronde, autour de la roue ; le dessus qui
        // descend à l'assise surélevée ; le carénage arrière qui enveloppe la
        // roue et se prolonge en poupe ; le dessous courbe.
        val frontCowl = arcPoints(front, axle, tyre + 90f, 150f, 300f)
        val rearCowl = arcPoints(rear, axle, tyre + 100f, 225f, 360f)
        val top = floatArrayOf(1150f, 230f, 1350f, 350f, 1460f, 330f, 1600f, 262f, 1800f, 272f, 1950f, 330f, 2150f, 300f)
        val tail = floatArrayOf(3420f, 780f, 3330f, 940f, 3000f, 1085f, 2400f, 1065f, 1800f, 1125f, 1300f, 1075f, 900f, 1055f)
        val shell = frontCowl + top + rearCowl + tail
        pen.shade(shell, 16)
        pen.poly(shell, closed = true, weight = Weight.THICK)
        // L'épaisseur des carénages, à l'intérieur.
        pen.poly(arcPoints(front, axle, tyre + 35f, 160f, 295f), weight = Weight.THIN)
        pen.poly(arcPoints(rear, axle, tyre + 40f, 230f, 355f), weight = Weight.THIN)
        // L'assise surélevée, le carter du moteur, plus grand, et ses ailettes.
        pen.poly(floatArrayOf(1440f, 340f, 1560f, 290f, 1780f, 290f, 1900f, 335f), weight = Weight.MAIN)
        pen.poly(floatArrayOf(1640f, 560f, 2200f, 520f, 2290f, 660f, 2230f, 870f, 1700f, 900f, 1600f, 740f), closed = true, weight = Weight.MAIN)
        for (i in 0 until 5) pen.line(1720f + i * 110f, 590f, 1690f + i * 110f, 850f, Weight.HAIR)
        pen.poly(floatArrayOf(1150f, 230f, 1320f, 540f, 1600f, 740f), weight = Weight.HAIR)
        pen.poly(floatArrayOf(2150f, 300f, 2260f, 440f, 2290f, 660f), weight = Weight.HAIR)

        // Les roues : le pneu, la bande lumineuse, et la coque qui passe au centre.
        for (cx in floatArrayOf(front, rear)) {
            pen.circle(cx, axle, tyre, Weight.THICK)
            pen.circle(cx, axle, tyre - 70f, Weight.MAIN)
            pen.circle(cx, axle, 330f, Weight.MAIN)
            pen.axis(cx - tyre - 80f, axle, cx + tyre + 80f, axle)
            pen.axis(cx, axle - tyre - 80f, cx, axle + tyre + 80f)
            val spin = time * 140f
            for (s in 0 until 12) pen.glowArc(cx, axle, tyre - 35f, spin + s * 30f, 16f, 0.9f, 1.4f)
        }
        // Le passage de roue avant, rond et éclairé, plus large ; l'hexagone arrière.
        pen.circle(front, axle, 360f, Weight.THIN)
        pen.glowArc(front, axle, 345f, 0f, 360f, 0.55f + 0.25f * sin(time * 2f), 2f)
        val hex = FloatArray(12) { i ->
            val a = Math.toRadians(i / 2 * 60.0 + 30.0)
            if (i % 2 == 0) rear + cos(a).toFloat() * 230f else axle + sin(a).toFloat() * 230f
        }
        pen.poly(hex, closed = true, weight = Weight.MAIN)
        pen.circle(rear, axle, 90f, Weight.THIN)

        // Le liseré, plus large, qui pulse de l'avant vers l'arrière.
        val strip = floatArrayOf(
            150f, 880f, 130f, 620f, 250f, 380f, 480f, 205f, 880f, 190f, 1180f, 330f, 1450f, 560f,
            1750f, 990f, 2350f, 1000f, 2950f, 1030f, 3280f, 880f, 3380f, 720f,
        )
        for (i in 0 until strip.size / 2 - 1) {
            val level = 0.55f + 0.45f * sin(time * 3.2f - i * 0.7f)
            pen.glowLine(strip[i * 2], strip[i * 2 + 1], strip[i * 2 + 2], strip[i * 2 + 3], level, 2.2f)
        }
        // Le ruban de lumière, qui se dépose derrière la moto.
        val ripple = 10f * sin(time * 5f)
        pen.glowPoly(floatArrayOf(3420f, 780f, 3560f, 790f + ripple, 3700f, 785f - ripple), level = 0.8f, width = 2.4f)

        // Les cotes.
        pen.dim(40f, ground, 3420f, ground, -120f, "3 380")
        pen.dim(front, axle, rear, axle, -700f, "2 110")
        pen.dim(3560f, ground, 3560f, 40f, -60f, "1 140")
        // Les notes du dessinateur.
        pen.note(1680f, 270f, 1500f, -60f, "RAISED SEAT")
        pen.note(front - 200f, axle - tyre - 40f, 60f, -120f, "COMPLETELY ROUNDED-OFF|WHEEL WELL")
        pen.note(front + 250f, axle - 250f, 1000f, -40f, "FRONT LIGHTED|WHEEL WELL WIDER")
        pen.note(3380f, 400f, 3000f, -60f, "PUSHED BACK WITH|DIFFERENT SHAPE")
        pen.note(rear + 200f, axle + 80f, 2800f, 1360f, "HEXAGON SHAPE|NOT CIRCLE")
        pen.note(1950f, 780f, 2000f, 1360f, "LARGER")
        pen.note(1500f, 1100f, 1300f, 1360f, "CURVED BOTTOM")
        pen.note(140f, 760f, 60f, 1360f, "WIDER LIGHTS")
        pen.note(3200f, 1000f, 3300f, 1360f, "WIDER AND|CURVED")
        pen.caption("SIDE ELEVATION", "ENCOM 786 · LIGHT CYCLE")
    }

    private fun plan(pen: Pen, time: Float) {
        val cy = 400f
        // Vue de dessus : l'avant étroit et arrondi, l'arrière plus large et galbé.
        val right = floatArrayOf(
            40f, 0f, 110f, 150f, 380f, 220f, 900f, 210f, 1300f, 270f, 1700f, 350f, 2200f, 365f,
            2700f, 350f, 3100f, 290f, 3350f, 170f, 3420f, 0f,
        )
        val outline = symmetric(right, cy, 1f)
        pen.shade(outline, 14)
        pen.poly(outline, closed = true, weight = Weight.THICK)
        // Le carénage intérieur, en retrait du bord, et l'arête centrale.
        pen.poly(symmetric(right, cy, 0.72f, from = 2, to = right.size / 2 - 2), closed = true, weight = Weight.THIN)
        pen.axis(-80f, cy, 3500f, cy)
        // Les roues, sous la coque : cachées, sauf le nez du pneu avant.
        for (x in floatArrayOf(130f, 2240f)) {
            pen.hidden(x, cy - 90f, x + 1040f, cy - 90f)
            pen.hidden(x, cy + 90f, x + 1040f, cy + 90f)
            pen.hidden(x + 520f, cy - 110f, x + 520f, cy + 110f)
        }
        pen.arc(130f, cy, 90f, 90f, 180f, Weight.MAIN)
        // Le tableau de bord, reculé, la bulle et l'assise.
        pen.poly(floatArrayOf(1480f, cy - 160f, 1680f, cy - 190f, 1680f, cy + 190f, 1480f, cy + 160f), closed = true, weight = Weight.THIN)
        pen.poly(floatArrayOf(1050f, cy, 1200f, cy - 90f, 1420f, cy - 140f, 1480f, cy - 130f, 1480f, cy + 130f, 1420f, cy + 140f, 1200f, cy + 90f), closed = true, weight = Weight.THIN)
        pen.line(1200f, cy - 90f, 1200f, cy + 90f, Weight.HAIR)
        pen.poly(floatArrayOf(1700f, cy - 200f, 2150f, cy - 220f, 2150f, cy + 220f, 1700f, cy + 200f), closed = true, weight = Weight.HAIR)
        for (x in floatArrayOf(1810f, 1920f, 2030f)) pen.line(x, cy - 205f, x, cy + 205f, Weight.HAIR)
        pen.poly(floatArrayOf(2250f, cy - 250f, 2950f, cy - 230f, 2950f, cy + 230f, 2250f, cy + 250f), closed = true, weight = Weight.HAIR)
        // L'aileron arrière, fin, sur l'axe.
        pen.poly(floatArrayOf(2950f, cy - 35f, 3330f, cy - 20f, 3380f, cy, 3330f, cy + 20f, 2950f, cy + 35f), closed = true, weight = Weight.MAIN)
        // Les grilles d'aération des flancs.
        for (i in 0 until 5) {
            val x = 2350f + i * 110f
            pen.line(x, cy - 330f, x + 60f, cy - 290f, Weight.HAIR)
            pen.line(x, cy + 330f, x + 60f, cy + 290f, Weight.HAIR)
        }
        val level = 0.7f + 0.3f * sin(time * 2f)
        pen.glowPoly(floatArrayOf(400f, cy - 175f, 900f, cy - 180f, 1300f, cy - 230f, 1700f, cy - 300f, 2200f, cy - 315f, 2700f, cy - 300f, 3100f, cy - 250f, 3330f, cy - 120f), level = level, width = 1.6f)
        pen.glowPoly(floatArrayOf(400f, cy + 175f, 900f, cy + 180f, 1300f, cy + 230f, 1700f, cy + 300f, 2200f, cy + 315f, 2700f, cy + 300f, 3100f, cy + 250f, 3330f, cy + 120f), level = level, width = 1.6f)
        pen.glowLine(2980f, cy, 3360f, cy, level = level, width = 1.2f)
        pen.dim(40f, cy + 365f, 3420f, cy + 365f, -130f, "3 380")
        pen.dim(3470f, cy + 365f, 3470f, cy - 365f, -40f, "730")
        pen.note(1580f, cy - 175f, 1250f, -120f, "DASH PUSHED BACK")
        pen.note(2200f, cy + 365f, 2300f, 960f, "CURVIER SHAPE")
        pen.note(3200f, cy - 25f, 3000f, -120f, "THINNER FIN")
        pen.caption("PLAN VIEW")
    }

    /**
     * Le contour fermé d'une demi-vue de dessus [half] (x, demi-largeur),
     * mise à l'échelle [inset] en largeur, entre les points [from] et [to].
     */
    private fun symmetric(half: FloatArray, cy: Float, inset: Float, from: Int = 0, to: Int = half.size / 2 - 1): FloatArray {
        val n = to - from + 1
        val out = FloatArray(n * 4)
        for (i in 0 until n) {
            val j = from + i
            val k = to - i
            out[i * 2] = half[j * 2]
            out[i * 2 + 1] = cy - half[j * 2 + 1] * inset
            out[(n + i) * 2] = half[k * 2]
            out[(n + i) * 2 + 1] = cy + half[k * 2 + 1] * inset
        }
        return out
    }

    /** Un pneu vu de face ou de dos : bande, flancs, sculptures et contact au sol. */
    private fun tyre(pen: Pen, cx: Float, top: Float, ground: Float, time: Float) {
        pen.rect(cx - 95f, top, cx + 95f, ground, Weight.MAIN)
        pen.line(cx - 60f, top, cx - 60f, ground, Weight.HAIR)
        pen.line(cx + 60f, top, cx + 60f, ground, Weight.HAIR)
        // Les sculptures défilent : la roue tourne.
        val pitch = 70f
        var y = top + (time * 90f) % pitch
        while (y < ground - 10f) {
            pen.line(cx - 60f, y, cx + 60f, y + 18f, Weight.HAIR)
            y += pitch
        }
        pen.hatch(cx - 95f, ground - 40f, cx + 95f, ground)
        val ring = 0.6f + 0.4f * sin(time * 3f)
        pen.glowLine(cx - 78f, top + 20f, cx - 78f, ground - 20f, level = ring, width = 1.4f)
        pen.glowLine(cx + 78f, top + 20f, cx + 78f, ground - 20f, level = ring, width = 1.4f)
    }

    private fun front(pen: Pen, time: Float) {
        val cx = 550f
        val ground = 1180f
        pen.line(0f, ground, 1100f, ground, Weight.THIN)
        // De face : la haute écope bombée, les flancs un peu plus larges, la roue dessous.
        val shell = floatArrayOf(
            cx - 270f, 1000f, cx - 370f, 760f, cx - 380f, 480f, cx - 300f, 230f, cx - 150f, 60f, cx, 20f,
            cx + 150f, 60f, cx + 300f, 230f, cx + 380f, 480f, cx + 370f, 760f, cx + 270f, 1000f,
        )
        pen.shade(shell, 16)
        pen.poly(shell, closed = true, weight = Weight.THICK)
        // Le carénage intérieur et la bulle, avec son reflet.
        pen.poly(floatArrayOf(cx - 200f, 960f, cx - 300f, 740f, cx - 310f, 480f, cx - 240f, 260f, cx - 120f, 110f, cx, 80f, cx + 120f, 110f, cx + 240f, 260f, cx + 310f, 480f, cx + 300f, 740f, cx + 200f, 960f), weight = Weight.HAIR)
        pen.poly(floatArrayOf(cx - 200f, 330f, cx - 120f, 160f, cx + 120f, 160f, cx + 200f, 330f, cx + 150f, 440f, cx - 150f, 440f), closed = true, weight = Weight.THIN)
        pen.line(cx - 90f, 200f, cx - 150f, 330f, Weight.HAIR)
        pen.line(cx - 50f, 200f, cx - 100f, 310f, Weight.HAIR)
        // Le phare : une fente horizontale.
        pen.rect(cx - 170f, 480f, cx + 170f, 520f, Weight.THIN)
        pen.glowLine(cx - 150f, 500f, cx + 150f, 500f, level = 0.7f + 0.3f * sin(time * 2.4f), width = 2.2f)
        // Les prises d'air sous le phare.
        for (i in 0 until 4) {
            val y = 570f + i * 45f
            pen.line(cx - 280f + i * 8f, y, cx - 130f, y, Weight.HAIR)
            pen.line(cx + 130f, y, cx + 280f - i * 8f, y, Weight.HAIR)
        }
        tyre(pen, cx, 760f, ground, time)
        pen.axis(cx, -60f, cx, ground + 60f)
        pen.glowPoly(floatArrayOf(cx - 350f, 560f, cx - 320f, 850f, cx - 240f, 980f), level = 0.8f, width = 2f)
        pen.glowPoly(floatArrayOf(cx + 350f, 560f, cx + 320f, 850f, cx + 240f, 980f), level = 0.8f, width = 2f)
        pen.dim(cx - 380f, ground, cx + 380f, ground, 160f, "760")
        pen.note(cx + 60f, 30f, cx + 250f, -140f, "HIGHER SCOOP")
        pen.note(cx - 380f, 480f, -40f, 180f, "SLIGHTLY|WIDER")
        pen.note(cx + 170f, 500f, cx + 470f, 700f, "HEADLIGHT|SLIT")
        pen.caption("FRONT VIEW")
    }

    private fun rear(pen: Pen, time: Float) {
        val cx = 550f
        val ground = 1180f
        pen.line(0f, ground, 1100f, ground, Weight.THIN)
        // De dos : une écope toute différente, un aileron plus fin, le feu en obus, surélevé.
        val shell = floatArrayOf(
            cx - 280f, 1000f, cx - 390f, 740f, cx - 400f, 450f, cx - 330f, 220f, cx - 90f, 110f, cx - 30f, 20f,
            cx + 30f, 20f, cx + 90f, 110f, cx + 330f, 220f, cx + 400f, 450f, cx + 390f, 740f, cx + 280f, 1000f,
        )
        pen.shade(shell, 16)
        pen.poly(shell, closed = true, weight = Weight.THICK)
        // L'aileron, fin, qui s'évase dans la coque.
        pen.poly(floatArrayOf(cx - 30f, 20f, cx - 22f, 300f, cx - 60f, 420f), weight = Weight.THIN)
        pen.poly(floatArrayOf(cx + 30f, 20f, cx + 22f, 300f, cx + 60f, 420f), weight = Weight.THIN)
        // L'écope arrière : deux coquilles galbées de part et d'autre.
        pen.poly(floatArrayOf(cx - 300f, 300f, cx - 180f, 240f, cx - 70f, 260f, cx - 90f, 440f, cx - 260f, 470f, cx - 330f, 420f), closed = true, weight = Weight.HAIR)
        pen.poly(floatArrayOf(cx + 300f, 300f, cx + 180f, 240f, cx + 70f, 260f, cx + 90f, 440f, cx + 260f, 470f, cx + 330f, 420f), closed = true, weight = Weight.HAIR)
        pen.poly(floatArrayOf(cx - 330f, 780f, cx - 200f, 860f, cx + 200f, 860f, cx + 330f, 780f), weight = Weight.HAIR)
        // Le feu arrière, en obus, surélevé.
        pen.rect(cx - 110f, 520f, cx + 110f, 680f, Weight.MAIN)
        pen.arc(cx, 600f, 80f, 0f, 360f, Weight.THIN)
        pen.arc(cx, 600f, 45f, 0f, 360f, Weight.HAIR)
        pen.glowDot(cx, 600f, 40f, 0.75f + 0.25f * sin(time * 3f))
        // L'émetteur du ruban de lumière, sous le feu.
        pen.rect(cx - 170f, 900f, cx + 170f, 940f, Weight.THIN)
        pen.glowLine(cx - 150f, 920f, cx + 150f, 920f, level = 0.6f + 0.4f * sin(time * 4f), width = 2f)
        tyre(pen, cx, 960f, ground, time)
        pen.axis(cx, -60f, cx, ground + 60f)
        pen.glowPoly(floatArrayOf(cx - 360f, 540f, cx - 330f, 840f, cx - 250f, 980f), level = 0.8f, width = 2f)
        pen.glowPoly(floatArrayOf(cx + 360f, 540f, cx + 330f, 840f, cx + 250f, 980f), level = 0.8f, width = 2f)
        pen.dim(cx - 400f, ground, cx + 400f, ground, 160f, "800")
        pen.note(cx - 200f, 250f, -40f, -120f, "DIFFERENT SCOOP|ALTOGETHER")
        pen.note(cx + 25f, 150f, cx + 300f, 20f, "THINNER FIN")
        pen.note(cx + 110f, 600f, cx + 420f, 760f, "BULLET|RAISED")
        pen.note(cx - 170f, 920f, -40f, 800f, "RIBBON|EMITTER")
        pen.caption("REAR VIEW")
    }
}
