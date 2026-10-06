package dev.levilainpetit.wux.wallpaper

import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PointF
import android.graphics.RectF
import android.graphics.Shader
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.max
import kotlin.math.pow
import kotlin.math.sin
import kotlin.random.Random

/**
 * Physique quantique : tout le téléphone devient le bas du « lustre » d'un
 * ordinateur quantique, le réfrigérateur à dilution vu de près.
 *
 * - au fond, la forêt de câbles coaxiaux et leurs boucles, deux étages et la
 *   colonne centrale empilée ;
 * - au milieu, le plateau doré et sa bride, les deux colonnes de cuivre
 *   couvertes de connecteurs, et le support de la puce ;
 * - devant, les câbles : ils retombent dans les colonnes et, en gerbe, dans
 *   le support de la puce. Les impulsions du réseau courent dessus.
 *
 * Ce qui vit : une rangée de connecteurs s'allume par dixième de batterie,
 * des impulsions de commande descendent vers la puce et la lecture remonte,
 * la puce bat et ses qubits scintillent ; en charge, une vague de froid
 * descend les boucles de la forêt.
 *
 * Les cotes sont celles de la maquette : un centième de la largeur, et une
 * hauteur de 216 unités ramenée à celle de l'écran par [y].
 */
class QuantumCore(kit: CoreKit) : CoreArt(kit) {

    override val wholePhone get() = true

    private val scale = kit.h / 216f
    private fun x(v: Float) = v * u
    private fun y(v: Float) = v * scale * u
    private fun p(px: Float, py: Float) = PointF(x(px), y(py))

    private class Loop(val center: PointF, val radius: Float, val level: Float)
    private class Strand(val x: Float, val loops: List<Loop>)

    private val plateY = 62f
    private val rows = 11
    private fun rowY(k: Int) = 92f + k * 8.6f
    private val brackets = listOf(22f to 34f, 66f to 78f)

    /** La forêt du haut : des câbles verticaux, une ou deux boucles chacun. */
    private val forest: List<Strand> = run {
        val random = Random(42)
        (0 until 30).mapNotNull { k ->
            val sx = 9f + k * 2.83f + (random.nextFloat() - 0.5f) * 0.8f
            if (sx > 41f && sx < 59f) return@mapNotNull null
            val loops = (0 until if (random.nextFloat() < 0.6f) 1 else 2).map {
                val r = 2.2f + random.nextFloat() * 1.6f
                val pick = if (random.nextBoolean()) -1f else 1f
                val side = when {
                    sx + pick * r * 2 > 92f -> -1f
                    sx + pick * r * 2 < 8f -> 1f
                    else -> pick
                }
                val ly = 14f + random.nextFloat() * 38f
                Loop(p(sx + side * r, ly), x(r), ly)
            }
            Strand(sx, loops)
        }
    }

    private fun bezier(a: PointF, b: PointF, c: PointF, d: PointF, n: Int = 18): List<PointF> = (0..n).map { i ->
        val t = i / n.toFloat()
        val k0 = (1 - t).pow(3)
        val k1 = 3 * (1 - t).pow(2) * t
        val k2 = 3 * (1 - t) * t * t
        val k3 = t * t * t
        PointF(k0 * a.x + k1 * b.x + k2 * c.x + k3 * d.x, k0 * a.y + k1 * b.y + k2 * c.y + k3 * d.y)
    }

    /** Les câbles du bas, en trois familles : vers les colonnes, la gerbe, et ceux qui pendent sous la puce. */
    private val toBrackets = mutableListOf<List<PointF>>()
    private val sheaf = mutableListOf<List<PointF>>()
    private val hanging = mutableListOf<List<PointF>>()

    init {
        val start = plateY + 3.5f
        brackets.forEachIndexed { bi, (x0, x1) ->
            val sgn = if (bi == 0) -1f else 1f
            for (k in 0 until rows) {
                val ey = rowY(k)
                // Dehors : du bord du plateau, une large boucle vers l'extérieur.
                val sx = if (bi == 0) 12f + k * 1.6f else 88f - k * 1.6f
                val ex = if (bi == 0) x0 - 1.5f else x1 + 1.5f
                toBrackets += bezier(p(sx, start), p(sx + sgn * (6 + k * 0.6f), start + 25f), p(ex + sgn * (8 + k * 0.9f), ey - 6f), p(ex, ey))
                // Dedans : vers le centre, puis le côté intérieur de la colonne.
                if (k % 2 == 0) {
                    val sx2 = if (bi == 0) 30f + k * 0.8f else 70f - k * 0.8f
                    val ex2 = if (bi == 0) x1 + 1.5f else x0 - 1.5f
                    toBrackets += bezier(p(sx2, start), p(sx2 - sgn * 6, start + 18 + k * 2), p(ex2 - sgn * 7, ey - 10), p(ex2, ey))
                }
            }
        }
        for (k in 0 until 14) {
            val side = if (k % 2 == 1) 1f else -1f
            val j = k / 2
            val sx = 50f + side * (3 + j * 1.6f)
            val ex = 44.5f + (k % 7) * 1.85f
            sheaf += bezier(p(sx, start), p(sx + side * (14 + j * 2), start + 6), p(ex + side * (10 + j), 104f), p(ex, 112f))
        }
        val random = Random(9)
        for (k in 0 until 8) {
            val sx = 44.8f + k * 1.5f
            hanging += bezier(
                p(sx, 152f), p(sx - 3 + k * 0.6f, 162f),
                p(sx + (random.nextFloat() - 0.5f) * 6, 170f), p(sx + (random.nextFloat() - 0.5f) * 3, 176f),
            )
        }
    }

    private val cables = toBrackets + sheaf + hanging
    private val cableRoutes = cables.map { CircuitScene.Route(it) }

    /** Les connecteurs des colonnes, rangée par rangée (du haut vers le bas). */
    private val connectors: List<List<PointF>> = (0 until rows).map { k -> brackets.flatMap { (x0, x1) -> listOf(p(x0, rowY(k)), p(x1, rowY(k))) } }
    private val plateConnectors = generateSequence(10f) { it + 2.6f }.takeWhile { it <= 90f }.map { p(it, plateY + 4.2f) }.toList()
    private val chip = RectF(x(47f), y(128f), x(53f), y(134f))
    private val chipCenter = PointF(chip.centerX(), chip.centerY())
    private val qubits = (0 until 9).map { i -> p(48.2f + (i % 3) * 1.8f, 129.2f + (i / 3) * 1.8f) }

    private fun hexagon(at: PointF, r: Float) = kit.polygon((0 until 6).map { kit.around(at, r, 30f + it * 60f) })
    private val bracketHexes = connectors.map { row -> row.map { hexagon(it, x(1.5f)) } }

    private fun polyline(points: List<PointF>) = Path().apply {
        moveTo(points[0].x, points[0].y)
        points.drop(1).forEach { lineTo(it.x, it.y) }
    }

    private fun seg(canvas: Canvas, x0: Float, y0: Float, x1: Float, y1: Float, paint: Paint) =
        canvas.drawLine(x(x0), y(y0), x(x1), y(y1), paint)

    // ——— Au fond : la forêt de câbles, les étages, la colonne ———

    override fun drawChassis(canvas: Canvas, line: Paint, thin: Paint, fill: Paint) {
        for (py in floatArrayOf(9f, 34f)) {
            seg(canvas, 7f, py, 93f, py, line)
            seg(canvas, 7f, py + 1.4f, 93f, py + 1.4f, thin)
        }
        for (k in 0 until 9) {
            val top = 10f + k * 5.4f
            val w = if (k % 2 == 1) 7f else 9f
            canvas.drawRect(x(50f - w), y(top), x(50f + w), y(top + 3.4f), line)
        }
        for (rx in floatArrayOf(42f, 43f, 57f, 58f, 12f, 13.2f, 34f, 35.2f, 66f, 67.2f, 88f, 89.2f)) seg(canvas, rx, 9f, rx, 60f, thin)
        forest.forEach { s ->
            seg(canvas, s.x, 9f, s.x, plateY, thin)
            s.loops.forEach { canvas.drawCircle(it.center.x, it.center.y, it.radius, thin) }
        }
    }

    // ——— Au milieu : le plateau, les colonnes, le support de la puce ———

    override fun drawStatic(canvas: Canvas, line: Paint, thin: Paint, fill: Paint) {
        canvas.drawRoundRect(RectF(x(6f), y(plateY), x(94f), y(plateY + 2.6f)), x(0.8f), x(0.8f), line)
        seg(canvas, 7f, plateY + 1.3f, 93f, plateY + 1.3f, thin)
        canvas.drawRoundRect(RectF(x(36f), y(plateY - 4f), x(64f), y(plateY)), x(1f), x(1f), line)
        plateConnectors.forEach { canvas.drawPath(hexagon(it, x(0.9f)), thin) }

        brackets.forEach { (x0, x1) ->
            canvas.drawRoundRect(RectF(x(x0 + 2f), y(80f), x(x1 - 2f), y(192f)), x(1f), x(1f), line)
            canvas.drawRoundRect(RectF(x(x0 + 3.2f), y(82f), x(x1 - 3.2f), y(190f)), x(0.6f), x(0.6f), thin)
            canvas.drawCircle(x((x0 + x1) / 2f), y(186f), x(0.9f), thin)
            for (k in 0 until rows) {
                seg(canvas, x0 + 1.5f, rowY(k), x0 + 2f, rowY(k), thin)
                seg(canvas, x1 - 2f, rowY(k), x1 - 1.5f, rowY(k), thin)
            }
        }
        connectors.forEachIndexed { k, row ->
            row.forEachIndexed { i, c ->
                canvas.drawPath(bracketHexes[k][i], line)
                canvas.drawCircle(c.x, c.y, x(0.45f), fill)
            }
        }

        // Le support : la barre de serrage, les blocs des câbles, la puce.
        canvas.drawRect(x(36f), y(92f), x(64f), y(95f), line)
        canvas.drawCircle(x(50f), y(93.5f), x(1f), thin)
        canvas.drawRoundRect(RectF(x(42f), y(108f), x(58f), y(122f)), x(0.8f), x(0.8f), line)
        canvas.drawRoundRect(RectF(x(44f), y(124f), x(56f), y(138f)), x(0.6f), x(0.6f), line)
        canvas.drawRect(chip, line)
        canvas.drawRoundRect(RectF(x(42f), y(140f), x(58f), y(152f)), x(0.8f), x(0.8f), line)
        for (r in 0 until 3) for (c in 0 until 7) {
            canvas.drawCircle(x(44.5f + c * 1.85f), y(113f + r * 3.4f), x(0.75f), thin)
            canvas.drawCircle(x(44.5f + c * 1.85f), y(144f + r * 3.2f), x(0.75f), thin)
        }
    }

    // ——— Devant : les câbles ———

    override fun drawBoard(canvas: Canvas, line: Paint, thin: Paint, fill: Paint) {
        cables.forEach { canvas.drawPath(polyline(it), thin) }
    }

    override fun outlines(): List<Path> =
        listOf(Path().apply { addRect(chip.left, chip.top, chip.right, chip.bottom, Path.Direction.CW) }) +
            bracketHexes.flatten() +
            listOf(Path().apply { addRoundRect(RectF(x(6f), y(plateY), x(94f), y(plateY + 2.6f)), x(0.8f), x(0.8f), Path.Direction.CW) })

    override val origin get() = chipCenter

    /** Le réseau : la gerbe, du plateau à la puce ; la charge : les câbles des colonnes ; le battement : ceux qui pendent. */
    override fun routes() = Triple(
        cableRoutes.subList(toBrackets.size, toBrackets.size + sheaf.size),
        cableRoutes.subList(0, toBrackets.size).map { it.reversed() },
        cableRoutes.subList(toBrackets.size + sheaf.size, cableRoutes.size).map { it.reversed() },
    )

    // ——— Ce qui vit ———

    override fun drawLiveChassis(canvas: Canvas, state: FrameState, ink: Ink) {
        if (!state.charging) return
        // Le refroidissement : une vague qui descend les boucles.
        val strength = strength(state)
        val wave = (state.timeMillis / 1000f * 0.4f) % 1.3f * 60f
        forest.forEach { s ->
            s.loops.forEach { l ->
                val cold = max(0f, 1f - abs(wave - l.level) / 6f)
                if (cold <= 0.05f) return@forEach
                ink.halo(canvas, l.center, l.radius * 1.6f, (110 * cold * strength).toInt())
                ink.stroke.color = ink.palette.core
                ink.stroke.alpha = (230 * cold * strength).toInt()
                ink.stroke.strokeWidth = 1.1f * density
                canvas.drawCircle(l.center.x, l.center.y, l.radius, ink.stroke)
            }
        }
    }

    private var lightColor = 0
    private var light: LinearGradient? = null

    override fun drawLive(canvas: Canvas, state: FrameState, ink: Ink) {
        val strength = strength(state)
        val ignition = state.ignition.coerceIn(0f, 1f)
        val seconds = state.timeMillis / 1000f
        val palette = ink.palette

        // La lueur sous le lustre, et celle du plateau.
        if (light == null || lightColor != palette.glow) {
            lightColor = palette.glow
            light = LinearGradient(0f, y(185f), 0f, y(213f), palette.glow and 0x00FFFFFF, (palette.glow and 0x00FFFFFF) or 0x30000000, Shader.TileMode.CLAMP)
        }
        ink.fill.shader = light
        ink.fill.alpha = (255 * strength * ignition).toInt()
        canvas.drawRect(x(4f), y(185f), x(96f), y(213f), ink.fill)
        ink.fill.shader = null
        ink.halo(canvas, p(50f, plateY - 2f), x(16f), (46 * strength * ignition).toInt())

        // Les rangées de connecteurs : de bas en haut, une par dixième de batterie.
        val lit = ceil(state.batteryLevel.coerceIn(0f, 1f) * 10f).toInt().coerceIn(1, 10)
        val breath = if (state.charging) (sin(seconds * 4.5f) + 1f) / 2f else 1f
        for (k in 0 until rows) {
            val order = rows - 1 - k
            val on = ((ignition - 0.05f * order) / 0.2f).coerceIn(0f, 1f)
            val a = on * strength * when {
                order < lit - 1 -> 1f
                order == lit - 1 -> 0.35f + 0.65f * breath
                else -> 0f
            }
            if (a <= 0f) continue
            connectors[k].forEachIndexed { i, c ->
                ink.halo(canvas, c, x(2.6f), (90 * a).toInt())
                ink.stroke.color = palette.core
                ink.stroke.alpha = (240 * a).toInt()
                ink.stroke.strokeWidth = 1f * density
                canvas.drawPath(bracketHexes[k][i], ink.stroke)
            }
        }

        // La puce : elle bat, ses qubits scintillent.
        val beat = ((sin(seconds * 2.2f) + 1f) / 2f).pow(3)
        ink.halo(canvas, chipCenter, x(9f), ((70 + 110 * beat) * strength * ignition).toInt())
        ink.stroke.color = palette.core
        ink.stroke.alpha = (230 * strength * ignition).toInt()
        ink.stroke.strokeWidth = 1.2f * density
        canvas.drawRect(chip, ink.stroke)
        qubits.forEachIndexed { i, q ->
            val s = (sin(seconds * (1.4f + i * 0.31f) + i * 2f) + 1f) / 2f
            ink.fill.color = palette.core
            ink.fill.alpha = ((100 + 155 * s) * strength * ignition).toInt()
            canvas.drawCircle(q.x, q.y, x(0.35f), ink.fill)
        }
    }

    private val point = PointF()

    override fun drawLiveBoard(canvas: Canvas, state: FrameState, ink: Ink) {
        // Des impulsions de commande vers la puce ; la lecture remonte par les câbles du bas.
        val strength = strength(state)
        val ignition = state.ignition.coerceIn(0f, 1f)
        val seconds = state.timeMillis / 1000f
        val speed = if (state.charging) 0.55f else 0.28f
        val count = if (state.charging) 2 else 1
        val firstHanging = toBrackets.size + sheaf.size
        cableRoutes.forEachIndexed { i, route ->
            if (i % 4 != 0) return@forEachIndexed
            for (j in 0 until count) {
                val t = (seconds * speed + i * 0.137f + j / count.toFloat()) % 1f
                route.at(if (i >= firstHanging) 1f - t else t, point)
                val a = sin(t * PI.toFloat()) * strength * ignition
                ink.halo(canvas, point, x(2.4f), (170 * a).toInt())
                ink.fill.color = ink.palette.core
                ink.fill.alpha = (255 * a).toInt()
                canvas.drawCircle(point.x, point.y, x(0.5f), ink.fill)
            }
        }
    }
}
