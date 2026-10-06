package dev.levilainpetit.wux.wallpaper

import android.graphics.Canvas
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PointF
import android.graphics.RectF
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.hypot
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sign
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.random.Random

/**
 * Intelligence artificielle : le téléphone de Circuit, et à la place de la
 * batterie un accélérateur d'IA, un cerveau de silicium.
 *
 * - devant, la carte mère de Circuit, détaillée : les objectifs et leurs
 *   bagues, le processeur sous son blindage et sa mémoire, une puce
 *   graphique, le modem, le contrôleur d'alimentation, des pistes (en
 *   serpentin, en paires), des petits composants, des vias de couture ; ses
 *   deux nappes descendent vers l'accélérateur ; la carte du bas porte les
 *   bobines d'alimentation, le moteur, le port et le haut-parleur ;
 * - au milieu, le boîtier de l'accélérateur : son substrat, ses billes et ses
 *   condensateurs, l'interposeur, quatre piles de mémoire et, sur la puce, le
 *   cerveau gravé en pistes qui finissent sur des plots.
 *
 * Ce qui vit : une inférence, en boucle (plus vive en charge) : les mémoires
 * sont lues couche après couche, les données filent jusqu'au cerveau, une
 * vague part de sa ligne médiane jusqu'aux plots, puis la réponse remonte par
 * les nappes et les cœurs du processeur s'allument. Les plots allumés suivent
 * la batterie. En charge, les bobines s'éclairent et la ligne médiane pulse.
 *
 * Les cotes sont celles de la maquette : un centième de la largeur, et une
 * hauteur de 216 unités ramenée à celle de l'écran par [y].
 */
class NeuralCore(kit: CoreKit) : CoreArt(kit) {

    override val wholePhone get() = true

    private val scale = kit.h / 216f
    private fun x(v: Float) = v * u
    private fun y(v: Float) = v * scale * u
    private fun p(px: Float, py: Float) = PointF(x(px), y(py))
    private fun r(l: Float, t: Float, rt: Float, b: Float) = RectF(x(l), y(t), x(rt), y(b))

    /** Un point en unités, avant d'être porté à l'écran. */
    private data class U(val x: Float, val y: Float)

    private fun List<U>.px() = map { p(it.x, it.y) }

    /** Une piste à 45° : verticale (ou horizontale), diagonale, puis droite jusqu'au but. */
    private fun route(a: U, b: U, vertical: Boolean = true): List<U> {
        val dx = b.x - a.x
        val dy = b.y - a.y
        val d = min(abs(dx), abs(dy))
        val sx = sign(dx)
        val sy = sign(dy)
        return if (vertical) {
            val m = U(a.x, b.y - sy * d)
            listOf(a, m, U(m.x + sx * d, b.y), b)
        } else {
            val m = U(b.x - sx * d, a.y)
            listOf(a, m, U(b.x, m.y + sy * d), b)
        }
    }

    /** Un serpentin d'accord de longueur, sur un segment horizontal ou vertical. */
    private fun meander(a: U, b: U, amp: Float, pitch: Float, side: Float): List<U> {
        val horizontal = a.y == b.y
        val length = if (horizontal) b.x - a.x else b.y - a.y
        val s = sign(length)
        val n = floor(abs(length) / pitch).toInt()
        val out = mutableListOf(a)
        for (i in 0 until n) {
            val o0 = (if (horizontal) a.x else a.y) + s * i * pitch
            val o1 = o0 + s * pitch / 2f
            val k = (if (i % 2 == 1) -1f else 1f) * amp * side
            if (horizontal) out += listOf(U(o0, a.y), U(o0, a.y + k), U(o1, a.y + k), U(o1, a.y))
            else out += listOf(U(a.x, o0), U(a.x + k, o0), U(a.x + k, o1), U(a.x, o1))
        }
        out += b
        return out
    }

    // ——— Les cotes, en unités ———

    private val top = 0.045f * 216f
    private val boardU = floatArrayOf(7f, top, 93f, 0.36f * 216f)
    private val plateU = floatArrayOf(22f, 0.40f * 216f, 90f, 0.80f * 216f)
    private val bottomU = floatArrayOf(7f, 0.84f * 216f, 93f, 0.955f * 216f)
    private val bt = bottomU[1]
    private val bb = boardU[3]

    private val pillU = floatArrayOf(58.5f, top + 4.5f, 91f, top + 21.5f)
    private val cams = listOf(Triple(82f, top + 13f, 7.5f), Triple(66f, top + 13f, 5.5f))
    private val flashU = U(56.5f, top + 17.5f)
    private val socU = floatArrayOf(29f, top + 19f, 51f, top + 41f)
    private val ramU = floatArrayOf(29f, top + 44f, 51f, top + 52f)
    private val gpuU = floatArrayOf(10f, top + 20f, 24f, top + 34f)
    private val modemU = floatArrayOf(74f, top + 46f, 88f, top + 60f)
    private val pmicU = floatArrayOf(58f, top + 50f, 68f, top + 60f)
    private val connU = floatArrayOf(10f, bb - 8f, 22f, bb - 4f)

    private val interU = floatArrayOf(25f, 92f, 87f, 160f)
    private val dieU = floatArrayOf(41f, 97f, 71f, 155f)
    private val hbmU = listOf(
        floatArrayOf(27.5f, 95f, 37f, 124f), floatArrayOf(27.5f, 128f, 37f, 157f),
        floatArrayOf(75f, 95f, 84.5f, 124f), floatArrayOf(75f, 128f, 84.5f, 157f),
    )
    private val cx = 56f
    private val cy = 126f
    private val rx = 13f
    private val ry = 24f
    private fun half(py: Float) = rx * sqrt(max(0f, 1f - ((py - cy) / ry).pow(2)))
    private fun topAt(px: Float) = cy - ry * sqrt(max(0f, 1f - ((px - cx) / rx).pow(2))) - 1f

    private val ribbonsU = listOf(40f, 63.5f)
    private val coilsU = listOf(U(23f, bt + 9.5f), U(32.5f, bt + 9.5f))
    private val motorU = floatArrayOf(38f, bt + 3.5f, 58f, bt + 12f)
    private val usbU = floatArrayOf(40f, bottomU[3] - 7f, 60f, bottomU[3] - 1.5f)

    private fun rect(a: FloatArray) = r(a[0], a[1], a[2], a[3])

    // ——— Le cerveau ———

    /** Ses deux hémisphères, aux circonvolutions douces. */
    private val lobes: List<List<PointF>> = listOf(-1f, 1f).map { side ->
        (0..72).map { i ->
            val a = -PI.toFloat() / 2f + i / 72f * PI.toFloat()
            val k = (1f + 0.018f * sin(a * 13f + 0.6f) + 0.012f * sin(a * 5f)) * (1f - 0.06f * max(0f, -sin(a)).pow(2))
            p(cx + side * (0.7f + rx * cos(a) * k), cy + ry * sin(a) * k)
        }
    }

    /** Ses plis, en pointillés. */
    private val folds: List<List<PointF>> = listOf(-1f, 1f).flatMap { side ->
        listOf(0.62f, 0.36f).mapIndexed { n, f ->
            (4..68).map { i ->
                val a = -PI.toFloat() / 2f + i / 72f * PI.toFloat()
                val k = f * (1f + 0.06f * sin(a * 11f + n * 2f))
                p(cx + side * (0.7f + rx * cos(a) * k), cy + ry * sin(a) * k)
            }
        }
    }

    /** Ses pistes, de la ligne médiane jusqu'à un plot, et leurs plots (rangés autour du contour). */
    private val paths: List<List<PointF>>
    private val pads: List<PointF>

    // ——— Les bus ———

    /** Des mémoires vers les flancs du cerveau, six lignes chacune. */
    private val memoryBus: List<List<List<PointF>>>

    /** Des nappes vers le haut du cerveau : de la mémoire (en serpentin) et du contrôleur d'alimentation. */
    private val upBus: List<List<PointF>>

    /** Le faisceau du bord, du connecteur à la carte du bas : le réseau de Circuit. */
    private val side: List<List<PointF>>

    /** Les larges pistes des bobines jusqu'à la platine. */
    private val power: List<List<PointF>>

    // ——— Le décor de la carte mère ———

    private val traces: List<List<PointF>>
    private val smd: List<Triple<PointF, Boolean, Float>>
    private val vias: List<PointF>
    private val caps: List<Pair<PointF, Boolean>>
    private val balls: List<PointF>

    /** Les cœurs du processeur et les cellules de la puce graphique : position et rythme. */
    private val cores: List<Pair<PointF, Float>>
    private val cells: List<Pair<PointF, Float>>

    init {
        val random = Random(2024)

        val brainPaths = mutableListOf<List<U>>()
        val brainPads = mutableListOf<U>()
        for (i in 0 until 17) {
            val py = cy - ry * 0.86f + i / 16f * ry * 1.72f
            for (s in listOf(-1f, 1f)) {
                val w = half(py)
                val x0 = cx + s * 1.2f
                val xa = cx + s * w * (0.18f + random.nextFloat() * 0.3f)
                val up = if (random.nextFloat() < 0.5f) -1f else 1f
                val d = w * (0.15f + random.nextFloat() * 0.2f)
                var y2 = py + up * d
                if (abs(y2 - cy) > ry * 0.9f) y2 = py - up * d
                val xe = cx + s * half(y2) * 0.86f
                val xb = xa + s * abs(y2 - py)
                val pts = if (abs(xe - cx) > abs(xb - cx) + 0.4f) listOf(U(x0, py), U(xa, py), U(xb, y2), U(xe, y2)) else listOf(U(x0, py), U(xe, py))
                brainPaths += pts
                brainPads += pts.last()
                // Une branche sur certaines pistes : un plot plus près du centre.
                if (random.nextFloat() < 0.6f && pts.size == 4) {
                    val m = U((xa + xb) / 2f, (py + y2) / 2f)
                    val yb = m.y - up * 2.4f
                    val wb = half(yb) * 0.6f
                    if (wb > abs(m.x - cx) + 1f) {
                        val branch = listOf(m, U(m.x + s * 2.4f, yb), U(cx + s * wb, yb))
                        brainPaths += branch
                        brainPads += branch.last()
                    }
                }
            }
        }
        paths = brainPaths.map { it.px() }
        pads = brainPads.sortedBy { atan2(it.y - cy, it.x - cx) }.map { p(it.x, it.y) }

        memoryBus = hbmU.mapIndexed { k, (x0, y0, x1, y1) ->
            val left = k < 2
            (0 until 6).map { i ->
                val py = y0 + 6f + i * ((y1 - y0 - 12f) / 5f)
                val ty = cy + (py - cy) * 0.82f
                val e = cx + (if (left) -1f else 1f) * (half(ty) + 0.9f)
                val edge = if (left) dieU[0] + 1.5f else dieU[2] - 1.5f
                (listOf(U(if (left) x1 else x0, py), U(edge, py)) + route(U(edge, py), U(e, ty), vertical = false).drop(1)).px()
            }
        }

        upBus = (0 until 6).map { i ->
            val x0 = 37f + i * 1.2f
            val tx = 48.6f + i * 1.25f
            val path = mutableListOf(U(x0, ramU[3]))
            if (i % 2 == 0) path += meander(U(x0, ramU[3] + 3f), U(x0, ramU[3] + 12f), 0.45f, 1.1f, if (i % 4 != 0) -1f else 1f)
            else path += U(x0, ramU[3] + 12f)
            path += route(U(x0, plateU[1] + 4f), U(tx, topAt(tx)))
            path.px()
        } + (0 until 6).map { i ->
            val x0 = 60.5f + i * 1.2f
            val tx = 57.2f + i * 1.25f
            (listOf(U(x0, pmicU[3])) + route(U(x0, plateU[1] + 4f), U(tx, topAt(tx)))).px()
        }

        side = (0 until 4).map { i ->
            val sx = connU[0] + 2f + i * 2.6f
            val lx = 11.2f + i * 1.5f
            listOf(
                U(sx, connU[3]), U(sx, connU[3] + 1.5f), U(lx, connU[3] + 1.5f + abs(sx - lx)),
                U(lx, bt - 1.5f - i * 0.6f), U(lx + 2f + i * 0.6f, bt + 0.5f),
            ).px()
        }

        power = coilsU.mapIndexed { i, c -> route(U(c.x, c.y - 4f), U(29f + i * 4.6f, plateU[3] - 1f)).px() }

        // Les petits composants, là où la carte mère est libre.
        val busy = listOf(
            floatArrayOf(pillU[0] - 1.5f, pillU[1] - 1.5f, pillU[2] + 1.5f, pillU[3] + 1.5f),
            floatArrayOf(socU[0] - 4.5f, socU[1] - 4.5f, socU[2] + 4.5f, ramU[3] + 3.5f),
            floatArrayOf(gpuU[0] - 2.5f, gpuU[1] - 2.5f, gpuU[2] + 2.5f, gpuU[3] + 2.5f),
            floatArrayOf(modemU[0] - 2.5f, modemU[1] - 2.5f, modemU[2] + 2.5f, modemU[3] + 2.5f),
            floatArrayOf(pmicU[0] - 2.5f, pmicU[1] - 2.5f, pmicU[2] + 2.5f, pmicU[3] + 2.5f),
            floatArrayOf(connU[0] - 1f, connU[1] - 1.5f, connU[2] + 1f, bb),
            floatArrayOf(53.5f, 22f, 59.5f, 30f),
            floatArrayOf(8f, gpuU[3] + 2f, 30f, gpuU[3] + 9.5f),
            floatArrayOf(35f, ramU[3], 45f, bb),
            floatArrayOf(58f, pmicU[3], 69f, bb),
            floatArrayOf(8f, bb - 5f, 18f, bb),
        )
        fun free(px: Float, py: Float, m: Float) = busy.none { (a, b, c, d) -> px > a - m && px < c + m && py > b - m && py < d + m }
        val parts = mutableListOf<Triple<U, Boolean, Float>>()
        var tries = 0
        while (tries++ < 900 && parts.size < 58) {
            val px = 9f + random.nextFloat() * 82f
            val py = boardU[1] + 2.4f + random.nextFloat() * (bb - boardU[1] - 5f)
            val vertical = random.nextFloat() < 0.5f
            val length = if (random.nextFloat() < 0.35f) 1.5f else 1f
            if (free(px, py, 1.6f) && parts.all { hypot(it.first.x - px, it.first.y - py) > 2.4f }) parts += Triple(U(px, py), vertical, length)
        }
        smd = parts.map { Triple(p(it.first.x, it.first.y), it.second, it.third) }

        val t = mutableListOf<List<U>>()
        // De la puce graphique au processeur, deux lignes accordées en serpentin.
        for (i in 0 until 4) {
            val lx = 21.8f - i * 2.4f
            val ly = gpuU[3] + 3.2f + i * 1.5f
            val pts = mutableListOf(U(lx, gpuU[3] + 1.3f), U(lx, ly))
            if (i < 2) pts += meander(U(lx + 0.8f, ly), U(socU[0] - 1.6f, ly), 0.42f, 1f, if (i == 1) -1f else 1f)
            pts += U(socU[0] - 1.3f, ly)
            t += pts
        }
        // Du processeur au modem, à 45°.
        for (i in 0 until 4) t += route(U(socU[2] + 1.3f, socU[3] - 2f - i * 2f), U(modemU[0] + 2.4f + i * 2.6f, modemU[1] - 1.3f), vertical = false)
        // Du processeur aux objectifs : des paires différentielles.
        for (i in 0 until 3) for (o in listOf(0f, 0.7f)) {
            t += route(U(socU[2] + 1.3f, socU[1] + 2.5f + i * 2.2f + o), U(cams[1].first - 3.5f + i * 2.6f + o, pillU[3] + 0.2f), vertical = false)
        }
        // De la puce graphique au connecteur.
        for (i in 0 until 3) t += route(U(gpuU[0] + 2.2f + i * 2.8f, gpuU[3] + 1.3f), U(connU[0] + 2f + i * 3.6f, connU[1] - 0.2f))
        // Du modem au bord : les antennes.
        for (i in 0 until 3) t += listOf(U(modemU[2] + 1.3f, modemU[1] + 3f + i * 3f), U(92f, modemU[1] + 3f + i * 3f))
        // Du contrôleur d'alimentation au processeur.
        for (i in 0 until 3) t += route(U(pmicU[0] + 2.5f + i * 2.5f, pmicU[1] - 1.3f), U(socU[2] + 1.3f, socU[3] - 10f - i * 2f))
        traces = t.map { it.px() }

        // Les vias de couture, le long des bords des cartes, et quelques autres.
        val v = mutableListOf<U>()
        var vx = 10f
        while (vx < 91f) {
            if (!(vx > pillU[0] - 2f && vx < pillU[2] + 1f)) v += U(vx, boardU[1] + 1.3f)
            v += U(vx, bt + 1.3f)
            if (!(vx > 38f && vx < 62f)) v += U(vx, bottomU[3] - 1.3f)
            vx += 2.2f
        }
        var vy = boardU[1] + 3.5f
        while (vy < bb - 1f) {
            if (vy < modemU[1] - 1f || vy > modemU[3] + 1f) v += U(91.7f, vy)
            if (vy < gpuU[1] - 2f || vy > bb - 10f) v += U(8.3f, vy)
            vy += 2.2f
        }
        tries = 0
        while (tries++ < 400 && v.size < 120) {
            val px = 9f + random.nextFloat() * 82f
            val py = boardU[1] + 3f + random.nextFloat() * (bb - boardU[1] - 6f)
            if (free(px, py, 2.4f) && parts.all { hypot(it.first.x - px, it.first.y - py) > 1.8f }) v += U(px, py)
        }
        vias = v.map { p(it.x, it.y) }

        // Les condensateurs de découplage autour de l'interposeur, et les billes du substrat.
        val c = mutableListOf<Pair<PointF, Boolean>>()
        var kx = 27f
        while (kx < 86f) {
            c += p(kx, 89.2f) to true
            if (kx < 43.5f || kx > 56.5f) c += p(kx, 162.4f) to true
            kx += 1.9f
        }
        var ky = 96f
        while (ky < 157f) {
            c += p(23.5f, ky) to false
            c += p(87.7f, ky) to false
            ky += 2.4f
        }
        caps = c
        val b = mutableListOf<PointF>()
        var bx = 28.4f
        while (bx <= 84f) {
            b += p(bx, plateU[1] + 2.6f)
            if (bx < 43f || bx > 57f) b += p(bx, plateU[3] - 2.6f)
            bx += 2.4f
        }
        balls = b

        cores = (0 until 16).map { i -> p(33.6f + (i % 4) * 3.4f, top + 23.6f + (i / 4) * 3.4f) to random.nextFloat() }
        cells = (0 until 25).map { i -> p(12.6f + (i % 5) * 1.8f, top + 22.6f + (i / 5) * 1.8f) to random.nextFloat() }
    }

    private val brainCenter = p(cx, cy)
    private val upRoutes = upBus.map { CircuitScene.Route(it) }
    private val memoryRoutes = memoryBus.map { bus -> bus.map { CircuitScene.Route(it) } }
    private val brainRoutes = paths.map { CircuitScene.Route(it) }
    private val powerPaths by lazy { power.map { polyline(it) } }

    // ——— Les pinceaux du dessin fixe ———

    private fun polyline(points: List<PointF>) = Path().apply {
        moveTo(points[0].x, points[0].y)
        for (i in 1 until points.size) lineTo(points[i].x, points[i].y)
    }

    private fun seg(canvas: Canvas, x0: Float, y0: Float, x1: Float, y1: Float, paint: Paint) =
        canvas.drawLine(x(x0), y(y0), x(x1), y(y1), paint)

    private fun round(canvas: Canvas, rect: RectF, radius: Float, paint: Paint) = canvas.drawRoundRect(rect, x(radius), x(radius), paint)

    private fun inset(a: FloatArray, d: Float) = r(a[0] + d, a[1] + d, a[2] - d, a[3] - d)

    /** Les broches tout autour d'une puce. */
    private fun pins(canvas: Canvas, a: FloatArray, step: Float, length: Float, paint: Paint) {
        var px = a[0] + 1.6f
        while (px < a[2] - 1f) {
            seg(canvas, px, a[1] - length, px, a[1], paint)
            seg(canvas, px, a[3], px, a[3] + length, paint)
            px += step
        }
        var py = a[1] + 1.6f
        while (py < a[3] - 1f) {
            seg(canvas, a[0] - length, py, a[0], py, paint)
            seg(canvas, a[2], py, a[2] + length, py, paint)
            py += step
        }
    }

    /** Un repère de coin, en équerre. */
    private fun corner(canvas: Canvas, px: Float, py: Float, s: Float, dx: Float, dy: Float, paint: Paint) {
        canvas.drawLine(x(px), y(py + dy * s), x(px), y(py), paint)
        canvas.drawLine(x(px), y(py), x(px + dx * s), y(py), paint)
    }

    private fun paints(line: Paint, thin: Paint, fill: Paint) = object {
        val fine = Paint(thin).apply { strokeWidth = 0.5f * density }
        val faint = Paint(fine).apply { alpha = 120 }
        val dim = Paint(fill).apply { alpha = 130 }
        val wide = Paint(line).apply { strokeWidth = 0.7f * u; alpha = 140 }
        val dashed = Paint(fine).apply { alpha = 110; pathEffect = DashPathEffect(floatArrayOf(0.9f * u, 0.9f * u), 0f) }
        val shield = Paint(thin).apply { alpha = 150; pathEffect = DashPathEffect(floatArrayOf(1.1f * u, 0.7f * u), 0f) }
    }

    // ——— Au milieu : l'accélérateur ———

    override fun drawStatic(canvas: Canvas, line: Paint, thin: Paint, fill: Paint) {
        val k = paints(line, thin, fill)
        val plate = rect(plateU)
        round(canvas, plate, 2f, line)
        round(canvas, inset(plateU, 1.2f), 1.4f, k.faint)
        listOf(
            U(plateU[0] + 3.4f, plateU[1] + 3.4f), U(plateU[2] - 3.4f, plateU[1] + 3.4f),
            U(plateU[0] + 3.4f, plateU[3] - 3.4f), U(plateU[2] - 3.4f, plateU[3] - 3.4f),
        ).forEach { screw(canvas, p(it.x, it.y), thin) }
        balls.forEach { canvas.drawCircle(it.x, it.y, x(0.42f), k.faint) }
        caps.forEach { (at, horizontal) ->
            if (horizontal) {
                canvas.drawRect(at.x - x(0.55f), at.y - x(0.35f), at.x - x(0.25f), at.y + x(0.35f), k.dim)
                canvas.drawRect(at.x + x(0.25f), at.y - x(0.35f), at.x + x(0.55f), at.y + x(0.35f), k.dim)
            } else {
                canvas.drawRect(at.x - x(0.35f), at.y - x(0.55f), at.x + x(0.35f), at.y - x(0.25f), k.dim)
                canvas.drawRect(at.x - x(0.35f), at.y + x(0.25f), at.x + x(0.35f), at.y + x(0.55f), k.dim)
            }
        }

        // L'interposeur, ses repères ; la puce, son anneau de garde et ses plots.
        round(canvas, rect(interU), 1f, thin)
        corner(canvas, interU[0] + 1f, interU[1] + 1f, 2f, 1f, 1f, k.fine)
        corner(canvas, interU[2] - 1f, interU[3] - 1f, 2f, -1f, -1f, k.fine)
        round(canvas, rect(dieU), 1f, line)
        round(canvas, inset(dieU, 1f), 0.6f, k.faint)
        var dx = dieU[0] + 2.2f
        while (dx < dieU[2] - 1.6f) {
            canvas.drawCircle(x(dx), y(dieU[1] + 2.2f), x(0.22f), k.dim)
            canvas.drawCircle(x(dx), y(dieU[3] - 2.2f), x(0.22f), k.dim)
            dx += 1.4f
        }

        // Les piles de mémoire : leur boîtier et leurs traversées (les couches vivent).
        hbmU.forEach { a ->
            round(canvas, rect(a), 0.7f, line)
            round(canvas, inset(a, 0.8f), 0.4f, k.faint)
            var ty = a[1] + 2f
            while (ty < a[3] - 1.5f) {
                canvas.drawCircle(x(a[0] + 1.8f), y(ty), x(0.2f), k.dim)
                canvas.drawCircle(x(a[2] - 1.8f), y(ty), x(0.2f), k.dim)
                ty += 1.25f
            }
        }
        memoryBus.flatten().forEach { canvas.drawPath(polyline(it), k.fine) }

        // Le cerveau.
        lobes.forEach { canvas.drawPath(polyline(it), line) }
        folds.forEach { canvas.drawPath(polyline(it), k.dashed) }
        paths.forEach { canvas.drawPath(polyline(it), k.fine) }
        pads.forEach { canvas.drawCircle(it.x, it.y, x(0.5f), k.fine) }
    }

    // ——— Devant : la carte mère, les nappes, la carte du bas ———

    override fun drawBoard(canvas: Canvas, line: Paint, thin: Paint, fill: Paint) {
        val k = paints(line, thin, fill)
        round(canvas, rect(boardU), 2.4f, line)
        round(canvas, rect(bottomU), 2.4f, line)
        round(canvas, inset(boardU, 1f), 1.6f, k.faint)

        vias.forEach { canvas.drawCircle(it.x, it.y, x(0.42f), k.faint) }
        traces.forEach { canvas.drawPath(polyline(it), k.fine) }
        smd.forEach { (at, vertical, length) ->
            val pw = x(0.55f)
            val w = x(0.75f) / 2f
            val l = x(length) / 2f
            if (vertical) {
                canvas.drawRect(at.x - w, at.y - l - pw, at.x + w, at.y - l, k.dim)
                canvas.drawRect(at.x - w, at.y + l, at.x + w, at.y + l + pw, k.dim)
                canvas.drawRect(at.x - w, at.y - l, at.x + w, at.y + l, k.faint)
            } else {
                canvas.drawRect(at.x - l - pw, at.y - w, at.x - l, at.y + w, k.dim)
                canvas.drawRect(at.x + l, at.y - w, at.x + l + pw, at.y + w, k.dim)
                canvas.drawRect(at.x - l, at.y - w, at.x + l, at.y + w, k.faint)
            }
        }

        // Les objectifs, leurs bagues graduées, le flash et le capteur.
        round(canvas, rect(pillU), 8.5f, line)
        round(canvas, inset(pillU, 1f), 7.5f, k.faint)
        cams.forEach { (lx, ly, rr) ->
            val c = p(lx, ly)
            canvas.drawCircle(c.x, c.y, x(rr), line)
            canvas.drawCircle(c.x, c.y, x(rr * 0.8f), k.fine)
            canvas.drawCircle(c.x, c.y, x(rr * 0.55f), k.fine)
            canvas.drawCircle(c.x, c.y, x(rr * 0.3f), thin)
            for (i in 0 until 36) {
                val a = i / 36f * 2f * PI.toFloat()
                val r0 = x(rr * if (i % 3 != 0) 0.86f else 0.83f)
                val r1 = x(rr * 0.93f)
                canvas.drawLine(c.x + cos(a) * r0, c.y + sin(a) * r0, c.x + cos(a) * r1, c.y + sin(a) * r1, k.faint)
            }
            val gleam = x(rr * 0.42f)
            canvas.drawArc(RectF(c.x - gleam, c.y - gleam, c.x + gleam, c.y + gleam), 207f, 54f, false, k.fine)
        }
        val flash = p(flashU.x, flashU.y)
        canvas.drawCircle(flash.x, flash.y, x(1.7f), thin)
        canvas.drawCircle(flash.x - x(0.55f), flash.y, x(0.45f), k.fine)
        canvas.drawCircle(flash.x + x(0.55f), flash.y, x(0.45f), k.fine)
        canvas.drawCircle(flash.x, flash.y - y(4.2f), x(0.9f), thin)

        // Le processeur : blindage, billes, puce, repère de coin, cœurs.
        round(canvas, r(socU[0] - 3.4f, socU[1] - 3.4f, socU[2] + 3.4f, ramU[3] + 2.6f), 2f, k.shield)
        round(canvas, rect(socU), 1f, line)
        pins(canvas, socU, 1.6f, 1.1f, k.fine)
        for (i in 0 until 9) {
            val o = 1.3f + i * 2.42f
            canvas.drawCircle(x(socU[0] + o), y(socU[1] + 1.3f), x(0.3f), k.dim)
            canvas.drawCircle(x(socU[0] + o), y(socU[3] - 1.3f), x(0.3f), k.dim)
            canvas.drawCircle(x(socU[0] + 1.3f), y(socU[1] + o), x(0.3f), k.dim)
            canvas.drawCircle(x(socU[2] - 1.3f), y(socU[1] + o), x(0.3f), k.dim)
        }
        round(canvas, inset(socU, 3.2f), 0.6f, thin)
        corner(canvas, socU[0] + 4f, socU[1] + 4f, 1.4f, 1f, 1f, k.fine)
        cores.forEach { (at, _) ->
            canvas.drawRoundRect(RectF(at.x, at.y, at.x + x(2.6f), at.y + y(2.6f)), x(0.25f), x(0.25f), k.fine)
            canvas.drawLine(at.x + x(0.6f), at.y + y(1.3f), at.x + x(2f), at.y + y(1.3f), k.faint)
        }
        // Sa mémoire : deux puces et leurs billes.
        listOf(ramU[0] to ramU[0] + 10.6f, ramU[2] - 10.6f to ramU[2]).forEach { (a, b) ->
            round(canvas, r(a, ramU[1], b, ramU[3]), 0.6f, line)
            for (i in 0 until 6) for (j in 0 until 3) canvas.drawCircle(x(a + 1.6f + i * 1.5f), y(ramU[1] + 2.2f + j * 1.8f), x(0.28f), k.dim)
        }
        // La puce graphique et ses cellules.
        round(canvas, rect(gpuU), 0.8f, line)
        pins(canvas, gpuU, 1.5f, 1f, k.fine)
        round(canvas, inset(gpuU, 2f), 0.4f, thin)
        corner(canvas, gpuU[0] + 1f, gpuU[1] + 1f, 1f, 1f, 1f, k.fine)
        // Le modem, le contrôleur d'alimentation, le connecteur.
        listOf(modemU, pmicU).forEach { a ->
            round(canvas, rect(a), 0.6f, line)
            pins(canvas, a, 1.8f, 1f, k.fine)
            round(canvas, inset(a, 1.8f), 0.3f, k.fine)
            corner(canvas, a[0] + 0.9f, a[1] + 0.9f, 1f, 1f, 1f, k.fine)
        }
        for (i in 0 until 3) for (j in 0 until 3) canvas.drawCircle(x(modemU[0] + 4.4f + i * 2.6f), y(modemU[1] + 4.4f + j * 2.6f), x(0.5f), k.fine)
        round(canvas, rect(connU), 0.4f, line)
        var cxu = connU[0] + 1.1f
        while (cxu < connU[2] - 0.6f) {
            seg(canvas, cxu, connU[1] + 0.8f, cxu, connU[3] - 0.8f, k.fine)
            cxu += 0.9f
        }

        // Les nappes : leurs connecteurs et leurs plis, et les bus qui y passent.
        ribbonsU.forEach { rx0 ->
            val y0 = bb
            val y1 = plateU[1]
            round(canvas, r(rx0 - 4.6f, y0 - 2.6f, rx0 + 4.6f, y0 - 0.2f), 0.3f, thin)
            round(canvas, r(rx0 - 4.6f, y1 - 2.2f, rx0 + 4.6f, y1 + 0.2f), 0.3f, thin)
            seg(canvas, rx0 - 4f, y0 - 0.2f, rx0 - 4f, y1 - 2.2f, k.fine)
            seg(canvas, rx0 + 4f, y0 - 0.2f, rx0 + 4f, y1 - 2.2f, k.fine)
            var fy = y0 + 1.6f
            while (fy < y1 - 3f) {
                seg(canvas, rx0 - 4f, fy, rx0 + 4f, fy, k.faint)
                fy += 2.2f
            }
        }
        upBus.forEach { canvas.drawPath(polyline(it), k.fine) }
        side.forEach { canvas.drawPath(polyline(it), thin) }

        // La carte du bas : alimentation, bobines, moteur, port, haut-parleur.
        power.forEach { canvas.drawPath(polyline(it), k.wide) }
        coilsU.forEach { c ->
            val at = p(c.x, c.y)
            round(canvas, r(c.x - 4f, c.y - 4f, c.x + 4f, c.y + 4f), 0.8f, line)
            canvas.drawCircle(at.x, at.y, x(2.6f), thin)
            for (i in 0 until 4) {
                val rr = x(1.1f + i * 0.4f)
                canvas.drawArc(RectF(at.x - rr, at.y - rr, at.x + rr, at.y + rr), 36f + i * 72f, 162f, false, k.faint)
            }
        }
        round(canvas, rect(motorU), 2f, line)
        val my = (motorU[1] + motorU[3]) / 2f
        canvas.drawCircle(x(motorU[0] + 4.2f), y(my), x(2.6f), k.fine)
        seg(canvas, motorU[0] + 8f, my, motorU[2] - 2f, my, k.fine)
        round(canvas, rect(usbU), 2.7f, line)
        round(canvas, r(usbU[0] + 2.5f, usbU[1] + 1.8f, usbU[2] - 2.5f, usbU[3] - 1.8f), 1f, k.fine)
        for (i in 0 until 12) seg(canvas, usbU[0] + 4f + i * 1.1f, usbU[1] + 2.3f, usbU[0] + 4f + i * 1.1f, usbU[1] + 2.9f, k.faint)
        for (j in 0 until 4) for (i in 0 until 8 - j % 2) {
            val at = p(67f + i * 3f + (j % 2) * 1.5f, bt + 4.5f + j * 2.6f)
            canvas.drawPath(kit.polygon((0 until 6).map { kit.around(at, x(1.05f), 30f + it * 60f) }), k.fine)
        }
    }

    override fun outlines(): List<Path> =
        listOf(Path().apply { addRoundRect(rect(dieU), x(1f), x(1f), Path.Direction.CW) }) +
            lobes.map { polyline(it) } +
            hbmU.map { a -> Path().apply { addRoundRect(rect(a), x(0.7f), x(0.7f), Path.Direction.CW) } } +
            listOf(
                Path().apply { addRoundRect(rect(interU), x(1f), x(1f), Path.Direction.CW) },
                Path().apply { addRoundRect(rect(plateU), x(2f), x(2f), Path.Direction.CW) },
            )

    override val origin get() = brainCenter

    /** Le réseau : le faisceau du bord ; la charge : les pistes d'alimentation ; le battement : les bus des nappes. */
    override fun routes() = Triple(
        side.map { CircuitScene.Route(it) },
        power.map { CircuitScene.Route(it).reversed() },
        upRoutes,
    )

    // ——— Ce qui vit ———

    /** Où en est l'inférence, de 0 à 1. */
    private fun phase(state: FrameState): Float {
        val period = if (state.charging) 2600f else 4000f
        return (state.timeMillis % period.toLong()) / period
    }

    private fun window(phi: Float, a: Float, b: Float) = (phi - a) / (b - a)

    private val point = PointF()

    private fun spark(canvas: Canvas, ink: Ink, at: PointF, a: Float, radius: Float = 0.42f) {
        if (a <= 0.01f) return
        ink.halo(canvas, at, x(2.1f), (165 * a).toInt())
        ink.fill.color = ink.palette.core
        ink.fill.alpha = (255 * a).coerceIn(0f, 255f).toInt()
        canvas.drawCircle(at.x, at.y, x(radius), ink.fill)
    }

    override fun drawLive(canvas: Canvas, state: FrameState, ink: Ink) {
        val strength = strength(state)
        val ignition = state.ignition.coerceIn(0f, 1f)
        val s = strength * ignition
        val seconds = state.timeMillis / 1000f
        val phi = phase(state)
        val palette = ink.palette
        val stroke = ink.stroke

        // 1. Les mémoires sont lues, couche après couche.
        hbmU.forEachIndexed { k, a ->
            for (i in 1 until 9) {
                val ly = a[1] + i * (a[3] - a[1]) / 9.2f + 0.4f
                val at = window(phi, 0.02f + i * 0.022f, 0.08f + i * 0.022f)
                val on = if (at > 0f && at < 1f) sin(at * PI.toFloat()) else 0f
                val idle = 0.3f + 0.1f * sin(seconds * 1.3f + k + i)
                stroke.color = if (on > 0.5f) palette.core else palette.line
                stroke.alpha = (255 * (idle + 0.65f * on).coerceAtMost(1f) * s).toInt()
                stroke.strokeWidth = 0.65f * density * (1f + on)
                seg(canvas, a[0] + 2.6f, ly, a[2] - 2.6f, ly, stroke)
            }
            val read = window(phi, 0.04f, 0.3f)
            if (read > 0f && read < 1f) ink.halo(canvas, p((a[0] + a[2]) / 2f, (a[1] + a[3]) / 2f), x(10f), (46 * sin(read * PI.toFloat()) * s).toInt())
        }

        // 2. Les données filent des mémoires vers le cerveau.
        val go = window(phi, 0.18f, 0.48f)
        if (go > 0f && go < 1f) memoryRoutes.forEach { bus ->
            bus.forEachIndexed { i, route ->
                val t = go * 1.15f - i * 0.03f
                if (t > 0f && t < 1f) {
                    route.at(t, point)
                    spark(canvas, ink, point, sin(t * PI.toFloat()) * s)
                }
            }
        }

        // 3. La pensée : la ligne médiane s'allume, une vague court jusqu'aux plots.
        val wave = window(phi, 0.42f, 0.74f)
        val mid = (if (state.charging) 0.55f + 0.45f * sin(seconds * 5f) else 0.25f) +
            if (wave > 0f && wave < 0.3f) 0.6f * sin(wave / 0.3f * PI.toFloat()) else 0f
        ink.halo(canvas, brainCenter, x(17f), ((40 + 56 * mid) * s).toInt())
        stroke.color = palette.core
        stroke.alpha = (255 * (0.45f + 0.55f * mid).coerceAtMost(1f) * s).toInt()
        stroke.strokeWidth = 1.1f * density
        seg(canvas, cx, cy - ry * 0.97f, cx, cy + ry * 0.97f, stroke)
        if (wave > 0f && wave < 1.2f) brainRoutes.forEachIndexed { i, route ->
            val t = wave * 1.1f - (i % 5) * 0.02f
            if (t > 0f && t < 1f) {
                route.at(t, point)
                spark(canvas, ink, point, 0.85f * sin(t * PI.toFloat()) * s, 0.34f)
            }
        }

        // Les plots : autant d'allumés que de batterie, la vague les fait briller en passant.
        val lit = (state.batteryLevel.coerceIn(0f, 1f) * pads.size).roundToInt().coerceAtLeast(1)
        val flash = if (wave > 0.8f && wave < 1.2f) sin((wave - 0.8f) / 0.4f * PI.toFloat()) else 0f
        val breath = if (state.charging) (sin(seconds * 4.5f) + 1f) / 2f else 1f
        pads.forEachIndexed { i, pad ->
            if (i >= lit) return@forEachIndexed
            // En charge, le dernier plot allumé respire.
            val last = if (i == lit - 1) 0.35f + 0.65f * breath else 1f
            val a = ((0.72f + 0.2f * sin(seconds * 2f + i * 0.7f) + 0.3f * flash).coerceAtMost(1f) * last * s)
            ink.halo(canvas, pad, x(1.7f + flash), ((76 + 76 * flash) * a).toInt())
            ink.fill.color = palette.core
            ink.fill.alpha = (255 * a).toInt()
            canvas.drawCircle(pad.x, pad.y, x(0.55f), ink.fill)
        }
    }

    override fun drawLiveBoard(canvas: Canvas, state: FrameState, ink: Ink) {
        val strength = strength(state)
        val ignition = state.ignition.coerceIn(0f, 1f)
        val s = strength * ignition
        val seconds = state.timeMillis / 1000f
        val phi = phase(state)
        val palette = ink.palette

        // 4. La réponse remonte par les nappes jusqu'au processeur.
        val up = window(phi, 0.72f, 0.96f)
        if (up > 0f && up < 1.1f) upRoutes.forEachIndexed { i, route ->
            val t = up * 1.1f - (i % 6) * 0.02f
            if (t > 0f && t < 1f) {
                route.at(1f - t, point)
                spark(canvas, ink, point, sin(t * PI.toFloat()) * s)
            }
        }
        val answer = window(phi, 0.9f, 1f).let { if (it > 0f) sin(it * PI.toFloat()) else 0f }

        // Les cœurs du processeur et les cellules de la puce graphique, qui calculent.
        cores.forEach { (at, rhythm) ->
            val b = max(0f, sin(seconds * (0.9f + rhythm * 1.6f) + rhythm * 20f)).pow(6)
            val a = max(b, answer)
            if (a <= 0.05f) return@forEach
            ink.fill.color = if (a > 0.6f) palette.core else palette.line
            ink.fill.alpha = (255 * (0.15f + 0.5f * a) * s).toInt()
            canvas.drawRect(at.x + x(0.35f), at.y + y(0.35f), at.x + x(2.25f), at.y + y(2.25f), ink.fill)
        }
        ink.halo(canvas, p((socU[0] + socU[2]) / 2f, (socU[1] + socU[3]) / 2f), x(12f), ((18 + 64 * answer) * s).toInt())
        cells.forEach { (at, rhythm) ->
            val b = max(0f, sin(seconds * (2f + rhythm * 3f) + rhythm * 40f)).pow(4)
            ink.fill.color = if (b > 0.6f) palette.core else palette.line
            ink.fill.alpha = (255 * (0.18f + 0.7f * b) * s).toInt()
            canvas.drawRect(at.x, at.y, at.x + x(1.3f), at.y + y(1.3f), ink.fill)
        }
        ink.halo(canvas, p((gpuU[0] + gpuU[2]) / 2f, (gpuU[1] + gpuU[3]) / 2f), x(8f), (26 * s).toInt())

        // En charge : les bobines s'éclairent, et leurs pistes jusqu'au boîtier.
        if (state.charging) {
            val surge = (sin(seconds * 4f) + 1f) / 2f
            ink.stroke.color = palette.core
            ink.stroke.alpha = (255 * (0.4f + 0.5f * surge) * s).toInt()
            ink.stroke.strokeWidth = 0.7f * u
            powerPaths.forEach { canvas.drawPath(it, ink.stroke) }
            coilsU.forEach { c ->
                val at = p(c.x, c.y)
                ink.halo(canvas, at, x(7f), ((76 + 76 * surge) * s).toInt())
                ink.stroke.alpha = (255 * (0.7f + 0.3f * surge) * s).toInt()
                ink.stroke.strokeWidth = 0.9f * density
                canvas.drawCircle(at.x, at.y, x(2.6f), ink.stroke)
            }
        }
    }
}
