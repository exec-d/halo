package dev.levilainpetit.wux.wallpaper

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PointF
import android.graphics.RectF
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.max
import kotlin.math.sin

/**
 * Physique quantique : le « lustre » d'un ordinateur quantique, le
 * réfrigérateur à dilution, suspendu à la place de la batterie. Cinq étages
 * de plaques de plus en plus froides, reliés par des tiges et par des câbles
 * coaxiaux qui font une boucle à chaque étage ; tout en bas, la chambre de
 * mélange et la puce à cinq qubits.
 *
 * - les étages s'allument selon la batterie (un par cinquième) ;
 * - des photons de commande descendent les câbles, plus vite en charge ;
 * - les qubits oscillent entre deux états, et les paires intriquées
 *   s'allument ensemble.
 */
class QuantumCore(kit: CoreKit) : CoreArt(kit) {

    private val cx = kit.area.centerX()

    /** Les plaques : hauteur (fraction), demi-largeur (unités). */
    private val plates = listOf(0.08f to 25f, 0.22f to 22f, 0.36f to 19f, 0.49f to 16f, 0.61f to 13f).map { (fy, half) ->
        Plate(kit.at(0.5f, fy).y, half * u, half * u * 0.2f)
    }

    private class Plate(val y: Float, val rx: Float, val ry: Float)

    private fun oval(cx: Float, p: Plate, dy: Float = 0f) = RectF(cx - p.rx, p.y - p.ry + dy, cx + p.rx, p.y + p.ry + dy)

    /** Les câbles coaxiaux, de la plaque du haut à la puce, en fraction de la demi-largeur. */
    private val coaxOffsets = listOf(-0.55f, -0.2f, 0.2f, 0.55f)

    private val chamber = RectF(cx - 7f * u, plates.last().y + 1.2f * u, cx + 7f * u, kit.at(0.5f, 0.69f).y)
    private val chip = RectF(cx - 9f * u, kit.at(0.5f, 0.72f).y, cx + 9f * u, kit.at(0.5f, 0.72f).y + 12f * u)

    /** Les cinq qubits en croix, et les couplages entre eux. */
    private val qubits = listOf(0f to 0f, -5.2f to 0f, 5.2f to 0f, 0f to -4.2f, 0f to 4.2f).map { (dx, dy) ->
        PointF(chip.centerX() + dx * u, chip.centerY() + dy * u)
    }
    private val couplers = listOf(0 to 1, 0 to 2, 0 to 3, 0 to 4)

    /** Le trajet d'un photon le long d'un câble : de plaque en plaque, puis vers son qubit. */
    private val photonRoutes: List<CircuitScene.Route> = coaxOffsets.mapIndexed { k, f ->
        val points = plates.map { PointF(cx + f * it.rx * 0.9f, it.y) }.toMutableList()
        points += PointF(cx + f * 9f * u, chamber.bottom)
        points += qubits[k + 1]
        CircuitScene.Route(points)
    }

    override fun drawStatic(canvas: Canvas, line: Paint, thin: Paint, fill: Paint) {
        plate(canvas, line, thin)
        // Suspendu à la carte mère par deux nappes.
        ribbon(canvas, cx - 16f * u, kit.area.top, thin)
        ribbon(canvas, cx + 16f * u, kit.area.top, thin)
        canvas.drawLine(cx - 16f * u, kit.area.top, cx - plates[0].rx * 0.8f, plates[0].y, thin)
        canvas.drawLine(cx + 16f * u, kit.area.top, cx + plates[0].rx * 0.8f, plates[0].y, thin)

        // Les tiges entre les étages.
        plates.zipWithNext().forEach { (a, b) ->
            for (f in floatArrayOf(-0.8f, 0.8f)) {
                canvas.drawLine(cx + f * b.rx, a.y + a.ry, cx + f * b.rx, b.y - b.ry, line)
            }
            canvas.drawLine(cx, a.y + a.ry, cx, b.y - b.ry, thin)
        }
        // Les plaques, en épaisseur.
        plates.forEach { p ->
            canvas.drawOval(oval(cx, p), line)
            canvas.drawOval(oval(cx, p, 1.1f * u), thin)
            canvas.drawLine(cx - p.rx, p.y, cx - p.rx, p.y + 1.1f * u, thin)
            canvas.drawLine(cx + p.rx, p.y, cx + p.rx, p.y + 1.1f * u, thin)
        }
        // Les câbles coaxiaux, une boucle de détente à chaque étage.
        for (route in photonRoutes) {
            val pts = route.points
            for (i in 0 until pts.size - 1) canvas.drawLine(pts[i].x, pts[i].y, pts[i + 1].x, pts[i + 1].y, thin)
            pts.subList(1, plates.size).forEach { at ->
                val loop = 1.3f * u
                canvas.drawOval(RectF(at.x - loop, at.y - loop * 2.4f, at.x + loop, at.y - loop * 0.6f), thin)
            }
        }
        // La chambre de mélange, et ses blindages.
        canvas.drawRoundRect(chamber, 1.2f * u, 1.2f * u, line)
        var y = chamber.top + 2f * u
        while (y < chamber.bottom - 1f * u) {
            canvas.drawLine(chamber.left, y, chamber.right, y, thin)
            y += 2f * u
        }
        // La puce : ses plots, ses résonateurs en méandres, ses qubits.
        canvas.drawRoundRect(chip, 0.8f * u, 0.8f * u, line)
        for (k in 0 until 6) {
            val x = chip.left + (1.5f + k * 3f) * u
            canvas.drawRect(x - 0.5f * u, chip.top - 1f * u, x + 0.5f * u, chip.top, thin)
            canvas.drawRect(x - 0.5f * u, chip.bottom, x + 0.5f * u, chip.bottom + 1f * u, thin)
        }
        couplers.forEach { (a, b) -> meander(canvas, qubits[a], qubits[b], thin) }
        qubits.forEach {
            canvas.drawCircle(it.x, it.y, 1.5f * u, line)
            canvas.drawCircle(it.x, it.y, 0.6f * u, thin)
        }
    }

    /** Un résonateur entre deux qubits : un fil en zigzag. */
    private fun meander(canvas: Canvas, a: PointF, b: PointF, thin: Paint) {
        val path = Path().apply { moveTo(a.x, a.y) }
        val n = 7
        val dx = b.x - a.x
        val dy = b.y - a.y
        val len = kotlin.math.hypot(dx, dy)
        val nx = -dy / len
        val ny = dx / len
        for (k in 1 until n) {
            val t = k / n.toFloat()
            val side = if (k % 2 == 0) 0.8f * u else -0.8f * u
            path.lineTo(a.x + dx * t + nx * side, a.y + dy * t + ny * side)
        }
        path.lineTo(b.x, b.y)
        canvas.drawPath(path, thin)
    }

    override fun outlines(): List<Path> =
        plates.map { p -> Path().apply { addOval(oval(cx, p), Path.Direction.CW) } } +
            Path().apply { addRoundRect(chip, 0.8f * u, 0.8f * u, Path.Direction.CW) }

    private val point = PointF()

    override fun drawLive(canvas: Canvas, state: FrameState, ink: Ink) {
        val strength = strength(state)
        val seconds = state.timeMillis / 1000f
        val ignition = state.ignition.coerceIn(0f, 1f)
        val lit = ceil(state.batteryLevel.coerceIn(0f, 1f) * plates.size).toInt().coerceIn(1, plates.size)
        val palette = ink.palette

        // Une lueur froide autour du lustre.
        ink.halo(canvas, PointF(cx, plates[2].y), kit.area.width() * 0.55f, (40 * strength * ignition).toInt())

        // Les étages : du haut vers le bas, autant que de cinquièmes de batterie.
        plates.forEachIndexed { i, p ->
            val on = ((ignition - 0.12f * i) / 0.25f).coerceIn(0f, 1f)
            val breath = if (state.charging && i == lit - 1) (sin(seconds * PI.toFloat() * 1.4f) + 1f) / 2f else 1f
            val a = on * strength * if (i < lit) 0.55f + 0.45f * breath else 0.08f
            ink.fill.color = palette.glow
            ink.fill.alpha = (28 * a).toInt()
            canvas.drawOval(oval(cx, p), ink.fill)
            ink.stroke.color = palette.core
            ink.stroke.alpha = (220 * a).toInt()
            ink.stroke.strokeWidth = 1.3f * density
            canvas.drawOval(oval(cx, p), ink.stroke)
        }

        // Les photons qui descendent les câbles.
        val speed = if (state.charging) 0.5f else 0.22f
        photonRoutes.forEachIndexed { k, route ->
            for (j in 0 until 2) {
                val t = (seconds * speed + k * 0.27f + j * 0.5f) % 1f
                route.at(t, point)
                val a = (sin(t * PI.toFloat()) * strength * ignition)
                ink.halo(canvas, point, 2.2f * u, (170 * a).toInt())
                ink.fill.color = palette.core
                ink.fill.alpha = (255 * a).toInt()
                canvas.drawCircle(point.x, point.y, 0.45f * u, ink.fill)
            }
        }

        // Les qubits : chacun oscille à sa fréquence ; les couplages brillent
        // quand les deux qubits sont dans le même état.
        val states = qubits.indices.map { i -> (sin(seconds * (1.3f + i * 0.37f) + i * 1.9f) + 1f) / 2f }
        couplers.forEach { (a, b) ->
            val together = max(0f, 1f - abs(states[a] - states[b]) * 3f)
            ink.stroke.color = palette.glow
            ink.stroke.alpha = (160 * together * strength * ignition).toInt()
            ink.stroke.strokeWidth = 2.2f * density
            canvas.drawLine(qubits[a].x, qubits[a].y, qubits[b].x, qubits[b].y, ink.stroke)
        }
        qubits.forEachIndexed { i, q ->
            val s = states[i]
            ink.halo(canvas, q, (2.4f + 1.6f * s) * u, ((60 + 170 * s) * strength * ignition).toInt())
            ink.fill.color = palette.core
            ink.fill.alpha = ((90 + 165 * s) * strength * ignition).toInt()
            canvas.drawCircle(q.x, q.y, 0.75f * u, ink.fill)
        }
    }
}
