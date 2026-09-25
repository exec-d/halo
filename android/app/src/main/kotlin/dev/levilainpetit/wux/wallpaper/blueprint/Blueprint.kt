package dev.levilainpetit.wux.wallpaper.blueprint

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RadialGradient
import android.graphics.Shader
import android.graphics.Typeface
import dev.levilainpetit.wux.wallpaper.LiveFrame
import dev.levilainpetit.wux.wallpaper.LiveScene
import dev.levilainpetit.wux.wallpaper.SceneKit
import java.time.LocalDate
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.min
import kotlin.math.sin

/** Un objet de film dessiné en plan technique. */
interface Subject {
    /** Le nom de l'objet, en tête du cartouche. */
    val title: String

    /** Le film, et une référence de pièce. */
    val reference: String

    /** L'échelle notée au cartouche. */
    val scale: String

    /** Le numéro de la planche. */
    val sheet: Int

    /** Délai entre deux images une fois le plan tracé (ms). */
    val frame: Long get() = 66L

    /**
     * Dessine les vues de l'objet dans la zone du plan ([top] à [bottom]), sur
     * toute la largeur [width]. [time] en secondes, inclinaison de -1 à 1.
     */
    fun draw(pen: Pen, width: Float, top: Float, bottom: Float, time: Float, tiltX: Float, tiltY: Float)
}

/**
 * Un fond d'écran en plan technique, aux couleurs du téléphone : papier
 * quadrillé, cadre repéré, cartouche, et l'objet de [subject]. À chaque
 * allumage de l'écran, le plan se trace trait par trait ; ses pièces mobiles
 * s'animent ; les vues glissent un peu avec l'inclinaison.
 */
class BlueprintScene(private val subject: Subject) : LiveScene {

    override val colors: IntArray? get() = own

    private var own: IntArray? = null
    private var width = 1
    private var height = 1
    private var density = 1f
    private var last = 0L
    private var time = 0f
    private var reveal = 1f
    private var wasLocked = false
    private var lastTotal = 1f
    private val pen = Pen()
    private val grid = Paint().apply { style = Paint.Style.STROKE }
    private val vignette = Paint()
    private var lockShade = Paint()

    override fun resize(width: Int, height: Int, density: Float) {
        this.width = width
        this.height = height
        this.density = density
        pen.density = density
        vignette.shader = RadialGradient(
            width / 2f, height * 0.5f, height * 0.7f,
            intArrayOf(Color.TRANSPARENT, Color.TRANSPARENT, Color.argb(150, 0, 0, 0)),
            floatArrayOf(0f, 0.6f, 1f), Shader.TileMode.CLAMP,
        )
        lockShade = SceneKit.lockShade(height)
    }

    override fun draw(canvas: Canvas, frame: LiveFrame) {
        val dt = SceneKit.step(last, frame.timeMillis)
        last = frame.timeMillis
        time += dt
        // L'écran s'allume sur l'écran de verrouillage : le plan se retrace.
        if (frame.locked && !wasLocked) reveal = 0f
        wasLocked = frame.locked
        if (reveal < 1f) reveal = min(1f, reveal + dt / REVEAL)

        val strength = SceneKit.strength(frame)
        val palette = frame.palette
        val paper = SceneKit.night(palette, 0.07f)
        val ink = SceneKit.mix(palette.line, palette.core, 0.35f)
        val glow = SceneKit.neon(palette)
        own = intArrayOf(glow, ink, paper)

        canvas.drawColor(paper)
        drawPaper(canvas, frame, ink, strength)

        pen.begin(canvas, ink, glow, strength, if (reveal >= 1f) Float.MAX_VALUE else lastTotal * ease(reveal))
        val w = width.toFloat()
        val top = height * 0.3f
        val bottom = height * 0.83f
        subject.draw(pen, w, top, bottom, time, frame.tiltX, frame.tiltY)
        drawTitleBlock()
        lastTotal = pen.total.coerceAtLeast(1f)

        canvas.drawRect(0f, 0f, w, height.toFloat(), vignette)
        if (frame.locked) canvas.drawRect(0f, 0f, w, height * 0.34f, lockShade)
    }

    override fun nextFrameIn(frame: LiveFrame): Long = if (reveal < 1f) 33L else subject.frame

    private fun ease(t: Float) = 1f - (1f - t) * (1f - t)

    /** Le papier : quadrillage fin, quadrillage fort, cadre et repères. */
    private fun drawPaper(canvas: Canvas, frame: LiveFrame, ink: Int, strength: Float) {
        val step = 12f * density
        val dx = -frame.tiltX * 4f * density
        val dy = -frame.tiltY * 4f * density
        grid.color = ink
        var i = 0
        var x = (dx % step) - step
        while (x < width + step) {
            grid.alpha = ((if (i % 5 == 0) 26 else 10) * strength).toInt()
            grid.strokeWidth = if (i % 5 == 0) 0.8f * density else 0.5f * density
            canvas.drawLine(x, 0f, x, height.toFloat(), grid)
            x += step
            i++
        }
        i = 0
        var y = (dy % step) - step
        while (y < height + step) {
            grid.alpha = ((if (i % 5 == 0) 26 else 10) * strength).toInt()
            grid.strokeWidth = if (i % 5 == 0) 0.8f * density else 0.5f * density
            canvas.drawLine(0f, y, width.toFloat(), y, grid)
            y += step
            i++
        }
        // Le cadre, double, et ses repères de zones.
        val m = 14f * density
        grid.alpha = (120 * strength).toInt()
        grid.strokeWidth = 1.2f * density
        canvas.drawRect(m, m, width - m, height - m, grid)
        grid.alpha = (60 * strength).toInt()
        grid.strokeWidth = 0.6f * density
        canvas.drawRect(m + 5 * density, m + 5 * density, width - m - 5 * density, height - m - 5 * density, grid)
        val text = pen.textPaint(7f, ink, (90 * strength).toInt())
        val zones = 6
        for (k in 0 until zones) {
            val zx = m + (width - 2 * m) * (k + 0.5f) / zones
            canvas.drawText("${k + 1}", zx, m - 3 * density, text)
            canvas.drawText("${k + 1}", zx, height - m + 9 * density, text)
            if (k > 0) {
                val tx = m + (width - 2 * m) * k / zones
                canvas.drawLine(tx, m, tx, m + 5 * density, grid)
                canvas.drawLine(tx, height - m, tx, height - m - 5 * density, grid)
            }
        }
        val rows = 8
        for (k in 0 until rows) {
            val zy = m + (height - 2 * m) * (k + 0.5f) / rows + 3 * density
            val letter = ('A' + k).toString()
            canvas.drawText(letter, m / 2, zy, text)
            canvas.drawText(letter, width - m / 2, zy, text)
        }
    }

    /** Le cartouche, en bas à droite. */
    private fun drawTitleBlock() {
        val m = 19f * density
        val right = width - m
        val bottom = height - m
        val left = width * 0.3f
        val top = bottom - 64f * density
        val split = top + 26f * density
        val mid = top + 45f * density
        pen.screen {
            pen.rect(left, top, right, bottom, Weight.MAIN)
            pen.line(left, split, right, split, Weight.THIN)
            pen.line(left, mid, right, mid, Weight.HAIR)
            val c1 = left + (right - left) * 0.36f
            val c2 = left + (right - left) * 0.68f
            pen.line(c1, mid, c1, bottom, Weight.HAIR)
            pen.line(c2, mid, c2, bottom, Weight.HAIR)
            pen.text(left + 8 * density, top + 13 * density, subject.title, 10.5f, Paint.Align.LEFT, bold = true)
            pen.text(left + 8 * density, top + 22 * density, subject.reference, 6.5f, Paint.Align.LEFT)
            pen.text(left + 8 * density, split + 12 * density, "HALO · DRAWING OFFICE", 6.5f, Paint.Align.LEFT)
            val date = LocalDate.now()
            val cells = listOf("SCALE ${subject.scale}", "SHEET %02d/10".format(subject.sheet), "%02d.%02d.%d".format(date.dayOfMonth, date.monthValue, date.year))
            val xs = listOf(left, c1, c2, right)
            for ((k, cell) in cells.withIndex()) {
                pen.text((xs[k] + xs[k + 1]) / 2, mid + 12 * density, cell, 6.5f, Paint.Align.CENTER)
            }
        }
    }

    private companion object {
        /** Durée du tracé (s). */
        const val REVEAL = 2.4f
    }
}

/** L'épaisseur d'un trait, et son éclat. */
enum class Weight(val width: Float, val alpha: Int) {
    THICK(1.8f, 255),
    MAIN(1.2f, 235),
    THIN(0.8f, 180),
    HAIR(0.55f, 130),
}

/**
 * Le crayon du plan : des traits, des cotes, des hachures, des repères, en
 * coordonnées de la vue courante (voir [view] et [fit]). Chaque trait
 * « coûte » sa longueur ; pendant le tracé, seuls ceux que le budget permet
 * sont dessinés, le dernier en partie.
 */
class Pen {
    var density = 1f
    private lateinit var canvas: Canvas
    private var ink = Color.WHITE
    private var glow = Color.WHITE
    private var strength = 1f
    private var budget = Float.MAX_VALUE

    /** Longueur totale des traits de l'image (px). */
    var total = 0f
        private set

    private var ox = 0f
    private var oy = 0f
    private var k = 1f

    private val stroke = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
    }
    private val fill = Paint(Paint.ANTI_ALIAS_FLAG)
    private val label = Paint(Paint.ANTI_ALIAS_FLAG).apply { typeface = Typeface.MONOSPACE }
    private val bold = Paint(Paint.ANTI_ALIAS_FLAG).apply { typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD) }

    fun begin(canvas: Canvas, ink: Int, glow: Int, strength: Float, budget: Float) {
        this.canvas = canvas
        this.ink = ink
        this.glow = glow
        this.strength = strength
        this.budget = budget
        total = 0f
        view(0f, 0f, 1f)
    }

    /** L'origine de la vue à l'écran (px) et l'échelle (px par unité). */
    fun view(left: Float, top: Float, scale: Float) {
        ox = left
        oy = top
        k = scale
    }

    /** Cadre une vue de [w] × [h] unités au centre de la zone donnée (px). */
    fun fit(w: Float, h: Float, left: Float, top: Float, right: Float, bottom: Float) {
        val scale = min((right - left) / w, (bottom - top) / h)
        view(left + ((right - left) - w * scale) / 2, top + ((bottom - top) - h * scale) / 2, scale)
    }

    /** Dessine [block] en pixels d'écran, puis revient à la vue courante. */
    fun screen(block: () -> Unit) {
        val saved = Triple(ox, oy, k)
        view(0f, 0f, 1f)
        block()
        view(saved.first, saved.second, saved.third)
    }

    fun sx(x: Float) = ox + x * k
    fun sy(y: Float) = oy + y * k

    fun textPaint(size: Float, color: Int, alpha: Int) = label.apply {
        textSize = size * density
        this.color = color
        this.alpha = alpha
        textAlign = Paint.Align.CENTER
        letterSpacing = 0.08f
    }

    /** La part d'un trait de [length] px que le budget permet de dessiner. */
    private fun spend(length: Float): Float {
        val start = total
        total += length
        if (budget == Float.MAX_VALUE) return 1f
        val left = budget - start
        return if (left <= 0f) 0f else min(1f, left / length)
    }

    private fun prepare(weight: Weight, color: Int = ink) {
        stroke.color = color
        stroke.alpha = (weight.alpha * strength).toInt()
        stroke.strokeWidth = weight.width * density
        stroke.pathEffect = null
    }

    fun line(x0: Float, y0: Float, x1: Float, y1: Float, weight: Weight = Weight.MAIN) {
        val ax = sx(x0)
        val ay = sy(y0)
        val bx = sx(x1)
        val by = sy(y1)
        val part = spend(hypot(bx - ax, by - ay))
        if (part <= 0f) return
        prepare(weight)
        canvas.drawLine(ax, ay, ax + (bx - ax) * part, ay + (by - ay) * part, stroke)
    }

    /** Une ligne brisée : x0, y0, x1, y1… */
    fun poly(points: FloatArray, closed: Boolean = false, weight: Weight = Weight.MAIN) {
        val n = points.size / 2
        for (i in 0 until n - 1) line(points[i * 2], points[i * 2 + 1], points[i * 2 + 2], points[i * 2 + 3], weight)
        if (closed && n > 2) line(points[n * 2 - 2], points[n * 2 - 1], points[0], points[1], weight)
    }

    /** Une ligne brisée, en miroir autour de x = [axis] (pour les vues symétriques). */
    fun mirror(points: FloatArray, axis: Float, closed: Boolean = false, weight: Weight = Weight.MAIN) {
        poly(points, closed, weight)
        poly(FloatArray(points.size) { if (it % 2 == 0) 2 * axis - points[it] else points[it] }, closed, weight)
    }

    fun rect(l: Float, t: Float, r: Float, b: Float, weight: Weight = Weight.MAIN) =
        poly(floatArrayOf(l, t, r, t, r, b, l, b), closed = true, weight = weight)

    fun arc(cx: Float, cy: Float, r: Float, start: Float, sweep: Float, weight: Weight = Weight.MAIN) {
        val radius = r * k
        val part = spend(radius * (Math.abs(sweep) * PI.toFloat() / 180f))
        if (part <= 0f) return
        prepare(weight)
        canvas.drawArc(android.graphics.RectF(sx(cx) - radius, sy(cy) - radius, sx(cx) + radius, sy(cy) + radius), start, sweep * part, false, stroke)
    }

    fun circle(cx: Float, cy: Float, r: Float, weight: Weight = Weight.MAIN) = arc(cx, cy, r, -90f, 360f, weight)

    /** Un axe : trait mixte, long-court. */
    fun axis(x0: Float, y0: Float, x1: Float, y1: Float) {
        val length = hypot(x1 - x0, y1 - y0)
        if (length <= 0f) return
        val dash = 14f * density / k
        val gap = 4f * density / k
        var t = 0f
        while (t < length) {
            val a = t / length
            val b = min(t + dash, length) / length
            line(x0 + (x1 - x0) * a, y0 + (y1 - y0) * a, x0 + (x1 - x0) * b, y0 + (y1 - y0) * b, Weight.HAIR)
            val c = min(t + dash + gap, length) / length
            val d = min(t + dash + gap + gap * 0.6f, length) / length
            if (c < 1f) line(x0 + (x1 - x0) * c, y0 + (y1 - y0) * c, x0 + (x1 - x0) * d, y0 + (y1 - y0) * d, Weight.HAIR)
            t += dash + gap * 2.6f
        }
    }

    /** Un trait caché : tirets courts. */
    fun hidden(x0: Float, y0: Float, x1: Float, y1: Float) {
        val length = hypot(x1 - x0, y1 - y0)
        if (length <= 0f) return
        val dash = 5f * density / k
        var t = 0f
        while (t < length) {
            val a = t / length
            val b = min(t + dash, length) / length
            line(x0 + (x1 - x0) * a, y0 + (y1 - y0) * a, x0 + (x1 - x0) * b, y0 + (y1 - y0) * b, Weight.HAIR)
            t += dash * 1.8f
        }
    }

    /**
     * Une cote entre deux points : lignes d'attache, ligne de cote décalée de
     * [offset] unités (vers la gauche du segment), flèches et valeur.
     */
    fun dim(x0: Float, y0: Float, x1: Float, y1: Float, offset: Float, value: String) {
        val length = hypot(x1 - x0, y1 - y0)
        if (length <= 0f) return
        val nx = -(y1 - y0) / length
        val ny = (x1 - x0) / length
        val ax = x0 + nx * offset
        val ay = y0 + ny * offset
        val bx = x1 + nx * offset
        val by = y1 + ny * offset
        val over = 4f * density / k * Math.signum(offset)
        line(x0 + nx * offset * 0.15f, y0 + ny * offset * 0.15f, ax + nx * over, ay + ny * over, Weight.HAIR)
        line(x1 + nx * offset * 0.15f, y1 + ny * offset * 0.15f, bx + nx * over, by + ny * over, Weight.HAIR)
        line(ax, ay, bx, by, Weight.HAIR)
        arrow(ax, ay, bx, by)
        arrow(bx, by, ax, ay)
        // La valeur, au milieu, dans le sens de la cote.
        val cx = sx((ax + bx) / 2)
        val cy = sy((ay + by) / 2)
        if (spend(30f) <= 0f) return
        var angle = Math.toDegrees(atan2((by - ay).toDouble(), (bx - ax).toDouble())).toFloat()
        if (angle > 90f || angle < -90f) angle += 180f
        canvas.save()
        canvas.rotate(angle, cx, cy)
        val paint = textPaint(7f, ink, (200 * strength).toInt())
        val lift = if (offset >= 0) 3f else -3f
        canvas.drawText(value, cx, cy - lift * density + (if (lift < 0) 7f * density else 0f), paint)
        canvas.restore()
    }

    private fun arrow(x0: Float, y0: Float, x1: Float, y1: Float) {
        val length = hypot(x1 - x0, y1 - y0)
        if (length <= 0f) return
        val ux = (x1 - x0) / length
        val uy = (y1 - y0) / length
        val size = 6f * density / k
        val wing = 2f * density / k
        line(x0, y0, x0 + ux * size - uy * wing, y0 + uy * size + ux * wing, Weight.THIN)
        line(x0, y0, x0 + ux * size + uy * wing, y0 + uy * size - ux * wing, Weight.THIN)
    }

    /** Des hachures à 45° dans un rectangle. */
    fun hatch(l: Float, t: Float, r: Float, b: Float, spacing: Float = 6f) {
        canvas.save()
        canvas.clipRect(sx(l), sy(t), sx(r), sy(b))
        val step = spacing * density / k
        var x = l - (b - t)
        while (x < r) {
            line(x, b, x + (b - t), t, Weight.HAIR)
            x += step
        }
        canvas.restore()
    }

    /** Un repère : un point, un trait de rappel et une bulle numérotée. */
    fun callout(x: Float, y: Float, tx: Float, ty: Float, number: Int) {
        val r = 7f * density / k
        val length = hypot(tx - x, ty - y)
        if (length <= r) return
        dot(x, y, 1.6f)
        line(x, y, tx - (tx - x) / length * r, ty - (ty - y) / length * r, Weight.HAIR)
        circle(tx, ty, r, Weight.THIN)
        text(tx, ty + 2.6f * density / k, "$number", 7f, Paint.Align.CENTER)
    }

    /** Un texte, en coordonnées de la vue ; [size] en dp. */
    fun text(x: Float, y: Float, value: String, size: Float, align: Paint.Align = Paint.Align.LEFT, bold: Boolean = false, alpha: Int = 210) {
        if (spend(value.length * size * density * 0.5f) <= 0f) return
        val paint = if (bold) this.bold else label
        paint.textSize = size * density
        paint.color = ink
        paint.alpha = (alpha * strength).toInt()
        paint.textAlign = align
        paint.letterSpacing = 0.08f
        canvas.drawText(value, sx(x), sy(y), paint)
    }

    /** Un point plein ; [size] en dp. */
    fun dot(x: Float, y: Float, size: Float, color: Int = ink) {
        if (spend(size * density * 2) <= 0f) return
        fill.shader = null
        fill.color = color
        fill.alpha = (230 * strength).toInt()
        canvas.drawCircle(sx(x), sy(y), size * density, fill)
    }

    // ——— Ce qui brille : les parties lumineuses de l'objet, en couleur vive ———

    /** Une ligne lumineuse ; [level] de 0 (éteinte) à 1. */
    fun glowLine(x0: Float, y0: Float, x1: Float, y1: Float, level: Float = 1f, width: Float = 1.6f) {
        val ax = sx(x0)
        val ay = sy(y0)
        val bx = sx(x1)
        val by = sy(y1)
        val part = spend(hypot(bx - ax, by - ay))
        if (part <= 0f || level <= 0f) return
        val ex = ax + (bx - ax) * part
        val ey = ay + (by - ay) * part
        for ((w, a) in GLOW) {
            stroke.color = glow
            stroke.alpha = (a * level * strength).toInt().coerceIn(0, 255)
            stroke.strokeWidth = (width + w) * density
            canvas.drawLine(ax, ay, ex, ey, stroke)
        }
    }

    fun glowPoly(points: FloatArray, closed: Boolean = false, level: Float = 1f, width: Float = 1.6f) {
        val n = points.size / 2
        for (i in 0 until n - 1) glowLine(points[i * 2], points[i * 2 + 1], points[i * 2 + 2], points[i * 2 + 3], level, width)
        if (closed && n > 2) glowLine(points[n * 2 - 2], points[n * 2 - 1], points[0], points[1], level, width)
    }

    fun glowArc(cx: Float, cy: Float, r: Float, start: Float, sweep: Float, level: Float = 1f, width: Float = 1.6f) {
        val radius = r * k
        val part = spend(radius * (Math.abs(sweep) * PI.toFloat() / 180f))
        if (part <= 0f || level <= 0f) return
        val oval = android.graphics.RectF(sx(cx) - radius, sy(cy) - radius, sx(cx) + radius, sy(cy) + radius)
        for ((w, a) in GLOW) {
            stroke.color = glow
            stroke.alpha = (a * level * strength).toInt().coerceIn(0, 255)
            stroke.strokeWidth = (width + w) * density
            canvas.drawArc(oval, start, sweep * part, false, stroke)
        }
    }

    /** Un halo lumineux autour d'un point ; [radius] en unités. */
    fun glowDot(x: Float, y: Float, radius: Float, level: Float = 1f) {
        if (spend(radius * k) <= 0f || level <= 0f) return
        val r = radius * k
        fill.shader = RadialGradient(
            sx(x), sy(y), r * 3f,
            intArrayOf(SceneKit.mix(glow, Color.WHITE, 0.6f), SceneKit.alpha(glow, (160 * level).toInt()), Color.TRANSPARENT),
            floatArrayOf(0f, 0.3f, 1f), Shader.TileMode.CLAMP,
        )
        fill.alpha = (255 * strength * level).toInt().coerceIn(0, 255)
        canvas.drawCircle(sx(x), sy(y), r * 3f, fill)
        fill.shader = null
    }

    /** Un aplat très léger, pour distinguer une pièce (x0, y0, x1, y1…). */
    fun shade(points: FloatArray, alpha: Int = 18) {
        if (spend(1f) <= 0f) return
        val path = android.graphics.Path()
        path.moveTo(sx(points[0]), sy(points[1]))
        for (i in 1 until points.size / 2) path.lineTo(sx(points[i * 2]), sy(points[i * 2 + 1]))
        path.close()
        fill.shader = null
        fill.color = ink
        fill.alpha = (alpha * strength).toInt()
        canvas.drawPath(path, fill)
    }

    /** Un point sur un cercle : angle en degrés, 0 à droite, sens horaire. */
    fun around(cx: Float, cy: Float, r: Float, degrees: Float): Pair<Float, Float> {
        val a = degrees * PI.toFloat() / 180f
        return (cx + cos(a) * r) to (cy + sin(a) * r)
    }

    private companion object {
        val GLOW = listOf(7f to 22, 3f to 60, 0f to 255)
    }
}
