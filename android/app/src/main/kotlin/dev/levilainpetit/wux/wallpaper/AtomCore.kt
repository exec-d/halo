package dev.levilainpetit.wux.wallpaper

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PointF
import android.graphics.RectF
import kotlin.math.hypot
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Énergie atomique : le cœur d'un réacteur vu de dessus, à la place de la
 * batterie. La cuve et ses brides, les assemblages de combustible en nid
 * d'abeilles, les grappes de contrôle, les boucles de refroidissement, et le
 * trèfle de la radioactivité sur la platine.
 *
 * - les assemblages s'allument du centre vers le bord selon la batterie ;
 * - des neutrons passent d'un assemblage à l'autre : la réaction en chaîne,
 *   plus vive en charge ;
 * - la lueur bleue de l'eau (l'effet Tcherenkov) respire lentement.
 */
class AtomCore(kit: CoreKit) : CoreArt(kit) {

    private val center = kit.at(0.5f, 0.44f)
    private val radius = kit.area.width() / 2f - 4f * u

    /** La taille d'un hexagone (rayon), et les assemblages jusqu'au troisième anneau. */
    private val hex = radius * 0.135f
    private val cells: List<PointF> = run {
        val out = mutableListOf<PointF>()
        for (q in -3..3) for (r in -3..3) {
            val s = -q - r
            if (maxOf(kotlin.math.abs(q), kotlin.math.abs(r), kotlin.math.abs(s)) > 3) continue
            val x = center.x + hex * sqrt(3f) * (q + r / 2f)
            val y = center.y + hex * 1.5f * r
            out += PointF(x, y)
        }
        out.sortedBy { hypot(it.x - center.x, it.y - center.y) }
    }

    /** Les grappes de contrôle : six assemblages du deuxième anneau. */
    private val rods = cells.filter {
        val d = hypot(it.x - center.x, it.y - center.y) / (hex * sqrt(3f))
        d in 1.9f..2.1f
    }.filterIndexed { i, _ -> i % 2 == 0 }

    private val trefoil = kit.at(0.5f, 0.88f)

    private fun hexagon(at: PointF, size: Float) = kit.polygon((0 until 6).map { kit.around(at, size, 30f + it * 60f) })

    /** Les assemblages en service, un peu en retrait de leur contour. */
    private val cores = cells.map { hexagon(it, hex * 0.86f) }

    override fun drawStatic(canvas: Canvas, line: Paint, thin: Paint, fill: Paint) {
        plate(canvas, line, thin)
        ribbon(canvas, center.x - 15f * u, kit.area.top, thin)
        ribbon(canvas, center.x + 15f * u, kit.area.top, thin)

        // Les quatre boucles de refroidissement, vers les coins.
        for (angle in floatArrayOf(-140f, -40f, 40f, 140f)) {
            val from = kit.around(center, radius, angle)
            val to = kit.around(center, radius * 1.32f, angle)
            for (side in floatArrayOf(-1.3f, 1.3f)) {
                val nx = -sin(Math.toRadians(angle.toDouble())).toFloat() * side * u
                val ny = kotlin.math.cos(Math.toRadians(angle.toDouble())).toFloat() * side * u
                canvas.drawLine(from.x + nx, from.y + ny, to.x + nx, to.y + ny, thin)
            }
            canvas.drawCircle(to.x, to.y, 2.2f * u, line)
            canvas.drawCircle(to.x, to.y, 1.2f * u, thin)
        }
        // La cuve : brides et goujons.
        canvas.drawCircle(center.x, center.y, radius, line)
        canvas.drawCircle(center.x, center.y, radius * 0.93f, thin)
        for (k in 0 until 24) {
            val at = kit.around(center, radius * 0.965f, k * 15f)
            canvas.drawCircle(at.x, at.y, 0.45f * u, thin)
        }
        canvas.drawCircle(center.x, center.y, radius * 0.86f, thin)
        // Les assemblages : hexagone, et son crayon central.
        cells.forEach { c ->
            canvas.drawPath(hexagon(c, hex * 0.92f), thin)
            canvas.drawCircle(c.x, c.y, hex * 0.22f, thin)
        }
        // Les grappes de contrôle : une croix dans leur assemblage.
        rods.forEach { c ->
            canvas.drawPath(hexagon(c, hex * 0.92f), line)
            canvas.drawLine(c.x - hex * 0.55f, c.y, c.x + hex * 0.55f, c.y, line)
            canvas.drawLine(c.x, c.y - hex * 0.55f, c.x, c.y + hex * 0.55f, line)
        }
        // Le trèfle, en bas de la platine.
        val tr = 4.4f * u
        canvas.drawCircle(trefoil.x, trefoil.y, tr * 0.22f, line)
        for (k in 0 until 3) {
            val start = -90f + k * 120f - 30f
            val outer = RectF(trefoil.x - tr, trefoil.y - tr, trefoil.x + tr, trefoil.y + tr)
            val inner = RectF(trefoil.x - tr * 0.36f, trefoil.y - tr * 0.36f, trefoil.x + tr * 0.36f, trefoil.y + tr * 0.36f)
            val blade = Path().apply {
                arcTo(outer, start, 60f)
                arcTo(inner, start + 60f, -60f)
                close()
            }
            canvas.drawPath(blade, line)
        }
        canvas.drawCircle(trefoil.x, trefoil.y, tr * 1.25f, thin)
    }

    override fun outlines(): List<Path> =
        listOf(Path().apply { addCircle(center.x, center.y, radius, Path.Direction.CW) }) + cells.map { hexagon(it, hex * 0.92f) }

    override fun drawLive(canvas: Canvas, state: FrameState, ink: Ink) {
        val strength = strength(state)
        val ignition = state.ignition.coerceIn(0f, 1f)
        val seconds = state.timeMillis / 1000f
        val palette = ink.palette

        // La lueur de l'eau, qui respire lentement.
        val breath = (sin(seconds * 0.9f) + 1f) / 2f
        ink.halo(canvas, center, radius * (1.05f + 0.1f * breath), ((45 + 35 * breath) * strength * ignition).toInt())

        // Les assemblages en service : du centre vers le bord, selon la batterie.
        val lit = (state.batteryLevel.coerceIn(0f, 1f) * cells.size).roundToInt().coerceIn(1, cells.size)
        val shown = (lit * ((ignition - 0.1f) / 0.9f).coerceIn(0f, 1f)).roundToInt()
        for (i in 0 until shown) {
            val c = cells[i]
            val flicker = 0.8f + 0.2f * sin(seconds * 3.3f + i * 2.1f)
            ink.fill.color = palette.glow
            ink.fill.alpha = (60 * flicker * strength).toInt()
            canvas.drawPath(cores[i], ink.fill)
            ink.fill.color = palette.core
            ink.fill.alpha = (200 * flicker * strength).toInt()
            canvas.drawCircle(c.x, c.y, hex * 0.2f, ink.fill)
        }

        // La réaction en chaîne : des neutrons d'un assemblage à son voisin.
        val rate = if (state.charging) 9f else 4f
        val tick = (seconds * rate).toInt()
        for (k in 0 until 4) {
            val event = tick - k
            val age = (seconds * rate - event) / 4f
            if (age !in 0f..1f) continue
            val from = cells[(noise(event * 13) * shown.coerceAtLeast(1)).toInt().coerceIn(0, cells.size - 1)]
            val angle = noise(event * 29) * 360f
            val to = kit.around(from, hex * sqrt(3f) * (0.6f + age), angle)
            val a = (1f - age) * strength * ignition
            ink.stroke.color = palette.core
            ink.stroke.alpha = (200 * a).toInt()
            ink.stroke.strokeWidth = 1f * density
            canvas.drawLine(from.x, from.y, to.x, to.y, ink.stroke)
            ink.halo(canvas, to, 2.4f * u, (220 * a).toInt())
            if (age < 0.25f) ink.halo(canvas, from, hex * 1.6f, (180 * (1f - age * 4f) * strength * ignition).toInt())
        }

        // Le trèfle s'éclaire en charge.
        if (state.charging) {
            val beat = (sin(seconds * 3f) + 1f) / 2f
            ink.halo(canvas, trefoil, 7f * u, ((50 + 120 * beat) * strength).toInt())
        }
    }
}
