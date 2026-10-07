package dev.levilainpetit.wux.wallpaper

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PointF
import android.graphics.RectF
import android.graphics.Typeface
import kotlin.math.PI
import kotlin.math.ceil
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sin

/**
 * Ghost in the Shell : tout le téléphone devient le cyberespace du film de
 * 1995, vu pendant une plongée dans le réseau.
 *
 * - au fond, les rayons du tunnel et les cascades de chiffres et de
 *   katakanas, plus petits au loin ;
 * - au milieu, les couches du réseau qui arrivent du fond, cadre après
 *   cadre, avec leurs graduations et leurs numéros, et leurs nœuds reliés de
 *   couche en couche, où filent des paquets ;
 * - devant, le réticule (dix segments, un par dixième de batterie, et SYNC),
 *   les règles graduées des bords et les données qui défilent.
 *
 * En charge, la plongée accélère et le centre pulse. Avec du réseau, les
 * paquets filent sur toutes les liaisons et les cascades tombent plus vite.
 *
 * Les cotes sont celles de la maquette : un centième de la largeur, et une
 * hauteur de 216 unités ramenée à celle de l'écran par [y].
 */
class GhostCore(kit: CoreKit) : CoreArt(kit) {

    override val wholePhone get() = true

    private val scale = kit.h / 216f
    private fun x(v: Float) = v * u
    private fun y(v: Float) = v * scale * u
    private fun p(px: Float, py: Float) = PointF(x(px), y(py))

    private val vx = 50f
    private val vy = 100f
    private val vanishing = p(vx, vy)
    private val layers = 9

    private class Column(val x: Float, val depth: Float, val speed: Float, val length: Int, val seed: Int)

    /** Les cascades : des colonnes de signes à des profondeurs différentes. */
    private val columns = (0 until 28).map {
        Column(5f + noise(it * 5) * 90f, noise(it * 5 + 1), 6f + noise(it * 5 + 2) * 14f, 6 + (noise(it * 5 + 3) * 12f).toInt(), (noise(it * 5 + 4) * 1000f).toInt())
    }

    /** Les nœuds de chaque couche, en fractions de sa demi-largeur et de sa demi-hauteur. */
    private val nodes = (0 until layers).map { k -> (0 until 5).map { j -> floatArrayOf(noise(k * 31 + j * 2) * 2f - 1f, noise(k * 31 + j * 2 + 1) * 2f - 1f) } }

    private val mono: Typeface = Typeface.MONOSPACE
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { typeface = mono }

    private fun seg(canvas: Canvas, x0: Float, y0: Float, x1: Float, y1: Float, paint: Paint) =
        canvas.drawLine(x(x0), y(y0), x(x1), y(y1), paint)

    // ——— Au fond : les rayons du tunnel ———

    override fun drawChassis(canvas: Canvas, line: Paint, thin: Paint, fill: Paint) {
        val ray = Paint(thin).apply { strokeWidth = 0.5f * density; alpha = 90 }
        for (k in 0 until 16) {
            val a = k / 16f * 2f * PI.toFloat()
            seg(canvas, vx, vy, vx + cos(a) * 80f, vy + sin(a) * 160f, ray)
        }
    }

    // ——— Devant : les règles, les coins, l'anneau fixe du réticule ———

    override fun drawBoard(canvas: Canvas, line: Paint, thin: Paint, fill: Paint) {
        val fine = Paint(thin).apply { strokeWidth = 0.5f * density }
        val faint = Paint(fine).apply { alpha = 110 }
        var ry = 20f
        while (ry < 200f) {
            val big = ry.roundToInt() % 10 == 0
            val paint = if (big) fine else faint
            seg(canvas, 7f, ry, if (big) 9.5f else 8.2f, ry, paint)
            seg(canvas, 93f, ry, if (big) 90.5f else 91.8f, ry, paint)
            ry += 2f
        }
        for ((cx, cy, dx, dy) in listOf(floatArrayOf(8f, 14f, 1f, 1f), floatArrayOf(92f, 14f, -1f, 1f), floatArrayOf(8f, 204f, 1f, -1f), floatArrayOf(92f, 204f, -1f, -1f))) {
            seg(canvas, cx, cy + dy * 5f, cx, cy, thin)
            seg(canvas, cx, cy, cx + dx * 5f, cy, thin)
        }
        // Le réticule : le cercle intérieur et la croix.
        canvas.drawCircle(vanishing.x, vanishing.y, x(RING * 0.55f), faint)
        seg(canvas, vx - RING - 8f, vy, vx - RING * 0.7f, vy, faint)
        seg(canvas, vx + RING * 0.7f, vy, vx + RING + 8f, vy, faint)
        seg(canvas, vx, vy - (RING + 8f) / scale, vx, vy - RING * 0.7f / scale, faint)
        seg(canvas, vx, vy + RING * 0.7f / scale, vx, vy + (RING + 8f) / scale, faint)
    }

    override fun drawStatic(canvas: Canvas, line: Paint, thin: Paint, fill: Paint) = Unit

    override fun outlines(): List<Path> =
        listOf(0.25f, 0.45f, 0.65f, 0.85f).map { z ->
            val f = 0.04f + 0.96f * z.pow(2.4f)
            Path().apply { addRect(x(vx - 44f * f), y(vy - 96f * f), x(vx + 44f * f), y(vy + 96f * f), Path.Direction.CW) }
        } + Path().apply { addCircle(vanishing.x, vanishing.y, x(RING), Path.Direction.CW) }

    override val origin get() = vanishing

    /** Le réseau : des coins vers le point de fuite (la plongée) ; la charge : du point de fuite vers le bas ; le battement : autour du réticule. */
    override fun routes() = Triple(
        listOf(p(8f, 14f), p(92f, 14f), p(8f, 204f), p(92f, 204f)).map { CircuitScene.Route(listOf(it, vanishing)) },
        listOf(CircuitScene.Route(listOf(vanishing, p(vx, 204f)))),
        (0 until 4).map { k ->
            CircuitScene.Route((0..12).map { i -> val a = (k / 4f + i / 48f) * 2f * PI.toFloat(); PointF(vanishing.x + cos(a) * x(RING + 6f), vanishing.y + sin(a) * x(RING + 6f)) })
        },
    )

    // ——— Ce qui vit ———

    private fun text(canvas: Canvas, ink: Ink, str: String, px: Float, py: Float, size: Float, a: Float, align: Paint.Align = Paint.Align.LEFT, bright: Boolean = false) {
        if (a <= 0.02f) return
        textPaint.textSize = x(size)
        textPaint.textAlign = align
        textPaint.color = if (bright) ink.palette.core else ink.palette.line
        textPaint.alpha = (255 * min(1f, a)).toInt()
        canvas.drawText(str, x(px), y(py) + textPaint.textSize * 0.36f, textPaint)
    }

    private fun hash(n: Int) = noise(n * 1_103_515 + 12_345)

    /** Au fond : les cascades de chiffres et de katakanas, plus vite avec du réseau ou en charge. */
    override fun drawLiveChassis(canvas: Canvas, state: FrameState, ink: Ink) {
        val s = strength(state) * state.ignition.coerceIn(0f, 1f)
        // Décalé pour que les cascades remplissent déjà l'écran à la première image.
        val seconds = state.timeMillis / 1000f + 40f
        val fast = (if (state.pulses.isNotEmpty()) 1.8f else 1f) * (if (state.charging) 1.4f else 1f)
        columns.forEach { col ->
            val size = 1.6f + col.depth * 1.8f
            val step = size * 1.15f
            val span = 220f + col.length * step
            val head = (seconds * col.speed * fast * (0.5f + col.depth)) % span - col.length * step
            for (k in 0 until col.length) {
                val cy = head + k * step
                if (cy < 12f || cy > 206f) continue
                val glyph = GLYPHS[(hash(col.seed + k * 13 + floor(seconds * 6f + k).toInt()) * GLYPHS.length).toInt().coerceAtMost(GLYPHS.length - 1)]
                val last = k == col.length - 1
                val a = (if (last) 1f else k / col.length.toFloat() * 0.6f) * (0.25f + 0.6f * col.depth) * 0.75f * s
                text(canvas, ink, glyph.toString(), col.x, cy, size, a, Paint.Align.CENTER, last)
            }
        }
    }

    private val point = PointF()
    private val frameRect = RectF()

    private class Frame(val k: Int, val z: Float, val f: Float, val a: Float)

    private fun speed(state: FrameState) = if (state.charging) 0.22f else 0.09f

    /** Au milieu : les couches du réseau qui arrivent du fond, leurs nœuds et leurs paquets. */
    override fun drawLive(canvas: Canvas, state: FrameState, ink: Ink) {
        val s = strength(state) * state.ignition.coerceIn(0f, 1f)
        val seconds = state.timeMillis / 1000f
        val speed = speed(state)
        val net = state.pulses.isNotEmpty()
        val palette = ink.palette
        val frames = (0 until layers).map { k ->
            val z = (k / layers.toFloat() + seconds * speed) % 1f
            val a = min(1f, z * 2.5f) * if (z > 0.85f) (1f - z) / 0.15f else 1f
            Frame(k, z, 0.04f + 0.96f * z.pow(2.4f), a)
        }.sortedBy { it.z }
        val stroke = ink.stroke
        frames.forEach { fr ->
            val hx = 44f * fr.f
            val hy = 96f * fr.f
            stroke.color = palette.line
            stroke.alpha = (166 * fr.a * s).toInt()
            stroke.strokeWidth = (0.5f + fr.f * 1.2f) * density * 0.6f
            frameRect.set(x(vx - hx), y(vy - hy), x(vx + hx), y(vy + hy))
            canvas.drawRect(frameRect, stroke)
            stroke.alpha = (90 * fr.a * s).toInt()
            stroke.strokeWidth = 0.5f * density
            for (t in -4..4) {
                val tx = vx + t * hx / 4.5f
                seg(canvas, tx, vy - hy, tx, vy - hy + 1.5f * fr.f, stroke)
                seg(canvas, tx, vy + hy, tx, vy + hy - 1.5f * fr.f, stroke)
            }
            if (fr.f > 0.12f) {
                val label = (1000 + fr.k * 137 + floor(seconds * speed).toInt() * 11) % 10000
                text(canvas, ink, label.toString().padStart(4, '0'), vx - hx + 1f, vy - hy + 2.5f * fr.f, 1f + 2.4f * fr.f, 0.7f * fr.a * s)
            }
        }
        fun at(fr: Frame, n: FloatArray, out: PointF) = out.set(x(vx + n[0] * 44f * fr.f * 0.85f), y(vy + n[1] * 96f * fr.f * 0.85f))
        val other = PointF()
        for (i in 0 until frames.size - 1) {
            val a = frames[i]
            val b = frames[i + 1]
            val fade = min(a.a, b.a) * s
            nodes[a.k].forEachIndexed { j, n ->
                at(a, n, point)
                at(b, nodes[b.k][(j * 2 + 1) % 5], other)
                stroke.color = palette.line
                stroke.alpha = (76 * fade).toInt()
                stroke.strokeWidth = 0.5f * density
                canvas.drawLine(point.x, point.y, other.x, other.y, stroke)
                if (net || (j + i) % 3 == 0) {
                    val t = (seconds * (if (net) 1.2f else 0.5f) + j * 0.3f + i * 0.17f) % 1f
                    val px = point.x + (other.x - point.x) * t
                    val py = point.y + (other.y - point.y) * t
                    ink.halo(canvas, PointF(px, py), x(2.1f), (130 * fade).toInt())
                    ink.fill.color = palette.core
                    ink.fill.alpha = (204 * fade).toInt()
                    canvas.drawCircle(px, py, x(0.25f + 0.3f * b.f), ink.fill)
                }
            }
            nodes[a.k].forEach { n ->
                at(a, n, point)
                stroke.color = palette.core
                stroke.alpha = (204 * a.a * s).toInt()
                stroke.strokeWidth = 0.6f * density
                canvas.drawCircle(point.x, point.y, x(0.3f + a.f * 0.9f), stroke)
            }
        }
        // Le centre : le point de fuite, qui pulse en charge.
        val pulse = if (state.charging) (sin(seconds * 5f) + 1f) / 2f else 0.3f
        ink.halo(canvas, vanishing, x(14f), ((50 + 76 * pulse) * s).toInt())
    }

    /** Devant : le réticule et la batterie, les données qui défilent. */
    override fun drawLiveBoard(canvas: Canvas, state: FrameState, ink: Ink) {
        val s = strength(state) * state.ignition.coerceIn(0f, 1f)
        val seconds = state.timeMillis / 1000f
        val palette = ink.palette
        val stroke = ink.stroke
        val battery = state.batteryLevel.coerceIn(0f, 1f)
        val lit = ceil(battery * 10f).toInt().coerceIn(1, 10)
        val breath = if (state.charging) (sin(seconds * 4.5f) + 1f) / 2f else 1f
        val rr = x(RING)
        frameRect.set(vanishing.x - rr, vanishing.y - rr, vanishing.x + rr, vanishing.y + rr)
        for (k in 0 until 10) {
            val on = k < lit
            val a = if (on) (if (k == lit - 1) 0.35f + 0.65f * breath else 1f) else 0.2f
            stroke.color = if (on) palette.core else palette.line
            stroke.alpha = (255 * a * s).toInt()
            stroke.strokeWidth = (if (on) 1.6f else 1f) * density
            canvas.drawArc(frameRect, -90f + k * 36f + 3f, 30f, false, stroke)
        }
        // L'anneau gradué qui tourne, et deux arcs en sens inverse.
        val spin = seconds * if (state.charging) 1.2f else 0.4f
        stroke.color = palette.line
        stroke.alpha = (150 * s).toInt()
        stroke.strokeWidth = 0.6f * density
        for (k in 0 until 36) {
            val a = spin + k * PI.toFloat() / 18f
            val r0 = x(RING + 2f)
            val r1 = x(RING + if (k % 3 != 0) 2.8f else 3.8f)
            canvas.drawLine(vanishing.x + cos(a) * r0, vanishing.y + sin(a) * r0, vanishing.x + cos(a) * r1, vanishing.y + sin(a) * r1, stroke)
        }
        val outer = x(RING + 6f)
        frameRect.set(vanishing.x - outer, vanishing.y - outer, vanishing.x + outer, vanishing.y + outer)
        stroke.alpha = (204 * s).toInt()
        stroke.strokeWidth = 0.8f * density
        val start = (-spin * 1.5f * 180f / PI.toFloat()) % 360f
        canvas.drawArc(frameRect, start, 69f, false, stroke)
        canvas.drawArc(frameRect, start + 180f, 69f, false, stroke)
        text(canvas, ink, "SYNC ${(battery * 100).roundToInt().toString().padStart(3, '0')}%", vx, vy + (RING + 11f) / scale, 2.8f, 0.9f * s, Paint.Align.CENTER, bright = true)

        // Les données du HUD.
        val speed = speed(state)
        text(canvas, ink, "NET.DIVE", 12f, 22f, 3f, 0.9f * s, bright = true)
        text(canvas, ink, "DEPTH ${((seconds * speed * 1000f).toLong() % 10000).toString().padStart(4, '0')}", 12f, 26.5f, 2.6f, 0.8f * s)
        text(canvas, ink, "%.4fN".format(java.util.Locale.ROOT, 35.68f + sin(seconds * 0.1f) * 0.01f), 88f, 22f, 2.6f, 0.8f * s, Paint.Align.RIGHT)
        text(canvas, ink, "%.4fE".format(java.util.Locale.ROOT, 139.76f + cos(seconds * 0.1f) * 0.01f), 88f, 26.5f, 2.6f, 0.8f * s, Paint.Align.RIGHT)
        val tick = floor(seconds * 4f).toInt()
        for (k in 0 until 6) {
            val hex = CharArray(8) { i -> HEX[(hash(k * 31 + i + tick) * 16f).toInt().coerceAtMost(15)] }
            text(canvas, ink, String(hex), 12f, 172f + k * 3.4f, 2.4f, 0.5f * s)
        }
    }

    private companion object {
        /** Le rayon du réticule, en unités. */
        const val RING = 10f
        const val GLYPHS = "0123456789ｱｲｳｴｵｶｷｸｹｺｻｼｽｾｿﾀﾁﾂﾃﾄﾅﾆﾇﾈﾉ"
        const val HEX = "0123456789ABCDEF"
    }
}
