package dev.levilainpetit.wux.wallpaper

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PointF
import android.graphics.RectF
import kotlin.math.ceil
import kotlin.math.sin

/**
 * Fallout : une porte d'abri antiatomique, la grande roue dentée, à la place
 * de la batterie, avec son vérin et son cadre ; dessous, un compteur Geiger
 * à aiguille. L'écran est un vieux tube cathodique vert.
 *
 * - les dix dents de la porte s'allument selon la batterie ;
 * - en charge, les verrous du moyeu tournent : la porte s'ouvre ;
 * - l'aiguille du compteur tremble et le compteur crépite ;
 * - une ligne de balayage descend l'écran, comme sur un tube.
 */
class VaultCore(kit: CoreKit) : CoreArt(kit) {

    private val center = kit.at(0.5f, 0.35f)
    private val radius = kit.area.width() / 2f - 9f * u
    private val teeth = 10
    private val dial = kit.at(0.5f, 0.84f)
    private val dialRadius = 9f * u

    private fun tooth(i: Int): List<PointF> {
        val a = i * 360f / teeth
        val half = 360f / teeth * 0.27f
        return listOf(
            kit.around(center, radius * 0.86f, a - half * 1.25f),
            kit.around(center, radius, a - half),
            kit.around(center, radius, a + half),
            kit.around(center, radius * 0.86f, a + half * 1.25f),
        )
    }

    /** Le contour de la roue : dents et creux. */
    private val gear: Path = Path().apply {
        for (i in 0 until teeth) {
            val t = tooth(i)
            if (i == 0) moveTo(t[0].x, t[0].y) else lineTo(t[0].x, t[0].y)
            t.drop(1).forEach { lineTo(it.x, it.y) }
            // Le creux, à mi-chemin de la dent suivante.
            val root = kit.around(center, radius * 0.86f, (i + 0.5f) * 360f / teeth)
            lineTo(root.x, root.y)
        }
        close()
    }
    private val toothPaths = (0 until teeth).map { kit.polygon(tooth(it)) }

    override fun drawStatic(canvas: Canvas, line: Paint, thin: Paint, fill: Paint) {
        plate(canvas, line, thin)
        ribbon(canvas, center.x - 18f * u, kit.area.top, thin)
        ribbon(canvas, center.x + 18f * u, kit.area.top, thin)
        // Le cadre de la porte, dans la roche.
        val frame = RectF(center.x - radius * 1.12f, center.y - radius * 1.12f, center.x + radius * 1.12f, center.y + radius * 1.12f)
        canvas.drawRoundRect(frame, 3f * u, 3f * u, thin)
        canvas.drawCircle(center.x, center.y, radius * 1.04f, thin)
        // La roue dentée, son anneau de boulons, son moyeu.
        canvas.drawPath(gear, line)
        canvas.drawCircle(center.x, center.y, radius * 0.78f, line)
        for (k in 0 until 20) {
            val at = kit.around(center, radius * 0.72f, k * 18f)
            canvas.drawCircle(at.x, at.y, 0.5f * u, thin)
        }
        canvas.drawCircle(center.x, center.y, radius * 0.64f, thin)
        canvas.drawCircle(center.x, center.y, radius * 0.24f, line)
        canvas.drawCircle(center.x, center.y, radius * 0.12f, thin)
        // Le vérin d'ouverture, à droite.
        val hinge = PointF(frame.right + 2f * u, center.y + radius * 0.3f)
        canvas.drawCircle(hinge.x, hinge.y, 1.6f * u, line)
        val arm = kit.around(center, radius * 0.9f, 20f)
        canvas.drawLine(hinge.x, hinge.y - 1.2f * u, arm.x, arm.y - 1.2f * u, thin)
        canvas.drawLine(hinge.x, hinge.y + 1.2f * u, arm.x, arm.y + 1.2f * u, thin)
        canvas.drawRect(RectF(hinge.x - 6f * u, hinge.y - 1.6f * u, hinge.x - 2f * u, hinge.y + 1.6f * u), thin)

        // Le compteur Geiger : cadran en demi-cercle, graduations, étiquette.
        val box = RectF(dial.x - dialRadius * 1.35f, dial.y - dialRadius * 1.25f, dial.x + dialRadius * 1.35f, dial.y + 1.5f * u)
        canvas.drawRoundRect(box, 1.2f * u, 1.2f * u, line)
        canvas.drawArc(RectF(dial.x - dialRadius, dial.y - dialRadius, dial.x + dialRadius, dial.y + dialRadius), 200f, 140f, false, thin)
        for (k in 0..10) {
            val a = 200f + k * 14f
            val p0 = kit.around(dial, dialRadius * (if (k % 5 == 0) 0.78f else 0.86f), a)
            val p1 = kit.around(dial, dialRadius, a)
            canvas.drawLine(p0.x, p0.y, p1.x, p1.y, thin)
        }
        canvas.drawCircle(dial.x, dial.y, 0.9f * u, line)
        val text = Paint(fill).apply {
            textSize = 2.4f * u
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText("RAD", dial.x, dial.y - dialRadius * 0.35f, text)
    }

    override fun outlines(): List<Path> =
        listOf(Path(gear), Path().apply { addCircle(dial.x, dial.y, dialRadius, Path.Direction.CW) })

    override fun drawLive(canvas: Canvas, state: FrameState, ink: Ink) {
        val strength = strength(state)
        val ignition = state.ignition.coerceIn(0f, 1f)
        val seconds = state.timeMillis / 1000f
        val palette = ink.palette

        ink.halo(canvas, center, radius * 1.1f, (36 * strength * ignition).toInt())

        // Les dents : une par dixième de batterie, la dernière respire en charge.
        val lit = ceil(state.batteryLevel.coerceIn(0f, 1f) * teeth).toInt().coerceIn(1, teeth)
        val breath = if (state.charging) (sin(seconds * 4.5f) + 1f) / 2f else 1f
        toothPaths.forEachIndexed { i, tooth ->
            // Dans le sens horaire, à partir du haut.
            val order = (i + teeth - 7) % teeth
            val on = ((ignition - 0.06f * order) / 0.2f).coerceIn(0f, 1f)
            val a = on * strength * when {
                order < lit - 1 -> 1f
                order == lit - 1 -> 0.35f + 0.65f * breath
                else -> 0.06f
            }
            ink.fill.color = palette.glow
            ink.fill.alpha = (95 * a).toInt()
            canvas.drawPath(tooth, ink.fill)
            ink.stroke.color = palette.core
            ink.stroke.alpha = (210 * a).toInt()
            ink.stroke.strokeWidth = 1.2f * density
            canvas.drawPath(tooth, ink.stroke)
        }

        // Les verrous du moyeu : trois bras, qui tournent quand la porte s'ouvre (en charge).
        val turn = if (state.charging) seconds * 40f else 0f
        ink.stroke.color = palette.core
        ink.stroke.alpha = (200 * strength * ignition).toInt()
        ink.stroke.strokeWidth = 1.6f * density
        for (k in 0 until 3) {
            val a = turn + k * 120f - 90f
            val p0 = kit.around(center, radius * 0.24f, a)
            val p1 = kit.around(center, radius * 0.6f, a)
            canvas.drawLine(p0.x, p0.y, p1.x, p1.y, ink.stroke)
            ink.fill.color = palette.core
            ink.fill.alpha = (220 * strength * ignition).toInt()
            canvas.drawCircle(p1.x, p1.y, 1.1f * u, ink.fill)
        }

        // Le compteur : l'aiguille tremble autour d'un niveau bas, avec des pics.
        val tick = (seconds * 12f).toInt()
        val click = noise(tick * 7) > 0.82f
        val level = 0.18f + 0.1f * sin(seconds * 0.7f) + noise(tick) * 0.08f + if (click) 0.2f else 0f
        val needle = 200f + 140f * level.coerceIn(0f, 1f)
        val tip = kit.around(dial, dialRadius * 0.92f, needle)
        ink.stroke.color = palette.core
        ink.stroke.alpha = (235 * strength * ignition).toInt()
        ink.stroke.strokeWidth = 1.1f * density
        canvas.drawLine(dial.x, dial.y, tip.x, tip.y, ink.stroke)
        if (click) ink.halo(canvas, dial, dialRadius * 0.8f, (90 * strength).toInt())

        // La ligne de balayage du tube, qui descend toute la platine.
        val sweep = (seconds / 3.2f) % 1f
        val y = kit.area.top + kit.area.height() * sweep
        ink.stroke.color = palette.glow
        ink.stroke.alpha = (60 * strength * ignition).toInt()
        ink.stroke.strokeWidth = 2.4f * density
        canvas.drawLine(kit.area.left + 1.5f * u, y, kit.area.right - 1.5f * u, y, ink.stroke)
    }
}
