package dev.levilainpetit.wux.wallpaper

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PointF
import android.graphics.RectF
import android.graphics.Typeface
import kotlin.math.ceil
import kotlin.math.sin
import kotlin.random.Random

/**
 * Ghost in the Shell : un cyber-cerveau dans sa coque, à la place de la
 * batterie. Deux hémisphères dont les circonvolutions sont des pistes, une
 * coque hexagonale, le tronc qui descend vers quatre prises de nuque ; une
 * pluie de code tombe derrière.
 *
 * - le « ghost », une étincelle, erre de piste en piste dans le cerveau ;
 * - les prises s'allument selon la batterie (un quart chacune) ;
 * - en charge, la plongée : la pluie accélère et le ghost s'emballe.
 */
class GhostCore(kit: CoreKit) : CoreArt(kit) {

    private val center = kit.at(0.5f, 0.36f)
    private val shellRadius = kit.area.width() / 2f - 5f * u
    private val shell = kit.polygon((0 until 6).map { kit.around(center, shellRadius, 30f + it * 60f) })

    /** Les deux hémisphères : des ovales côte à côte. */
    private val lobeW = shellRadius * 0.74f
    private val lobeH = shellRadius * 0.98f
    private val left = RectF(center.x - lobeW * 1.02f, center.y - lobeH / 2f - 2f * u, center.x - 0.6f * u, center.y + lobeH / 2f - 2f * u)
    private val right = RectF(center.x + 0.6f * u, left.top, center.x + lobeW * 1.02f, left.bottom)

    /** Les circonvolutions : des pistes à angles droits, tirées au hasard (toujours le même) dans chaque lobe. */
    private val folds: List<CircuitScene.Route> = run {
        val random = Random(1995)
        listOf(left, right).flatMap { lobe ->
            (0 until 11).map {
                var x = lobe.left + lobe.width() * (0.2f + 0.6f * random.nextFloat())
                var y = lobe.top + lobe.height() * (0.15f + 0.7f * random.nextFloat())
                val points = mutableListOf(PointF(x, y))
                repeat(5) { k ->
                    val step = (2f + random.nextFloat() * 4f) * u
                    if (k % 2 == 0) x += if (random.nextBoolean()) step else -step else y += if (random.nextBoolean()) step else -step
                    // Rester dans l'ovale.
                    val dx = (x - lobe.centerX()) / (lobe.width() / 2f * 0.85f)
                    val dy = (y - lobe.centerY()) / (lobe.height() / 2f * 0.85f)
                    if (dx * dx + dy * dy > 1f) {
                        x = lobe.centerX() + (x - lobe.centerX()) * 0.6f
                        y = lobe.centerY() + (y - lobe.centerY()) * 0.6f
                    }
                    points += PointF(x, y)
                }
                CircuitScene.Route(points)
            }
        }
    }

    /** Les quatre prises de nuque, sous le tronc. */
    private val ports: List<RectF> = (0 until 4).map { k ->
        val x = kit.at(0.5f, 0f).x + (k - 1.5f) * 9f * u
        val y = kit.at(0.5f, 0.86f).y
        RectF(x - 3f * u, y - 3.2f * u, x + 3f * u, y + 3.2f * u)
    }
    private val stem = PointF(center.x, left.bottom)

    /** Les colonnes de la pluie de code : leur abscisse et leur vitesse. */
    private val columns = (0 until 11).map { k ->
        val x = kit.area.left + 4f * u + (kit.area.width() - 8f * u) * k / 10f
        x to (0.6f + noise(k * 3 + 1) * 0.8f)
    }
    private val glyphs = "ｱｲｳｴｵｶｷｸｹｺｻｼｽｾｿﾀﾁﾂﾃﾄﾅﾆﾇﾈﾉ0123456789"
    private val glyphPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        typeface = Typeface.MONOSPACE
        textAlign = Paint.Align.CENTER
        textSize = 2.6f * u
    }

    override fun drawStatic(canvas: Canvas, line: Paint, thin: Paint, fill: Paint) {
        plate(canvas, line, thin)
        ribbon(canvas, center.x - 13f * u, kit.area.top, thin)
        ribbon(canvas, center.x + 13f * u, kit.area.top, thin)
        // La coque, doublée, et ses attaches.
        canvas.drawPath(shell, line)
        canvas.drawPath(kit.polygon((0 until 6).map { kit.around(center, shellRadius * 0.93f, 30f + it * 60f) }), thin)
        for (k in 0 until 6) {
            val at = kit.around(center, shellRadius * 0.965f, 30f + k * 60f)
            canvas.drawCircle(at.x, at.y, 0.6f * u, thin)
        }
        // Les hémisphères et leurs circonvolutions.
        canvas.drawOval(left, line)
        canvas.drawOval(right, line)
        folds.forEach { route ->
            val p = route.points
            for (i in 0 until p.size - 1) canvas.drawLine(p[i].x, p[i].y, p[i + 1].x, p[i + 1].y, thin)
            canvas.drawCircle(p.first().x, p.first().y, 0.45f * u, thin)
            canvas.drawCircle(p.last().x, p.last().y, 0.45f * u, thin)
        }
        // Le tronc, puis un câble vers chaque prise.
        canvas.drawLine(stem.x - 1.2f * u, stem.y, stem.x - 1.2f * u, stem.y + 8f * u, line)
        canvas.drawLine(stem.x + 1.2f * u, stem.y, stem.x + 1.2f * u, stem.y + 8f * u, line)
        val hub = PointF(stem.x, stem.y + 8f * u)
        canvas.drawCircle(hub.x, hub.y, 1.6f * u, line)
        ports.forEach { port ->
            canvas.drawLine(hub.x, hub.y, port.centerX(), port.top, thin)
            canvas.drawRoundRect(port, 1.2f * u, 1.2f * u, line)
            for (k in -1..1) canvas.drawCircle(port.centerX() + k * 1.5f * u, port.centerY(), 0.45f * u, thin)
        }
    }

    override fun outlines(): List<Path> =
        listOf(Path(shell), Path().apply { addOval(left, Path.Direction.CW) }, Path().apply { addOval(right, Path.Direction.CW) }) +
            ports.map { Path().apply { addRoundRect(it, 1.2f * u, 1.2f * u, Path.Direction.CW) } }

    private val point = PointF()

    override fun drawLive(canvas: Canvas, state: FrameState, ink: Ink) {
        val strength = strength(state)
        val ignition = state.ignition.coerceIn(0f, 1f)
        val seconds = state.timeMillis / 1000f
        val palette = ink.palette
        val dive = if (state.charging) 2.2f else 1f

        // La pluie de code, derrière la coque.
        val area = kit.area
        val step = glyphPaint.textSize * 1.15f
        val rows = ((area.height() - 4f * u) / step).toInt()
        glyphPaint.color = palette.line
        columns.forEachIndexed { c, (x, speed) ->
            val head = (seconds * speed * dive * 6f + noise(c * 11) * rows) % (rows + 8)
            for (r in 0 until rows) {
                val behind = head - r
                if (behind < 0f || behind > 8f) continue
                val fade = 1f - behind / 8f
                val glyph = glyphs[(noise(c * 97 + r * 13 + (seconds * 2f).toInt()) * glyphs.length).toInt().coerceAtMost(glyphs.length - 1)]
                glyphPaint.color = if (behind < 1f) palette.core else palette.line
                glyphPaint.alpha = ((if (behind < 1f) 255 else 150) * fade * strength * ignition).toInt()
                canvas.drawText(glyph.toString(), x, area.top + 3f * u + r * step, glyphPaint)
            }
        }

        // Le cerveau respire doucement.
        val breath = (sin(seconds * 1.2f) + 1f) / 2f
        ink.halo(canvas, center, shellRadius * (0.9f + 0.1f * breath), ((40 + 30 * breath) * strength * ignition).toInt())

        // Le ghost : une étincelle qui suit une circonvolution, puis passe à une autre.
        val travel = seconds * 0.55f * dive
        val which = travel.toInt()
        val t = travel % 1f
        val route = folds[(noise(which * 5) * folds.size).toInt().coerceAtMost(folds.size - 1)]
        for (k in 0 until 6) {
            val tt = (t - k * 0.04f).coerceAtLeast(0f)
            route.at(tt, point)
            val a = (1f - k / 6f) * strength * ignition
            if (k == 0) ink.halo(canvas, point, 3.4f * u, (230 * a).toInt())
            ink.fill.color = palette.core
            ink.fill.alpha = (255 * a).toInt()
            canvas.drawCircle(point.x, point.y, (0.6f - k * 0.07f) * u, ink.fill)
        }

        // Les prises : une par quart de batterie, la dernière respire en charge.
        val lit = ceil(state.batteryLevel.coerceIn(0f, 1f) * ports.size).toInt().coerceIn(1, ports.size)
        val pulse = if (state.charging) (sin(seconds * 4.5f) + 1f) / 2f else 1f
        ports.forEachIndexed { i, port ->
            val on = ((ignition - 0.15f * i) / 0.3f).coerceIn(0f, 1f)
            val a = on * strength * when {
                i < lit - 1 -> 1f
                i == lit - 1 -> 0.35f + 0.65f * pulse
                else -> 0.06f
            }
            ink.fill.color = palette.glow
            ink.fill.alpha = (120 * a).toInt()
            canvas.drawRoundRect(port, 1.2f * u, 1.2f * u, ink.fill)
            ink.halo(canvas, PointF(port.centerX(), port.centerY()), 4f * u, (150 * a).toInt())
        }
    }
}
