package dev.levilainpetit.wux.wallpaper

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PointF
import android.graphics.RectF
import kotlin.math.abs
import kotlin.math.max

/**
 * Intelligence artificielle : une puce d'accélération (NPU) à la place de la
 * batterie, avec, gravé dessus, un réseau de neurones de cinq couches.
 *
 * - une inférence traverse le réseau couche après couche : les neurones
 *   activés s'allument et le signal court sur leurs connexions ; à la sortie,
 *   un neurone l'emporte ;
 * - en charge, le réseau apprend : après chaque passe, la correction remonte ;
 * - la jauge sur le côté de la puce suit la batterie.
 */
class NeuralCore(kit: CoreKit) : CoreArt(kit) {

    private val die = RectF(kit.area).apply { inset(5f * u, 5f * u) }
    private val sizes = listOf(5, 7, 8, 7, 4)

    /** Les neurones, couche par couche, du haut (entrée) vers le bas (sortie). */
    private val layers: List<List<PointF>> = sizes.mapIndexed { l, n ->
        val y = die.top + die.height() * (0.1f + 0.8f * l / (sizes.size - 1))
        val span = die.width() * 0.78f * (0.55f + 0.45f * n / sizes.max())
        (0 until n).map { i -> PointF(die.left + 3f * u + (die.width() - 6f * u - span) / 2f + 0f + span * (i + 0.5f) / n, y) }
    }

    private val gauge = RectF(die.right - 3.2f * u, die.top + 4f * u, die.right - 1.2f * u, die.bottom - 4f * u)

    override fun drawStatic(canvas: Canvas, line: Paint, thin: Paint, fill: Paint) {
        plate(canvas, line, thin)
        ribbon(canvas, kit.area.centerX() - 14f * u, kit.area.top, thin)
        ribbon(canvas, kit.area.centerX() + 14f * u, kit.area.top, thin)
        // La puce, ses broches tout autour, son repère d'angle.
        canvas.drawRoundRect(die, 1f * u, 1f * u, line)
        canvas.drawRoundRect(RectF(die).apply { inset(0.9f * u, 0.9f * u) }, 0.6f * u, 0.6f * u, thin)
        var x = die.left + 2.5f * u
        while (x < die.right - 2f * u) {
            canvas.drawLine(x, die.top - 1.6f * u, x, die.top, thin)
            canvas.drawLine(x, die.bottom, x, die.bottom + 1.6f * u, thin)
            x += 2.2f * u
        }
        var y = die.top + 2.5f * u
        while (y < die.bottom - 2f * u) {
            canvas.drawLine(die.left - 1.6f * u, y, die.left, y, thin)
            canvas.drawLine(die.right, y, die.right + 1.6f * u, y, thin)
            y += 2.2f * u
        }
        canvas.drawPath(kit.polygon(listOf(PointF(die.left + 1.6f * u, die.top + 1.6f * u), PointF(die.left + 4f * u, die.top + 1.6f * u), PointF(die.left + 1.6f * u, die.top + 4f * u))), thin)

        // Les connexions, en traits pâles, puis les neurones.
        val faint = Paint(thin).apply { alpha = 90 }
        layers.zipWithNext().forEach { (a, b) ->
            a.forEach { p -> b.forEach { q -> canvas.drawLine(p.x, p.y, q.x, q.y, faint) } }
        }
        layers.forEach { layer ->
            layer.forEach {
                canvas.drawCircle(it.x, it.y, 1.5f * u, line)
            }
        }
        // La jauge, dix cases.
        canvas.drawRect(gauge, thin)
        for (k in 1 until 10) {
            val yy = gauge.bottom - gauge.height() * k / 10f
            canvas.drawLine(gauge.left, yy, gauge.right, yy, thin)
        }
    }

    override fun outlines(): List<Path> =
        listOf(Path().apply { addRoundRect(die, 1f * u, 1f * u, Path.Direction.CW) }) +
            layers.flatten().map { Path().apply { addCircle(it.x, it.y, 1.5f * u, Path.Direction.CW) } }

    /** Le neurone i de la couche l est-il activé pendant la passe [pass] ? Toujours un gagnant en sortie. */
    private fun active(pass: Int, l: Int, i: Int): Boolean {
        if (l == layers.size - 1) return i == (noise(pass * 31 + 7) * layers[l].size).toInt().coerceAtMost(layers[l].size - 1)
        return noise(pass * 131 + l * 17 + i * 5) > 0.45f
    }

    private val point = PointF()

    override fun drawLive(canvas: Canvas, state: FrameState, ink: Ink) {
        val strength = strength(state)
        val ignition = state.ignition.coerceIn(0f, 1f)
        val seconds = state.timeMillis / 1000f
        val palette = ink.palette
        // Une passe : la vague traverse les couches (0 → 4), puis un temps de repos.
        val duration = if (state.charging) 1.6f else 2.6f
        val pass = (seconds / duration).toInt()
        val wave = (seconds / duration % 1f) * (layers.size + 1.5f)
        val learning = state.charging && wave > layers.size

        // Le signal sur les connexions entre couches actives.
        for (l in 0 until layers.size - 1) {
            val t = wave - l
            if (t !in 0f..1f) continue
            layers[l].forEachIndexed { i, p ->
                if (!active(pass, l, i)) return@forEachIndexed
                layers[l + 1].forEachIndexed { j, q ->
                    if (!active(pass, l + 1, j)) return@forEachIndexed
                    point.set(p.x + (q.x - p.x) * t, p.y + (q.y - p.y) * t)
                    ink.stroke.color = palette.glow
                    ink.stroke.alpha = (110 * strength * ignition).toInt()
                    ink.stroke.strokeWidth = 1.1f * density
                    canvas.drawLine(p.x, p.y, point.x, point.y, ink.stroke)
                    ink.fill.color = palette.core
                    ink.fill.alpha = (230 * strength * ignition).toInt()
                    canvas.drawCircle(point.x, point.y, 0.45f * u, ink.fill)
                }
            }
        }
        // En charge, la correction remonte, de la sortie vers l'entrée.
        if (learning) {
            val t = (wave - layers.size) / 1.5f
            val y = die.bottom - (die.height()) * t
            ink.stroke.color = palette.core
            ink.stroke.alpha = (120 * (1f - t) * strength).toInt()
            ink.stroke.strokeWidth = 1.2f * density
            canvas.drawLine(die.left + 1f * u, y, die.right - 4f * u, y, ink.stroke)
        }
        // Les neurones : allumés au passage de la vague, puis qui s'éteignent.
        layers.forEachIndexed { l, layer ->
            val since = wave - l
            val glow = if (since >= 0f) max(0f, 1f - since * 0.45f) else 0f
            layer.forEachIndexed { i, p ->
                val on = active(pass, l, i)
                val winner = l == layers.size - 1 && on
                val a = (if (on) 0.25f + 0.75f * glow else 0.12f) * strength * ignition * if (winner && since > 0) 1.2f else 1f
                if (on && glow > 0f) ink.halo(canvas, p, (if (winner) 5f else 3.2f) * u, (200 * a).toInt())
                ink.fill.color = palette.core
                ink.fill.alpha = (255 * a).toInt().coerceAtMost(255)
                canvas.drawCircle(p.x, p.y, 0.95f * u, ink.fill)
            }
        }
        // La jauge : une case par dixième de batterie, la dernière respire en charge.
        val cells = kotlin.math.ceil(state.batteryLevel.coerceIn(0f, 1f) * 10f).toInt().coerceIn(1, 10)
        val breath = if (state.charging) (kotlin.math.sin(seconds * 4.5f) + 1f) / 2f else 1f
        for (k in 0 until cells) {
            val a = if (k == cells - 1) 0.35f + 0.65f * breath else 1f
            ink.fill.color = palette.glow
            ink.fill.alpha = (170 * a * strength * ignition).toInt()
            val top = gauge.bottom - gauge.height() * (k + 1) / 10f
            canvas.drawRect(gauge.left + 0.3f * u, top + 0.3f * u, gauge.right - 0.3f * u, top + gauge.height() / 10f - 0.3f * u, ink.fill)
        }
        // Un léger battement de la puce entière.
        ink.halo(canvas, PointF(die.centerX(), die.centerY()), die.width() * 0.7f, (24 * strength * ignition * (1f + abs(kotlin.math.sin(seconds)))).toInt())
    }
}
