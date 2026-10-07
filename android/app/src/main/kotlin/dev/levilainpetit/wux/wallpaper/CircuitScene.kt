package dev.levilainpetit.wux.wallpaper

import android.graphics.Bitmap
import android.graphics.BlurMaskFilter
import android.graphics.Canvas
import android.graphics.DashPathEffect
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PointF
import android.graphics.RectF
import kotlin.math.hypot
import kotlin.math.sqrt
import kotlin.random.Random

/**
 * L'intérieur du téléphone, dessiné en schéma néon : châssis, bobine de
 * recharge, batterie, carte mère et carte du bas, pistes.
 *
 * Tout ce qui ne bouge pas est rendu une fois, en masques [Bitmap.Config.ALPHA_8]
 * teintés au moment du dessin : la couleur suit le téléphone sans rien
 * redessiner. Trois plans à des profondeurs différentes donnent la parallaxe.
 *
 * La disposition suit un Pixel 7 vu à travers l'écran : barre photo en haut,
 * objectifs arrière à droite, caméra frontale au centre du poinçon, lecteur
 * d'empreinte sous l'écran, boutons à droite et tiroir SIM à gauche.
 *
 * Les coordonnées sont en « unités » : un centième de la largeur de l'écran.
 * Tout est décrit vu de dos, puis retourné comme on le verrait par l'écran.
 *
 * Variantes ([Core]) : à la place de la batterie et de la bobine, le
 * convecteur temporel de Retour vers le futur (avec, sur les cartes, les
 * afficheurs des circuits temporels, des bobines d'alimentation et Mr. Fusion)
 * ou le réacteur arc d'Iron Man.
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
    /** Le convecteur temporel, s'il remplace la batterie. */
    val flux: Flux? = null,
    /** Le réacteur arc, s'il remplace la batterie. */
    val arc: Arc? = null,
    /** Les autres cœurs ([CoreArt]) : ils se dessinent et s'animent eux-mêmes. */
    val art: CoreArt? = null,
) {

    /**
     * Ce qui occupe la place de la batterie : la batterie, le convecteur, le
     * réacteur arc, ou un des cœurs des thèmes ([CoreArt]) : réfrigérateur
     * quantique, cerveau de silicium, cuve de réacteur nucléaire, Pip-Boy,
     * cyber-cerveau.
     */
    enum class Core { BATTERY, FLUX, ARC, QUANTUM, NEURAL, ATOM, VAULT, GHOST }

    /**
     * Ce qui s'anime dans le réacteur : les dix bobines (autant de dixièmes de
     * batterie), leur angle au centre, et le cœur.
     */
    class Arc(
        val coils: List<Path>,
        /** Angle de chaque bobine vu de face (degrés, 0 à droite, sens horaire). */
        val angles: List<Float>,
        /** L'enroulement de chaque bobine : des segments (x0, y0, x1, y1…), éclairés par-dessous. */
        val windings: List<FloatArray>,
        val core: PointF,
        /** Rayon du boîtier, et du cœur. */
        val radius: Float,
        val coreRadius: Float,
        /** Les deux pistes d'énergie, en arcs, qui tournent en sens contraires. */
        val outerTrack: Path,
        val innerTrack: Path,
        val unit: Float,
    )

    /**
     * Ce qui s'anime dans le convecteur : les lampes de chaque bras, du bout
     * vers le cœur, le cœur, et la jauge (le niveau de batterie).
     */
    class Flux(
        val arms: List<List<PointF>>,
        val core: PointF,
        val coreRadius: Float,
        val gauge: RectF,
        /** Les trois afficheurs des circuits temporels : destination, présent, dernier départ. */
        val clocks: List<RectF>,
        /** Mr. Fusion, sur la carte du bas : il s'éclaire pendant la charge. */
        val fusion: PointF,
        val fusionRadius: Float,
        val unit: Float,
    )

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
        const val GLASS = 3
        const val GLOW_SCALE = 2

        /** Rayons (unités) des bobines d'alimentation et de Mr. Fusion. */
        const val TOROID = 2.6f
        const val FUSION = 4.2f

        /** Les bobines du réacteur arc, et la demi-largeur de chacune (degrés). */
        const val COILS = 10
        const val COIL_HALF = 13f

        fun build(width: Int, height: Int, density: Float, core: Core = Core.BATTERY): CircuitScene =
            Builder(width, height, density, core).build()
    }

    private class Builder(val width: Int, val height: Int, val density: Float, val core: Core) {
        val flux = core == Core.FLUX
        val arc = core == Core.ARC
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

        fun r(l: Float, t: Float, rt: Float, b: Float) = RectF(l * u, t * u, rt * u, b * u)
        fun p(px: Float, py: Float) = PointF(px * u, py * u)

        // Plans, en unités.
        val margin = 3f
        val board = r(7f, 0.045f * h, 93f, 0.36f * h)
        val battery = r(10f, 0.40f * h, 78f, 0.80f * h)
        val bottom = r(7f, 0.84f * h, 93f, 0.955f * h)

        // Composants de la carte mère, relatifs à son bord haut.
        val top = board.top / u
        // Objectifs principal et ultra grand-angle côte à côte, dans leur
        // pastille, puis le flash (vus de dos : à gauche).
        val camera1 = p(18f, top + 13f)
        val camera2 = p(34f, top + 13f)
        val camera1Radius = 7.5f
        val camera2Radius = 5.5f
        val pill = r(9f, top + 4.5f, 41.5f, top + 21.5f)
        val flash = p(43.5f, top + 17.5f)
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

        // Convecteur : les afficheurs des circuits temporels, entre les
        // appareils photo et le modem ; trois bobines d'alimentation au-dessus
        // de l'arrivée des câbles ; Mr. Fusion sur la carte du bas, à la place
        // du haut-parleur.
        val clocks = listOf(25f, 32f, 39f).map { y -> r(12f, top + y, 32f, top + y + 5.5f) }
        val toroids = listOf(150f, 300f, 450f).map { x -> PointF(q(x, 0f).x, board.bottom - 4.5f * u) }
        val fusion = p(22f, bottomTop + 8f)

        // Réacteur arc : centré sur la batterie.
        val reactorCenter = PointF(battery.centerX(), battery.centerY())
        val reactorRadius = minOf(battery.width(), battery.height()) / 2f - 3f * u

        // Les autres cœurs, en coordonnées de face.
        val art = CoreArt.of(
            core,
            CoreKit(u, density, RectF(width - battery.right, battery.top, width - battery.left, battery.bottom), board.bottom, h),
        )

        /** Dessine [block] en coordonnées de face dans un plan dessiné vu de dos. */
        private fun front(canvas: Canvas, block: (Canvas) -> Unit) {
            canvas.save()
            canvas.scale(-1f, 1f, width / 2f, 0f)
            block(canvas)
            canvas.restore()
        }

        /** Un cœur qui redessine tout le téléphone. */
        private val whole = art?.takeIf { it.wholePhone }

        fun build(): CircuitScene {
            val network = networkRoutes()
            val data = dataRoutes()
            val inner = innerRoutes()
            val layers = listOf(
                layer(depth = 1f, alpha = 90) {
                    if (whole != null) {
                        frame(it)
                        front(it) { c -> whole.drawChassis(c, line, thin, fill) }
                    } else {
                        chassis(it)
                    }
                },
                layer(depth = 0.55f, alpha = 150) {
                    when (core) {
                        Core.BATTERY -> battery(it)
                        Core.FLUX -> capacitor(it)
                        Core.ARC -> reactor(it)
                        // Le plan est dessiné vu de dos : on revient à la face.
                        else -> art?.let { a -> front(it) { c -> a.drawStatic(c, line, thin, fill) } }
                    }
                },
                layer(depth = 0.2f, alpha = 230) {
                    if (whole != null) front(it) { c -> whole.drawBoard(c, line, thin, fill) } else board(it)
                },
                layer(depth = 0f, alpha = 170) { glass(it) },
            )
            val cell = RectF(battery).apply { inset(2.2f * u, 2.2f * u) }
            val mirror = Matrix().apply { setScale(-1f, 1f, width / 2f, 0f) }
            fun Route.mirrored() = Route(points.map { PointF(width - it.x, it.y) })
            return CircuitScene(
                width = width,
                height = height,
                layers = layers,
                batteryCell = RectF(width - cell.right, cell.top, width - cell.left, cell.bottom),
                origin = whole?.origin ?: PointF(width - soc.centerX(), soc.centerY()),
                parts = parts().onEach { it.path.transform(mirror) },
                antennas = antennas().apply { transform(mirror) },
                // Les trajets d'un cœur sont déjà de face.
                networkRoutes = whole?.routes()?.first ?: network.map { it.mirrored() },
                dataRoutes = whole?.routes()?.second ?: data.map { it.mirrored() },
                innerRoutes = whole?.routes()?.third ?: inner.map { it.mirrored() },
                density = density,
                flux = if (flux) {
                    fun PointF.m() = PointF(width - x, y)
                    val g = gauge()
                    fun RectF.m() = RectF(width - right, top, width - left, bottom)
                    Flux(
                        arms = arms().map { arm -> arm.lamps.map { it.m() } },
                        core = center().m(),
                        coreRadius = qs(46f),
                        gauge = g.m(),
                        clocks = clocks.map { it.m() },
                        fusion = fusion.m(),
                        fusionRadius = FUSION * u,
                        unit = u,
                    )
                } else {
                    null
                },
                arc = if (arc) {
                    val center = PointF(width - reactorCenter.x, reactorCenter.y)
                    fun track(radius: Float, count: Int, sweep: Float) = Path().apply {
                        val box = RectF(center.x - radius, center.y - radius, center.x + radius, center.y + radius)
                        for (k in 0 until count) addArc(box, k * 360f / count, sweep)
                    }
                    Arc(
                        coils = coils().map { Path().apply { addPoly(it) }.apply { transform(mirror) } },
                        // Vu de face, le sens des angles s'inverse.
                        angles = (0 until COILS).map { 180f - coilAngle(it) },
                        windings = (0 until COILS).map { i ->
                            windings(i).flatMap { (a, b) -> listOf(width - a.x, a.y, width - b.x, b.y) }.toFloatArray()
                        },
                        core = center,
                        radius = reactorRadius,
                        coreRadius = reactorRadius * 0.3f,
                        outerTrack = track(reactorRadius * 0.885f, 6, 34f),
                        innerTrack = track(reactorRadius * 0.47f, 8, 22f),
                        unit = u,
                    )
                } else {
                    null
                },
                art = art,
            )
        }

        private fun parts(): List<Part> {
            val shapes = mutableListOf<Pair<Path, Int>>()
            if (whole != null) {
                // Ses contours, de face, retournés comme le reste (qui l'est à la fin).
                val mirror = Matrix().apply { setScale(-1f, 1f, width / 2f, 0f) }
                val origin = whole.origin?.let { PointF(width - it.x, it.y) } ?: PointF(width / 2f, height / 2f)
                val bounds = RectF()
                val sorted = whole.outlines().map { Path(it).apply { transform(mirror) } }.sortedBy {
                    it.computeBounds(bounds, true)
                    hypot(bounds.centerX() - origin.x, bounds.centerY() - origin.y)
                }
                val last = (sorted.size - 1).coerceAtLeast(1).toFloat()
                return sorted.mapIndexed { i, path -> Part(path, BATTERY, i / last) }
            }
            fun rect(rect: RectF, radius: Float, layer: Int = BOARD) {
                shapes += Path().apply { addRoundRect(rect, radius * u, radius * u, Path.Direction.CW) } to layer
            }
            listOf(soc, ram, modem, pmic).forEach { rect(it, 0.8f) }
            listOf(connector, bottomConnector).forEach { rect(it, 0f) }
            rect(usb, 2.7f)
            rect(motor, 2f)
            rect(battery, if (core == Core.BATTERY) 4f else 2f, BATTERY)
            if (flux) {
                arms().forEach { shapes += Path().apply { addPoly(it.body) } to BATTERY }
                val core = center()
                shapes += Path().apply { addCircle(core.x, core.y, qs(46f), Path.Direction.CW) } to BATTERY
                clocks.forEach { rect(it, 0.6f) }
                toroids.forEach { shapes += Path().apply { addCircle(it.x, it.y, TOROID * u, Path.Direction.CW) } to BOARD }
                shapes += Path().apply { addCircle(fusion.x, fusion.y, FUSION * u, Path.Direction.CW) } to BOARD
            }
            art?.outlines()?.forEach {
                // Vus de face : retournés comme le reste, qui l'est à la fin.
                shapes += Path(it).apply { transform(Matrix().apply { setScale(-1f, 1f, width / 2f, 0f) }) } to BATTERY
            }
            if (arc) {
                coils().forEach { shapes += Path().apply { addPoly(it) } to BATTERY }
                shapes += Path().apply { addCircle(reactorCenter.x, reactorCenter.y, reactorRadius, Path.Direction.CW) } to BATTERY
            }
            rect(pill, 8.5f)
            for ((center, radius) in listOf(camera1 to camera1Radius, camera2 to camera2Radius)) {
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
            // Dessiné vu de dos, retourné pour être vu par l'écran.
            draw(Canvas(core).apply { scale(-1f, 1f, width / 2f, 0f) })
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

            // La barre photo, qui traverse le dos.
            canvas.drawRoundRect(r(margin + 0.8f, top + 2f, 100f - margin - 0.8f, top + 24f), 11f * u, 11f * u, thin)
            // Vus de dos : boutons marche et volume à gauche, tiroir SIM à droite.
            canvas.drawRoundRect(r(margin - 0.9f, 0.2f * h, margin + 0.5f, 0.245f * h), 0.6f * u, 0.6f * u, line)
            canvas.drawRoundRect(r(margin - 0.9f, 0.285f * h, margin + 0.5f, 0.38f * h), 0.6f * u, 0.6f * u, line)
            val sim = r(100f - margin - 0.5f, 0.13f * h, 100f - margin + 0.9f, 0.19f * h)
            canvas.drawRoundRect(sim, 0.6f * u, 0.6f * u, thin)
            canvas.drawCircle(sim.centerX(), sim.bottom - 1.2f * u, 0.35f * u, thin)

            val coil = PointF(battery.centerX(), battery.centerY())
            if (core == Core.BATTERY) for (i in 0 until 8) canvas.drawCircle(coil.x, coil.y, (22f - i * 1.6f) * u, thin)
            if (core == Core.BATTERY) {
                canvas.drawLine(coil.x - 1.5f * u, coil.y + 22f * u, coil.x - 1.5f * u, coil.y + 27f * u, thin)
                canvas.drawLine(coil.x + 1.5f * u, coil.y + 22f * u, coil.x + 1.5f * u, coil.y + 27f * u, thin)
            }

            listOf(
                p(11f, top + 3f), p(89f, top + 3f), p(11f, board.bottom / u - 3f),
                p(11f, bottomTop + 13f), p(89f, bottomTop + 13f),
            ).forEach { screw(canvas, it) }
        }

        /** Le cadre seul, et les boutons : ce qui reste du téléphone quand un cœur redessine tout. */
        private fun frame(canvas: Canvas) {
            val frame = r(margin, margin, 100f - margin, h - margin)
            canvas.drawRoundRect(frame, 11f * u, 11f * u, line)
            canvas.drawRoundRect(RectF(frame).apply { inset(1.4f * u, 1.4f * u) }, 9.6f * u, 9.6f * u, thin)
            canvas.drawRoundRect(r(margin - 0.9f, 0.2f * h, margin + 0.5f, 0.245f * h), 0.6f * u, 0.6f * u, line)
            canvas.drawRoundRect(r(margin - 0.9f, 0.285f * h, margin + 0.5f, 0.38f * h), 0.6f * u, 0.6f * u, line)
        }

        private fun screw(canvas: Canvas, at: PointF) {
            canvas.drawCircle(at.x, at.y, 1.3f * u, thin)
            canvas.drawLine(at.x - 0.7f * u, at.y, at.x + 0.7f * u, at.y, thin)
            canvas.drawLine(at.x, at.y - 0.7f * u, at.x, at.y + 0.7f * u, thin)
        }

        // ——— Sous le verre : caméra frontale, écouteur, lecteur d'empreinte ———

        private fun glass(canvas: Canvas) {
            val punch = p(50f, 0.034f * h)
            canvas.drawCircle(punch.x, punch.y, 2.2f * u, line)
            fill.alpha = 70
            canvas.drawCircle(punch.x, punch.y, 1.1f * u, fill)
            canvas.drawRoundRect(r(41f, margin + 0.9f, 59f, margin + 1.6f), 0.4f * u, 0.4f * u, thin)
            val sensor = p(44.5f, 0.034f * h)
            fill.alpha = 120
            canvas.drawCircle(sensor.x, sensor.y, 0.5f * u, fill)
            fill.alpha = 255

            val print = p(50f, 0.775f * h)
            canvas.drawCircle(print.x, print.y, 5.5f * u, thin)
            for (k in 1..3) {
                val radius = k * 1.3f * u
                canvas.drawArc(
                    RectF(print.x - radius, print.y - radius, print.x + radius, print.y + radius),
                    200f + k * 8f, 150f - k * 16f, false, thin,
                )
                canvas.drawArc(
                    RectF(print.x - radius, print.y - radius, print.x + radius, print.y + radius),
                    20f + k * 8f, 150f - k * 16f, false, thin,
                )
            }
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

        // ——— Plan du milieu, variante : le convecteur temporel ———
        //
        // Le boîtier prend la place de la batterie. Ses cotes sont celles du
        // décor du film (600 × 760 mm), ramenées au rectangle de la batterie
        // par [q] : origine en haut à gauche du boîtier, vu de dos.

        fun q(x: Float, y: Float) = PointF(battery.left + x * battery.width() / 600f, battery.top + y * battery.height() / 760f)
        fun qs(v: Float) = v * battery.width() / 600f
        fun qr(l: Float, t: Float, r: Float, b: Float) = q(l, t).let { a -> q(r, b).let { z -> RectF(a.x, a.y, z.x, z.y) } }

        fun center() = q(300f, 400f)

        /** Un bras du Y : le corps de l'électrode (4 coins), ses bagues, le tube, les lampes. */
        class Arm(val body: List<PointF>, val rings: List<Pair<PointF, PointF>>, val tube: List<Pair<PointF, PointF>>, val lamps: List<PointF>, val gaps: List<Pair<PointF, PointF>>)

        fun arms(): List<Arm> {
            val c = center()
            return listOf(q(140f, 185f), q(460f, 185f), q(300f, 650f)).map { e ->
                val len = hypot(c.x - e.x, c.y - e.y)
                val ux = (c.x - e.x) / len
                val uy = (c.y - e.y) / len
                val nx = -uy
                val ny = ux
                fun at(t: Float, side: Float) = PointF(e.x + ux * t + nx * side, e.y + uy * t + ny * side)
                val h = qs(34f)
                val body = qs(90f)
                val start = body
                val end = len - qs(44f)
                val lamps = (0 until 5).map { i -> start + qs(20f) + (end - start - qs(40f)) * i / 4f }
                Arm(
                    body = listOf(at(0f, h), at(body, h), at(body, -h), at(0f, -h)),
                    rings = listOf(22f, 45f, 68f).map { at(qs(it), h) to at(qs(it), -h) },
                    tube = listOf(at(start, qs(14f)) to at(end, qs(14f)), at(start, -qs(14f)) to at(end, -qs(14f))),
                    lamps = lamps.map { at(it, 0f) },
                    gaps = lamps.map { at(it, qs(22f)) to at(it, -qs(22f)) },
                )
            }
        }

        /** La jauge, au-dessus du hublot (le bas du boîtier passe sous le lecteur d'empreinte). */
        fun gauge() = qr(176f, 42f, 424f, 70f)

        private fun Path.addPoly(points: List<PointF>) {
            moveTo(points[0].x, points[0].y)
            points.drop(1).forEach { lineTo(it.x, it.y) }
            close()
        }

        private fun chamfer(l: Float, t: Float, r: Float, b: Float, c: Float) = listOf(
            q(l + c, t), q(r - c, t), q(r, t + c), q(r, b - c), q(r - c, b), q(l + c, b), q(l, b - c), q(l, t + c),
        )

        private fun capacitor(canvas: Canvas) {
            // Le boîtier, la porte, le hublot à pans coupés.
            canvas.drawRoundRect(battery, 2f * u, 2f * u, line)
            canvas.drawRoundRect(qr(22f, 22f, 578f, 738f), 1.2f * u, 1.2f * u, thin)
            canvas.drawPath(Path().apply { addPoly(chamfer(72f, 92f, 528f, 690f, 34f)) }, line)
            canvas.drawPath(Path().apply { addPoly(chamfer(86f, 106f, 514f, 676f, 28f)) }, thin)
            // Charnières d'un côté, loquet de l'autre.
            canvas.drawRect(qr(-14f, 120f, 0f, 200f), thin)
            canvas.drawRect(qr(-14f, 560f, 0f, 640f), thin)
            canvas.drawRect(qr(578f, 330f, 606f, 470f), thin)
            canvas.drawRect(qr(592f, 360f, 620f, 440f), thin)
            // Quatre vis aux coins de la porte.
            listOf(q(48f, 48f), q(552f, 48f), q(48f, 712f), q(552f, 712f)).forEach { screw(canvas, it) }
            // La jauge, en dix cases.
            val g = gauge()
            canvas.drawRect(g, thin)
            for (i in 1 until 10) {
                val x = g.left + g.width() * i / 10f
                canvas.drawLine(x, g.top + 0.5f * u, x, g.bottom - 0.5f * u, thin)
            }
            // Les trois câbles d'alimentation, qui montent jusqu'à la carte mère.
            for (x in floatArrayOf(150f, 300f, 450f)) {
                val gland = qr(x - 30f, -22f, x + 30f, 0f)
                canvas.drawRect(gland, thin)
                val l = q(x - 16f, 0f).x
                val r = q(x + 16f, 0f).x
                canvas.drawLine(l, gland.top, l, board.bottom, thin)
                canvas.drawLine(r, gland.top, r, board.bottom, thin)
                var y = gland.top - 1.4f * u
                while (y > board.bottom + 0.6f * u) {
                    canvas.drawLine(l, y, r, y, thin)
                    y -= 1.4f * u
                }
            }
            // Le Y : électrodes baguées, tubes, éclateurs, et le cœur.
            for (arm in arms()) {
                canvas.drawPath(Path().apply { addPoly(arm.body) }, line)
                for ((a, b) in arm.rings + arm.tube + arm.gaps) canvas.drawLine(a.x, a.y, b.x, b.y, thin)
            }
            val c = center()
            canvas.drawCircle(c.x, c.y, qs(46f), line)
            canvas.drawCircle(c.x, c.y, qs(26f), thin)
            // Les fils qui relient chaque électrode à son câble.
            val (left, right, low) = arms().map { it.body[0] }
            val feed = Path().apply {
                moveTo(left.x, left.y)
                q(150f, 110f).let { lineTo(it.x, it.y) }
                q(150f, 22f).let { lineTo(it.x, it.y) }
                moveTo(right.x, right.y)
                q(450f, 110f).let { lineTo(it.x, it.y) }
                q(450f, 22f).let { lineTo(it.x, it.y) }
                moveTo(low.x, low.y)
                q(548f, 650f).let { lineTo(it.x, it.y) }
                q(548f, 22f).let { lineTo(it.x, it.y) }
                q(300f, 22f).let { lineTo(it.x, it.y) }
            }
            canvas.drawPath(feed, thin)
        }

        /** Une bobine d'alimentation torique, vue de dessus, avec son enroulement. */
        private fun toroid(canvas: Canvas, at: PointF) {
            val r = TOROID * u
            canvas.drawCircle(at.x, at.y, r, line)
            canvas.drawCircle(at.x, at.y, r * 0.45f, thin)
            for (i in 0 until 14) {
                val a = Math.toRadians(i * 360.0 / 14)
                val c = kotlin.math.cos(a).toFloat()
                val sn = kotlin.math.sin(a).toFloat()
                canvas.drawLine(at.x + c * r * 0.45f, at.y + sn * r * 0.45f, at.x + c * r, at.y + sn * r, thin)
            }
        }

        /** Mr. Fusion : le couvercle rond et ses nervures. */
        private fun fusion(canvas: Canvas) {
            val r = FUSION * u
            canvas.drawCircle(fusion.x, fusion.y, r, line)
            canvas.drawCircle(fusion.x, fusion.y, r * 0.62f, thin)
            canvas.drawCircle(fusion.x, fusion.y, r * 0.22f, thin)
            for (i in 0 until 8) {
                val a = Math.toRadians(i * 45.0 + 22.5)
                val c = kotlin.math.cos(a).toFloat()
                val sn = kotlin.math.sin(a).toFloat()
                canvas.drawLine(fusion.x + c * r * 0.62f, fusion.y + sn * r * 0.62f, fusion.x + c * r * 0.95f, fusion.y + sn * r * 0.95f, thin)
            }
        }

        // ——— Plan du milieu, variante : le réacteur arc ———

        /** L'angle (degrés, vu de dos) de la bobine [i], la première en haut. */
        fun coilAngle(i: Int) = -90f + i * 360f / COILS

        fun around(radius: Float, degrees: Float): PointF {
            val a = Math.toRadians(degrees.toDouble())
            return PointF(reactorCenter.x + kotlin.math.cos(a).toFloat() * radius, reactorCenter.y + kotlin.math.sin(a).toFloat() * radius)
        }

        /** Chaque bobine : un secteur d'anneau entre 0,58 et 0,82 du rayon. */
        fun coils(): List<List<PointF>> = (0 until COILS).map { i ->
            val a = coilAngle(i)
            val half = COIL_HALF
            val outer = (0..8).map { k -> around(reactorRadius * 0.82f, a - half + 2 * half * k / 8f) }
            val inner = (0..8).map { k -> around(reactorRadius * 0.58f, a + half - 2 * half * k / 8f) }
            outer + inner
        }

        /** Les spires d'une bobine : des traits en travers de l'anneau. */
        fun windings(i: Int): List<Pair<PointF, PointF>> {
            val a = coilAngle(i)
            return (0..10).map { k ->
                val o = a - COIL_HALF + 1.5f + (2 * COIL_HALF - 3f) * k / 10f
                around(reactorRadius * 0.605f, o) to around(reactorRadius * 0.795f, o)
            }
        }

        /** Un boulon à tête hexagonale. */
        private fun bolt(canvas: Canvas, at: PointF, radius: Float) {
            canvas.drawCircle(at.x, at.y, radius, thin)
            val hex = (0 until 6).map { k ->
                val t = Math.toRadians(k * 60.0 + 30)
                PointF(at.x + kotlin.math.cos(t).toFloat() * radius * 0.62f, at.y + kotlin.math.sin(t).toFloat() * radius * 0.62f)
            }
            canvas.drawPath(Path().apply { addPoly(hex) }, thin)
        }

        private fun reactor(canvas: Canvas) {
            val rr = reactorRadius
            val c = reactorCenter
            val step = 360f / COILS
            // La platine, ses vis, et les deux câbles vers la carte mère.
            canvas.drawRoundRect(battery, 2f * u, 2f * u, line)
            canvas.drawRoundRect(RectF(battery).apply { inset(1.4f * u, 1.4f * u) }, 1.4f * u, 1.4f * u, thin)
            listOf(
                PointF(battery.left + 3.5f * u, battery.top + 3.5f * u), PointF(battery.right - 3.5f * u, battery.top + 3.5f * u),
                PointF(battery.left + 3.5f * u, battery.bottom - 3.5f * u), PointF(battery.right - 3.5f * u, battery.bottom - 3.5f * u),
            ).forEach { screw(canvas, it) }
            for (dx in floatArrayOf(-14f, 14f)) {
                val x = c.x + dx * u
                canvas.drawRect(RectF(x - 3f * u, battery.top - 2f * u, x + 3f * u, battery.top), thin)
                canvas.drawLine(x - 1.6f * u, battery.top - 2f * u, x - 1.6f * u, board.bottom, thin)
                canvas.drawLine(x + 1.6f * u, battery.top - 2f * u, x + 1.6f * u, board.bottom, thin)
                var y = battery.top - 3.4f * u
                while (y > board.bottom + 0.6f * u) {
                    canvas.drawLine(x - 1.6f * u, y, x + 1.6f * u, y, thin)
                    y -= 1.4f * u
                }
                // Le câble rejoint le boîtier par un connecteur.
                val angle = if (dx < 0) -118f else -62f
                val entry = around(rr, angle)
                canvas.drawLine(x, battery.top, entry.x, entry.y, thin)
                val plug = around(rr * 1.03f, angle)
                canvas.drawCircle(plug.x, plug.y, 1.3f * u, line)
                canvas.drawCircle(plug.x, plug.y, 0.6f * u, thin)
            }
            // Des fils plus fins, des coins de la platine vers le boîtier.
            for (angle in floatArrayOf(40f, 140f)) {
                val to = around(rr, angle)
                val from = PointF(if (angle < 90f) battery.right - 5f * u else battery.left + 5f * u, battery.bottom - 6f * u)
                canvas.drawLine(from.x, from.y, from.x, to.y + (from.y - to.y) * 0.4f, thin)
                canvas.drawLine(from.x, to.y + (from.y - to.y) * 0.4f, to.x, to.y, thin)
            }

            // Le boîtier : deux anneaux, un moletage, dix boulons entre les bobines.
            canvas.drawCircle(c.x, c.y, rr, line)
            canvas.drawCircle(c.x, c.y, rr * 0.955f, thin)
            for (k in 0 until 90) {
                val angle = k * 4f
                // Pas de moletage sous les boulons.
                val offset = ((angle - coilAngle(0) - step / 2) % step + step) % step
                if (offset < 6f || offset > step - 6f) continue
                val p0 = around(rr * 0.91f, angle)
                val p1 = around(rr * 0.945f, angle)
                canvas.drawLine(p0.x, p0.y, p1.x, p1.y, thin)
            }
            for (i in 0 until COILS) bolt(canvas, around(rr * 0.93f, coilAngle(i) + step / 2), 1.1f * u)
            canvas.drawCircle(c.x, c.y, rr * 0.86f, line)

            // Les dix bobines, leurs spires, et les entretoises qui les séparent.
            coils().forEach { canvas.drawPath(Path().apply { addPoly(it) }, line) }
            for (i in 0 until COILS) {
                windings(i).forEach { (a, b) -> canvas.drawLine(a.x, a.y, b.x, b.y, thin) }
                val between = coilAngle(i) + step / 2
                val spacer = listOf(
                    around(rr * 0.57f, between - 2.2f), around(rr * 0.845f, between - 1.6f),
                    around(rr * 0.845f, between + 1.6f), around(rr * 0.57f, between + 2.2f),
                )
                canvas.drawPath(Path().apply { addPoly(spacer) }, thin)
                around(rr * 0.71f, between).let { canvas.drawCircle(it.x, it.y, 0.45f * u, thin) }
            }

            // Le collier intérieur, ses encoches face aux bobines.
            canvas.drawCircle(c.x, c.y, rr * 0.545f, line)
            canvas.drawCircle(c.x, c.y, rr * 0.5f, thin)
            for (i in 0 until COILS) {
                val a = coilAngle(i)
                for (o in floatArrayOf(-4f, 4f)) {
                    val p0 = around(rr * 0.5f, a + o)
                    val p1 = around(rr * 0.545f, a + o)
                    canvas.drawLine(p0.x, p0.y, p1.x, p1.y, thin)
                }
            }

            // Le cœur : son logement perlé, le triangle du nouvel élément, le noyau.
            canvas.drawCircle(c.x, c.y, rr * 0.44f, line)
            for (k in 0 until 24) around(rr * 0.405f, k * 15f).let { canvas.drawCircle(it.x, it.y, 0.3f * u, thin) }
            val tri = listOf(-90f, 30f, 150f).map { around(rr * 0.33f, it) }
            canvas.drawPath(Path().apply { addPoly(tri) }, line)
            tri.forEachIndexed { k, v ->
                val to = around(rr * 0.44f, -90f + k * 120f)
                canvas.drawLine(v.x, v.y, to.x, to.y, thin)
            }
            canvas.drawPath(Path().apply { addPoly(listOf(90f, 210f, 330f).map { around(rr * 0.19f, it) }) }, thin)
            canvas.drawCircle(c.x, c.y, rr * 0.14f, thin)
            canvas.drawCircle(c.x, c.y, rr * 0.07f, thin)
        }

        // ——— Plan avant : cartes, puces, pistes ———

        private fun board(canvas: Canvas) {
            canvas.drawRoundRect(board, 3f * u, 3f * u, line)
            canvas.drawRoundRect(bottom, 3f * u, 3f * u, line)

            // Appareils photo et flash.
            canvas.drawRoundRect(pill, 8.5f * u, 8.5f * u, thin)
            for ((center, radius) in listOf(camera1 to camera1Radius, camera2 to camera2Radius)) {
                canvas.drawCircle(center.x, center.y, radius * u, line)
                canvas.drawCircle(center.x, center.y, radius * 0.72f * u, thin)
                canvas.drawCircle(center.x, center.y, radius * 0.38f * u, thin)
                fill.alpha = 60
                canvas.drawCircle(center.x, center.y, radius * 0.38f * u, fill)
            }
            canvas.drawCircle(flash.x, flash.y, 1.5f * u, thin)
            fill.alpha = 90
            canvas.drawCircle(flash.x, flash.y, 0.8f * u, fill)

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
            // Le convecteur met Mr. Fusion à la place de la grille du haut-parleur.
            var gx = if (flux) 99f else 13f
            while (gx <= 31f) {
                var gy = bottomTop + 4f
                while (gy <= bottomTop + 12f) {
                    val dot = p(gx, gy)
                    canvas.drawCircle(dot.x, dot.y, 0.55f * u, fill)
                    gy += 2.4f
                }
                gx += 2.4f
            }
            if (flux) fusion(canvas)
            val mic = p(66f, bottomTop + 8f)
            canvas.drawCircle(mic.x, mic.y, 1f * u, thin)
            if (flux) {
                clocks.forEach {
                    canvas.drawRoundRect(it, 0.6f * u, 0.6f * u, line)
                    canvas.drawRect(RectF(it).apply { inset(0.8f * u, 0.8f * u) }, thin)
                }
                toroids.forEach { toroid(canvas, it) }
            }

            // Petits composants semés sur la carte mère, hors des grosses puces.
            val keepOut = listOf(
                RectF(soc.left - 4f * u, soc.top - 4f * u, soc.right + 4f * u, ram.bottom + 4f * u),
                RectF(pill).apply { inset(-1.5f * u, -1.5f * u) },
                RectF(flash.x - 2.5f * u, flash.y - 2.5f * u, flash.x + 2.5f * u, flash.y + 2.5f * u),
                RectF(modem).apply { inset(-2f * u, -2f * u) },
                RectF(pmic).apply { inset(-2f * u, -2f * u) },
                RectF(connector).apply { inset(-2f * u, -2f * u) },
            ) + (if (flux) clocks.map { RectF(it).apply { inset(-1.5f * u, -1.5f * u) } } + toroids.map { RectF(it.x - 4f * u, it.y - 4f * u, it.x + 4f * u, it.y + 4f * u) } else emptyList()) +
                traces.map { bounds(it).apply { inset(-1.2f * u, -1.2f * u) } }
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
            // Convecteur : de la première bobine d'alimentation aux afficheurs.
            if (flux) {
                val from = toroids.first()
                val x = from.x / u + 1.2f
                routes += Route(route(p(x, from.y / u - TOROID + 0.3f), p(x, clocks.last().bottom / u)))
            }
            // Processeur → appareil photo.
            for (i in 0 until 3) {
                val sx = soc.left / u + 4f + i * 2.5f
                // Plus à droite, plus haut : les L s'emboîtent sans se croiser.
                val cy = camera1.y / u - 1f - i * 2f
                // Jusqu'au bord de l'ultra grand-angle.
                val dy = cy - camera2.y / u
                val edge = camera2.x / u + sqrt(camera2Radius * camera2Radius - dy * dy) + 0.6f
                routes += Route(route(p(sx, soc.top / u), p(sx, cy), p(edge, cy)))
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
