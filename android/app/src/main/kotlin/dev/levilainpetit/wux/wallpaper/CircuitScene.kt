package dev.levilainpetit.wux.wallpaper

import android.graphics.Bitmap
import android.graphics.BlurMaskFilter
import android.graphics.Canvas
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PointF
import android.graphics.RectF
import kotlin.math.hypot
import kotlin.random.Random

/**
 * L'intérieur du téléphone, dessiné en schéma néon : châssis, bobine de
 * recharge, batterie, carte mère et carte du bas, pistes.
 *
 * Tout ce qui ne bouge pas est rendu une fois, en masques [Bitmap.Config.ALPHA_8]
 * teintés au moment du dessin : la couleur suit le téléphone sans rien
 * redessiner. Trois plans à des profondeurs différentes donnent la parallaxe.
 *
 * Les coordonnées sont en « unités » : un centième de la largeur de l'écran.
 */
class CircuitScene private constructor(
    val width: Int,
    val height: Int,
    /** Les plans, du plus profond au plus proche du verre. */
    val layers: List<Layer>,
    /** Intérieur de la batterie, pour son niveau (plan [BATTERY]). */
    val batteryCell: RectF,
    /** Centre du processeur : l'allumage part de là. */
    val origin: PointF,
    /** Composants et pistes qui s'illuminent à l'allumage, du plus proche du processeur au plus loin. */
    val parts: List<Part>,
    /** Antennes le long du châssis (plan [BOARD]), allumées selon le signal. */
    val antennas: Path,
    /** Parcours des impulsions liées au réseau : antenne → modem → processeur. */
    val networkRoutes: List<Route>,
    /** Parcours processeur → port USB, parcourus à l'envers pendant la charge. */
    val dataRoutes: List<Route>,
    /** Petits parcours internes pour le battement de cœur du processeur. */
    val innerRoutes: List<Route>,
    val density: Float,
) {

    class Layer(
        /** 0 : collé au verre ; 1 : au fond du téléphone. */
        val depth: Float,
        val core: Bitmap,
        /** Halo flou, à demi-résolution : dessiné étiré ×[GLOW_SCALE]. */
        val glow: Bitmap,
        val glowOffsetX: Float,
        val glowOffsetY: Float,
        /** Intensité du plan (0–255) : le fond est plus sombre. */
        val alpha: Int,
    )

    /**
     * Un élément qui s'illumine à l'allumage : sa forme, son plan, et son
     * moment, de 0 (le processeur) à 1 (le plus loin).
     */
    class Part(val path: Path, val layer: Int, val delay: Float)

    /** Une ligne brisée parcourue par une impulsion. */
    class Route(val points: List<PointF>) {
        private val lengths = FloatArray(points.size).also {
            for (i in 1 until points.size) {
                it[i] = it[i - 1] + hypot(points[i].x - points[i - 1].x, points[i].y - points[i - 1].y)
            }
        }
        val length: Float get() = lengths.last()

        /** Position à la fraction [t] (0–1) du parcours. */
        fun at(t: Float, out: PointF) {
            val target = t.coerceIn(0f, 1f) * length
            var i = 1
            while (i < points.size - 1 && lengths[i] < target) i++
            val span = (lengths[i] - lengths[i - 1]).takeIf { it > 0f } ?: 1f
            val f = ((target - lengths[i - 1]) / span).coerceIn(0f, 1f)
            out.set(
                points[i - 1].x + (points[i].x - points[i - 1].x) * f,
                points[i - 1].y + (points[i].y - points[i - 1].y) * f,
            )
        }

        fun reversed() = Route(points.reversed())
    }

    fun recycle() = layers.forEach {
        it.core.recycle()
        it.glow.recycle()
    }

    companion object {
        const val CHASSIS = 0
        const val BATTERY = 1
        const val BOARD = 2
        const val GLOW_SCALE = 2

        fun build(width: Int, height: Int, density: Float): CircuitScene = Builder(width, height, density).build()
    }

    private class Builder(val width: Int, val height: Int, val density: Float) {
        val u = width / 100f
        /** Hauteur de l'écran en unités (≈ 222 sur un téléphone 20:9). */
        val h = height / u

        val line = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = 1.1f * density
            strokeJoin = Paint.Join.ROUND
            strokeCap = Paint.Cap.ROUND
        }
        val thin = Paint(line).apply { strokeWidth = 0.7f * density }
        val fill = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }

        fun x(v: Float) = v * u
        fun r(l: Float, t: Float, rt: Float, b: Float) = RectF(l * u, t * u, rt * u, b * u)
        fun p(px: Float, py: Float) = PointF(px * u, py * u)

        // Plans, en unités.
        val margin = 3f
        val board = r(7f, 0.045f * h, 93f, 0.36f * h)
        val battery = r(10f, 0.40f * h, 78f, 0.80f * h)
        val bottom = r(7f, 0.84f * h, 93f, 0.955f * h)

        // Composants de la carte mère, relatifs à son bord haut.
        val top = board.top / u
        val camera1 = p(22f, top + 13f)
        val camera2 = p(22f, top + 32f)
        val soc = r(49f, top + 19f, 71f, top + 41f)
        val ram = r(49f, top + 44f, 71f, top + 52f)
        val modem = r(12f, top + 46f, 26f, top + 60f)
        val pmic = r(32f, top + 50f, 42f, top + 60f)
        val connector = r(78f, board.bottom / u - 8f, 90f, board.bottom / u - 4f)
        val bottomTop = bottom.top / u
        val bottomConnector = r(78f, bottomTop + 1f, 90f, bottomTop + 4.5f)
        val usb = r(40f, bottom.bottom / u - 7f, 60f, bottom.bottom / u - 1.5f)
        val motor = r(38f, bottomTop + 3.5f, 58f, bottomTop + 12f)

        val traces = mutableListOf<List<PointF>>()

        fun build(): CircuitScene {
            val network = networkRoutes()
            val data = dataRoutes()
            val inner = innerRoutes()
            val layers = listOf(
                layer(depth = 1f, alpha = 90) { chassis(it) },
                layer(depth = 0.55f, alpha = 150) { battery(it) },
                layer(depth = 0.2f, alpha = 230) { board(it) },
            )
            val cell = RectF(battery).apply { inset(2.2f * u, 2.2f * u) }
            return CircuitScene(
                width = width,
                height = height,
                layers = layers,
                batteryCell = cell,
                origin = PointF(soc.centerX(), soc.centerY()),
                parts = parts(),
                antennas = antennas(),
                networkRoutes = network,
                dataRoutes = data,
                innerRoutes = inner,
                density = density,
            )
        }

        private fun parts(): List<Part> {
            val shapes = mutableListOf<Pair<Path, Int>>()
            fun rect(rect: RectF, radius: Float, layer: Int = BOARD) {
                shapes += Path().apply { addRoundRect(rect, radius * u, radius * u, Path.Direction.CW) } to layer
            }
            listOf(soc, ram, modem, pmic).forEach { rect(it, 0.8f) }
            listOf(connector, bottomConnector).forEach { rect(it, 0f) }
            rect(usb, 2.7f)
            rect(motor, 2f)
            rect(battery, 4f, BATTERY)
            for ((center, radius) in listOf(camera1 to 8f, camera2 to 6.5f)) {
                shapes += Path().apply { addCircle(center.x, center.y, radius * u, Path.Direction.CW) } to BOARD
            }
            traces.forEach { points ->
                shapes += Path().apply {
                    moveTo(points[0].x, points[0].y)
                    points.drop(1).forEach { lineTo(it.x, it.y) }
                } to BOARD
            }
            val origin = PointF(soc.centerX(), soc.centerY())
            val bounds = RectF()
            val sorted = shapes.sortedBy { (path, _) ->
                path.computeBounds(bounds, true)
                hypot(bounds.centerX() - origin.x, bounds.centerY() - origin.y)
            }
            val last = (sorted.size - 1).coerceAtLeast(1).toFloat()
            return sorted.mapIndexed { i, (path, layer) -> Part(path, layer, i / last) }
        }

        private fun layer(depth: Float, alpha: Int, draw: (Canvas) -> Unit): Layer {
            val core = Bitmap.createBitmap(width, height, Bitmap.Config.ALPHA_8)
            draw(Canvas(core))
            val small = Bitmap.createScaledBitmap(core, width / GLOW_SCALE, height / GLOW_SCALE, true)
            val offset = IntArray(2)
            val blur = Paint().apply { maskFilter = BlurMaskFilter(5f * density, BlurMaskFilter.Blur.NORMAL) }
            val glow = small.extractAlpha(blur, offset)
            small.recycle()
            return Layer(
                depth = depth,
                core = core,
                glow = glow,
                glowOffsetX = offset[0] * GLOW_SCALE.toFloat(),
                glowOffsetY = offset[1] * GLOW_SCALE.toFloat(),
                alpha = alpha,
            )
        }

        // ——— Plan du fond : châssis, vis, bobine de recharge sans fil ———

        private fun chassis(canvas: Canvas) {
            val frame = r(margin, margin, 100f - margin, h - margin)
            canvas.drawRoundRect(frame, 11f * u, 11f * u, line)
            canvas.drawRoundRect(RectF(frame).apply { inset(1.4f * u, 1.4f * u) }, 9.6f * u, 9.6f * u, thin)

            val coil = PointF(battery.centerX(), battery.centerY())
            for (i in 0 until 8) canvas.drawCircle(coil.x, coil.y, (22f - i * 1.6f) * u, thin)
            canvas.drawLine(coil.x - 1.5f * u, coil.y + 22f * u, coil.x - 1.5f * u, coil.y + 27f * u, thin)
            canvas.drawLine(coil.x + 1.5f * u, coil.y + 22f * u, coil.x + 1.5f * u, coil.y + 27f * u, thin)

            listOf(
                p(11f, top + 3f), p(89f, top + 3f), p(11f, board.bottom / u - 3f),
                p(11f, bottomTop + 13f), p(89f, bottomTop + 13f),
            ).forEach { screw(canvas, it) }
        }

        private fun screw(canvas: Canvas, at: PointF) {
            canvas.drawCircle(at.x, at.y, 1.3f * u, thin)
            canvas.drawLine(at.x - 0.7f * u, at.y, at.x + 0.7f * u, at.y, thin)
            canvas.drawLine(at.x, at.y - 0.7f * u, at.x, at.y + 0.7f * u, thin)
        }

        // ——— Plan du milieu : la batterie ———

        private fun battery(canvas: Canvas) {
            canvas.drawRoundRect(battery, 4f * u, 4f * u, line)
            canvas.drawRoundRect(RectF(battery).apply { inset(1.4f * u, 1.4f * u) }, 3f * u, 3f * u, thin)
            // Languette et bornes.
            val tab = RectF(battery.centerX() - 10f * u, battery.top - 2.4f * u, battery.centerX() + 10f * u, battery.top)
            canvas.drawRect(tab, thin)
            canvas.drawRect(RectF(tab.left + 2f * u, tab.top + 0.6f * u, tab.left + 6f * u, tab.bottom - 0.6f * u), thin)
            canvas.drawRect(RectF(tab.right - 6f * u, tab.top + 0.6f * u, tab.right - 2f * u, tab.bottom - 0.6f * u), thin)
            // Bandes adhésives du bas.
            for (i in 0..1) {
                val cx = battery.left + (22f + i * 24f) * u
                canvas.drawRect(RectF(cx - 3f * u, battery.bottom - 0.8f * u, cx + 3f * u, battery.bottom + 3f * u), thin)
            }
        }

        // ——— Plan avant : cartes, puces, pistes ———

        private fun board(canvas: Canvas) {
            canvas.drawRoundRect(board, 3f * u, 3f * u, line)
            canvas.drawRoundRect(bottom, 3f * u, 3f * u, line)

            // Appareils photo et flash.
            for ((center, radius) in listOf(camera1 to 8f, camera2 to 6.5f)) {
                canvas.drawCircle(center.x, center.y, radius * u, line)
                canvas.drawCircle(center.x, center.y, radius * 0.72f * u, thin)
                canvas.drawCircle(center.x, center.y, radius * 0.38f * u, thin)
                fill.alpha = 60
                canvas.drawCircle(center.x, center.y, radius * 0.38f * u, fill)
            }
            canvas.drawCircle(camera2.x, camera2.y + 10f * u, 1.6f * u, thin)

            // Blindage autour du processeur et de la mémoire.
            val shield = Paint(thin).apply { pathEffect = DashPathEffect(floatArrayOf(1.2f * u, 0.8f * u), 0f) }
            canvas.drawRoundRect(RectF(soc.left - 3f * u, soc.top - 3f * u, soc.right + 3f * u, ram.bottom + 2.5f * u), 2f * u, 2f * u, shield)

            chip(canvas, soc)
            val die = RectF(soc).apply { inset(5f * u, 5f * u) }
            fill.alpha = 45
            canvas.drawRect(die, fill)
            canvas.drawRect(die, thin)
            pins(canvas, soc, 8)
            chip(canvas, ram)
            chip(canvas, modem)
            pins(canvas, modem, 5)
            chip(canvas, pmic)
            connector(canvas, connector)
            connector(canvas, bottomConnector)

            // Carte du bas : port USB-C, vibreur, haut-parleur, micro.
            canvas.drawRoundRect(usb, 2.7f * u, 2.7f * u, line)
            canvas.drawRoundRect(RectF(usb).apply { inset(2.5f * u, 1.8f * u) }, 1f * u, 1f * u, thin)
            canvas.drawRoundRect(motor, 2f * u, 2f * u, thin)
            canvas.drawCircle(motor.centerX(), motor.centerY(), 2.6f * u, thin)
            fill.alpha = 200
            var gx = 13f
            while (gx <= 31f) {
                var gy = bottomTop + 4f
                while (gy <= bottomTop + 12f) {
                    canvas.drawCircle(x(gx), x(gy), 0.55f * u, fill)
                    gy += 2.4f
                }
                gx += 2.4f
            }
            canvas.drawCircle(x(66f), x(bottomTop + 8f), 1f * u, thin)

            // Petits composants semés sur la carte mère, hors des grosses puces.
            val keepOut = listOf(
                RectF(soc.left - 4f * u, soc.top - 4f * u, soc.right + 4f * u, ram.bottom + 4f * u),
                RectF(camera1.x - 10f * u, camera1.y - 10f * u, camera1.x + 10f * u, camera2.y + 13f * u),
                RectF(modem).apply { inset(-2f * u, -2f * u) },
                RectF(pmic).apply { inset(-2f * u, -2f * u) },
                RectF(connector).apply { inset(-2f * u, -2f * u) },
            ) + traces.map { bounds(it).apply { inset(-1.2f * u, -1.2f * u) } }
            val random = Random(7)
            fill.alpha = 150
            repeat(160) {
                val w = (if (random.nextBoolean()) 1.6f else 1f) * u
                val hh = w * 0.55f
                val cx = board.left + (3f * u) + random.nextFloat() * (board.width() - 6f * u)
                val cy = board.top + (3f * u) + random.nextFloat() * (board.height() - 6f * u)
                val rect = if (random.nextBoolean()) RectF(cx, cy, cx + w, cy + hh) else RectF(cx, cy, cx + hh, cy + w)
                if (keepOut.none { RectF.intersects(it, rect) }) canvas.drawRect(rect, fill)
            }
            fill.alpha = 255

            // Pistes, avec une pastille à chaque extrémité.
            traces.forEach { points ->
                val path = Path().apply {
                    moveTo(points[0].x, points[0].y)
                    points.drop(1).forEach { lineTo(it.x, it.y) }
                }
                canvas.drawPath(path, thin)
                canvas.drawCircle(points.first().x, points.first().y, 0.55f * u, fill)
                canvas.drawCircle(points.last().x, points.last().y, 0.55f * u, fill)
            }
        }

        // Pas de texte dans le décor : il se mêlerait à celui des widgets.
        private fun chip(canvas: Canvas, rect: RectF) {
            canvas.drawRoundRect(rect, 0.8f * u, 0.8f * u, line)
        }

        /** Des pattes sur les quatre côtés de [rect]. */
        private fun pins(canvas: Canvas, rect: RectF, count: Int) {
            val len = 1.2f * u
            for (i in 0 until count) {
                val f = (i + 1f) / (count + 1f)
                val px = rect.left + rect.width() * f
                val py = rect.top + rect.height() * f
                canvas.drawLine(px, rect.top, px, rect.top - len, thin)
                canvas.drawLine(px, rect.bottom, px, rect.bottom + len, thin)
                canvas.drawLine(rect.left, py, rect.left - len, py, thin)
                canvas.drawLine(rect.right, py, rect.right + len, py, thin)
            }
        }

        private fun connector(canvas: Canvas, rect: RectF) {
            canvas.drawRect(rect, thin)
            var tx = rect.left + 0.8f * u
            while (tx < rect.right - 0.4f * u) {
                canvas.drawLine(tx, rect.top + 0.6f * u, tx, rect.bottom - 0.6f * u, thin)
                tx += 1f * u
            }
        }

        private fun antennas(): Path {
            val inset = margin + 1.4f
            return Path().apply {
                fun segment(a: PointF, b: PointF) {
                    moveTo(a.x, a.y)
                    lineTo(b.x, b.y)
                }
                segment(p(inset, 0.18f * h), p(inset, 0.34f * h))
                segment(p(100f - inset, 0.46f * h), p(100f - inset, 0.66f * h))
                segment(p(35f, inset), p(65f, inset))
                segment(p(16f, h - inset), p(40f, h - inset))
                segment(p(60f, h - inset), p(84f, h - inset))
            }
        }

        // ——— Pistes et parcours des impulsions ———

        /** Une piste passant par [corners], chaque angle coupé à 45°. */
        private fun route(vararg corners: PointF, chamfer: Float = 1.6f): List<PointF> {
            val c = chamfer * u
            val out = mutableListOf(corners.first())
            for (i in 1 until corners.size - 1) {
                val prev = corners[i - 1]
                val at = corners[i]
                val next = corners[i + 1]
                val inLen = hypot(at.x - prev.x, at.y - prev.y)
                val outLen = hypot(next.x - at.x, next.y - at.y)
                val cc = minOf(c, inLen / 2f, outLen / 2f)
                out += PointF(at.x - (at.x - prev.x) / inLen * cc, at.y - (at.y - prev.y) / inLen * cc)
                out += PointF(at.x + (next.x - at.x) / outLen * cc, at.y + (next.y - at.y) / outLen * cc)
            }
            out += corners.last()
            traces += out
            return out
        }

        private fun networkRoutes(): List<Route> {
            val routes = mutableListOf<Route>()
            val edge = margin + 1.4f
            for (i in 0 until 3) {
                // Antenne → modem.
                val y = modem.top / u + 4f + i * 3f
                val antenna = route(p(edge, y), p(modem.left / u, y))
                // Modem → gestion d'énergie → processeur, en L imbriqués.
                val py = pmic.top / u + 2.5f + i * 2.5f
                val toPmic = route(p(modem.right / u, py), p(pmic.left / u, py))
                val sy = soc.top / u + 9f + i * 3f
                val sx = pmic.left / u + 2.5f + i * 2.5f
                val toSoc = route(p(sx, pmic.top / u), p(sx, sy), p(soc.left / u, sy))
                routes += Route(antenna + toPmic + toSoc)
            }
            return routes
        }

        private fun dataRoutes(): List<Route> = (0 until 4).map { i ->
            val y = soc.top / u + 3f + i * 1.8f
            val column = 88f - i * 2f
            val low = bottomTop + 8f + (3 - i) * 1.6f
            Route(
                route(
                    p(soc.right / u, y),
                    p(column, y),
                    p(column, low),
                    p(usb.right / u + 1.5f, low),
                ),
            )
        }

        private fun innerRoutes(): List<Route> {
            val routes = mutableListOf<Route>()
            // Processeur → appareil photo.
            for (i in 0 until 3) {
                val sx = soc.left / u + 4f + i * 2.5f
                // Plus à droite, plus haut : les L s'emboîtent sans se croiser.
                val cy = camera1.y / u - 1f - i * 2f
                routes += Route(route(p(sx, soc.top / u), p(sx, cy), p(camera1.x / u + 8.5f, cy)))
            }
            // Processeur → mémoire.
            for (i in 0 until 5) {
                val sx = soc.left / u + 3f + i * 4f
                route(p(sx, soc.bottom / u + 1.2f), p(sx, ram.top / u))
            }
            return routes
        }

        private fun bounds(points: List<PointF>): RectF {
            val rect = RectF(points[0].x, points[0].y, points[0].x, points[0].y)
            points.forEach { rect.union(it.x, it.y) }
            return rect
        }
    }
}
