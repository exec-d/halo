package dev.levilainpetit.wux.wallpaper

import android.graphics.Canvas
import android.graphics.DashPathEffect
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PointF
import android.graphics.RectF
import android.graphics.Shader
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Énergie atomique : tout le téléphone devient la cuve d'un réacteur à eau
 * pressurisée, vue en coupe.
 *
 * - devant, la cuve : en haut les mécanismes des grappes de commande (leur
 *   indicateur de position, leurs trois bobines) qui traversent le couvercle
 *   bombé, la bride et ses goujons, les parois et leurs soudures, les
 *   tubulures d'entrée (à gauche) et de sortie (à droite), le fond bombé et
 *   les traversées d'instrumentation qui en sortent ;
 * - au milieu, les équipements internes : l'enveloppe du cœur et son ressort
 *   de maintien, les écrans thermiques, les tubes guides, les plaques, les
 *   onze assemblages de combustible (embouts, grilles, crayons) et les
 *   structures du bas.
 *
 * Ce qui vit : les barres de commande sont sorties du cœur d'autant que de
 * batterie, et le cœur brille d'autant plus ; les indicateurs de position
 * allument un segment par dixième. Des fissions s'allument dans les crayons
 * et en allument d'autres à côté. L'eau du circuit primaire entre froide,
 * descend le long de la cuve, remonte à travers le cœur en s'éclairant et
 * ressort chaude. En charge, les bobines des mécanismes s'allument tour à
 * tour et le débit s'accélère.
 *
 * Les cotes sont celles de la maquette : un centième de la largeur, et une
 * hauteur de 216 unités ramenée à celle de l'écran par [y].
 */
class AtomCore(kit: CoreKit) : CoreArt(kit) {

    override val wholePhone get() = true

    private val scale = kit.h / 216f
    private fun x(v: Float) = v * u
    private fun y(v: Float) = v * scale * u
    private fun p(px: Float, py: Float) = PointF(x(px), y(py))
    private fun r(l: Float, t: Float, rt: Float, b: Float) = RectF(x(l), y(t), x(rt), y(b))

    // ——— Les cotes, en unités ———

    private val coreX0 = 18.5f
    private val coreX1 = 81.5f
    private val aw = (coreX1 - coreX0) / 11f
    private fun ax(i: Int) = coreX0 + aw * (i + 0.5f)

    /** Les neuf mécanismes, au-dessus des neuf assemblages du milieu. */
    private val crdm = (1..9).map { ax(it) }
    private val fuelTop = 102.5f
    private val fuelBot = 155.5f
    private val grids = (0 until 8).map { fuelTop + 2f + it * (fuelBot - fuelTop - 4f) / 7f }
    private val thimbles = listOf(27.1f, 38.5f, 50f, 61.5f, 72.9f)
    private val studs = generateSequence(9.2f) { it + 3.4f }.takeWhile { it <= 90.9f }.toList()
    private val holes = generateSequence(20f) { it + 2.6f }.takeWhile { it < 81f }.toList()

    /** Le haut du couvercle au-dessus de [px] (unités). */
    private fun headY(px: Float) = 52f - 14f * sqrt(max(0f, 1f - ((px - 50f) / 40f).pow(2)))

    /** Le fond intérieur de la cuve sous [px] (unités), un peu au-dessus de la paroi. */
    private fun bottomY(px: Float) = 178f + 19.5f * sqrt(max(0f, 1f - ((px - 50f) / 37.5f).pow(2))) - 2.5f

    /** Le circuit primaire : entrée, descente, fond, un assemblage, plénum supérieur, sortie. */
    private val flows: List<List<PointF>> = listOf(1, 3, 5, 7, 9, 2, 8).mapIndexed { k, col ->
        val cx = ax(col) + if (k % 2 == 1) 1.2f else -1.2f
        val pts = mutableListOf(p(4.6f, 69f), p(11f, 69f), p(13.6f, 72f), p(13.6f, 120f), p(13.6f, 170f))
        for (i in 0..12) {
            val fx = 13.6f + (cx - 13.6f) * i / 12f
            pts += p(fx, min(bottomY(fx), 196f))
        }
        pts += listOf(p(cx, 166f), p(cx, 98f), p(cx + (78f - cx) * 0.5f, 86f), p(80f, 72f), p(86f, 69f), p(95.4f, 69f))
        pts
    }
    private val flowRoutes = flows.map { CircuitScene.Route(it) }
    private val thimbleRoutes = thimbles.map { CircuitScene.Route(listOf(p(it, 161.4f), p(it, 206.5f))) }
    private val guideRoutes = crdm.map { CircuitScene.Route(listOf(p(it, 36f), p(it, fuelTop))) }
    private val coreCenter = p(50f, 129f)

    private fun seg(canvas: Canvas, x0: Float, y0: Float, x1: Float, y1: Float, paint: Paint) =
        canvas.drawLine(x(x0), y(y0), x(x1), y(y1), paint)

    private fun round(canvas: Canvas, l: Float, t: Float, rt: Float, b: Float, radius: Float, paint: Paint) =
        canvas.drawRoundRect(r(l, t, rt, b), x(radius), x(radius), paint)

    /** Une demi-ellipse : le haut (couvercle) ou le bas (fond). */
    private fun dome(canvas: Canvas, cx: Float, cy: Float, rx: Float, ry: Float, upper: Boolean, paint: Paint) =
        canvas.drawArc(r(cx - rx, cy - ry, cx + rx, cy + ry), if (upper) 180f else 0f, 180f, false, paint)

    private fun fine(thin: Paint, alpha: Int) = Paint(thin).apply { strokeWidth = 0.5f * density; this.alpha = alpha }

    // ——— Devant : la cuve ———

    override fun drawBoard(canvas: Canvas, line: Paint, thin: Paint, fill: Paint) {
        val faint = fine(thin, 110)
        val soft = fine(thin, 170)
        val heavy = Paint(line).apply { strokeWidth = 1.5f * density }

        // Les mécanismes de commande : capot, indicateur de position, bobines, carter jusqu'au couvercle.
        round(canvas, 20f, 36.2f, 80f, 38f, 0.3f, soft)
        seg(canvas, 22f, 10f, 46.5f, 10f, faint)
        seg(canvas, 53.5f, 10f, 78f, 10f, faint)
        crdm.forEach { cx ->
            round(canvas, cx - 1.5f, 11f, cx + 1.5f, 23f, 1.2f, line)
            for (k in 0 until 10) seg(canvas, cx - 0.9f, 12.6f + k, cx + 0.9f, 12.6f + k, faint)
            for (c in 0 until 3) {
                val cy = 24f + c * 4f
                round(canvas, cx - 2.3f, cy, cx + 2.3f, cy + 3.2f, 0.5f, line)
                var w = 0.7f
                while (w < 3.2f) {
                    seg(canvas, cx - 2f, cy + w, cx + 2f, cy + w, faint)
                    w += 0.6f
                }
            }
            seg(canvas, cx - 1f, 36f, cx - 1f, headY(cx), thin)
            seg(canvas, cx + 1f, 36f, cx + 1f, headY(cx), thin)
            if (abs(cx - 50f) > 3f) seg(canvas, cx, 10f, cx, 11f, faint)
        }

        // Le couvercle bombé, ses anneaux de levage.
        dome(canvas, 50f, 52f, 40f, 14f, true, heavy)
        dome(canvas, 50f, 52f, 37.5f, 11.8f, true, soft)
        for (lx in floatArrayOf(14.5f, 85.5f)) canvas.drawCircle(x(lx), y(44.8f), x(1.1f), soft)

        // La bride, ses goujons et leurs écrous.
        round(canvas, 7f, 52f, 93f, 58.2f, 0.6f, line)
        seg(canvas, 8f, 55.1f, 92f, 55.1f, faint)
        studs.forEach { sx ->
            seg(canvas, sx, 50.4f, sx, 59.6f, faint)
            round(canvas, sx - 0.9f, 50.4f, sx + 0.9f, 51.8f, 0.2f, soft)
        }

        // Les parois, leur revêtement, leurs soudures, le fond bombé.
        for ((outer, inner, clad) in listOf(Triple(10f, 12.5f, 11.6f), Triple(90f, 87.5f, 88.4f))) {
            seg(canvas, outer, 58.2f, outer, 64f, heavy)
            seg(canvas, outer, 74f, outer, 178f, heavy)
            seg(canvas, inner, 58.2f, inner, 64f, thin)
            seg(canvas, inner, 74f, inner, 178f, thin)
            seg(canvas, clad, 74f, clad, 178f, faint)
        }
        dome(canvas, 50f, 178f, 40f, 22f, false, heavy)
        dome(canvas, 50f, 178f, 37.5f, 19.5f, false, thin)
        val weld = Paint(soft).apply { pathEffect = DashPathEffect(floatArrayOf(0.5f * u, 0.5f * u), 0f) }
        for (wy in floatArrayOf(100f, 140f, 178f)) {
            seg(canvas, 10f, wy, 12.5f, wy, weld)
            seg(canvas, 87.5f, wy, 90f, wy, weld)
        }

        // Les tubulures et leurs brides ; la sortie traverse l'enveloppe du cœur.
        for ((a, b, left) in listOf(Triple(4.6f, 12.5f, true), Triple(87.5f, 95.4f, false))) {
            seg(canvas, a, 64f, b, 64f, line)
            seg(canvas, a, 74f, b, 74f, line)
            val fx = if (left) 7f else 93f
            round(canvas, fx - 1f, 62.4f, fx + 1f, 75.6f, 0.3f, thin)
            seg(canvas, if (left) 10f else 90f, 75.5f, if (left) 8f else 92f, 78f, faint)
        }

        // Les traversées d'instrumentation, sous la cuve.
        thimbles.forEach { tx ->
            seg(canvas, tx, 196f, tx, 206.5f, soft)
            round(canvas, tx - 0.9f, 206.5f, tx + 0.9f, 208f, 0.2f, thin)
        }
    }

    // ——— Au milieu : les équipements internes et le cœur ———

    override fun drawStatic(canvas: Canvas, line: Paint, thin: Paint, fill: Paint) {
        val faint = fine(thin, 100)
        val soft = fine(thin, 170)

        // La sortie, jusqu'au plénum supérieur ; l'enveloppe du cœur, sa bride, le ressort de maintien.
        seg(canvas, 83f, 64f, 87.5f, 64f, thin)
        seg(canvas, 83f, 74f, 87.5f, 74f, thin)
        round(canvas, 12.5f, 58.6f, 87.5f, 61f, 0.3f, line)
        seg(canvas, 17f, 61f, 17f, 168f, line)
        seg(canvas, 83f, 61f, 83f, 64f, line)
        seg(canvas, 83f, 74f, 83f, 168f, line)
        seg(canvas, 18f, 61f, 18f, 168f, faint)
        seg(canvas, 82f, 74f, 82f, 168f, faint)
        canvas.drawPath(Path().apply {
            var sx = 13f
            moveTo(x(sx), y(57.6f + 0.35f * sin(sx * 3f)))
            while (sx <= 87f) {
                lineTo(x(sx), y(57.6f + 0.35f * sin(sx * 3f)))
                sx += 0.4f
            }
        }, soft)
        for (sx in floatArrayOf(14.2f, 84.2f)) round(canvas, sx, 100f, sx + 1.6f, 158f, 0.3f, thin)
        // Les traversées, dans le plénum inférieur.
        thimbles.forEach { tx ->
            seg(canvas, tx, 161.4f, tx, 168f, faint)
            seg(canvas, tx, 171.2f, tx, 196f, faint)
        }

        // Les équipements internes supérieurs.
        round(canvas, 18f, 61.5f, 82f, 64.2f, 0.3f, line)
        crdm.forEach { cx ->
            round(canvas, cx - 1.7f, 64.2f, cx + 1.7f, 96f, 0.4f, thin)
            var ry = 67f
            while (ry < 95f) {
                seg(canvas, cx - 1.7f, ry, cx + 1.7f, ry, faint)
                ry += 3.4f
            }
            round(canvas, cx - 2.2f, 88f, cx + 2.2f, 90f, 0.2f, soft)
        }
        for (cx in floatArrayOf(ax(0), ax(10))) {
            seg(canvas, cx - 0.5f, 64.2f, cx - 0.5f, 96f, soft)
            seg(canvas, cx + 0.5f, 64.2f, cx + 0.5f, 96f, soft)
        }
        round(canvas, 18f, 96f, 82f, 99f, 0.3f, line)
        holes.forEach { canvas.drawCircle(x(it + 0.6f), y(97.5f), x(0.45f), faint) }

        // Le cœur : onze assemblages, leurs embouts, leurs crayons, leurs grilles.
        for (i in 0 until 11) {
            val x0 = coreX0 + i * aw + 0.25f
            val x1 = x0 + aw - 0.5f
            round(canvas, x0, 99.6f, x1, fuelTop, 0.3f, thin)
            round(canvas, x0, fuelBot, x1, 158.2f, 0.3f, thin)
            round(canvas, x0, fuelTop, x1, fuelBot, 0.1f, soft)
            for (k in 1 until 6) {
                val rx = x0 + k * (x1 - x0) / 6f
                seg(canvas, rx, fuelTop + 0.4f, rx, fuelBot - 0.4f, faint)
            }
            grids.forEach { gy ->
                seg(canvas, x0, gy, x1, gy, soft)
                seg(canvas, x0, gy + 0.7f, x1, gy + 0.7f, soft)
            }
        }

        // La plaque inférieure du cœur, les équipements internes inférieurs.
        round(canvas, 18f, 158.4f, 82f, 161.4f, 0.3f, line)
        holes.forEach { canvas.drawCircle(x(it + 0.6f), y(159.9f), x(0.45f), faint) }
        var cx = 23f
        while (cx <= 77f) {
            round(canvas, cx - 0.8f, 161.4f, cx + 0.8f, 168f, 0.2f, soft)
            cx += 6f
        }
        round(canvas, 17f, 168f, 83f, 171.2f, 0.4f, line)
        round(canvas, 30f, 173.5f, 70f, 175f, 0.3f, soft)
        var hx = 32f
        while (hx < 69f) {
            canvas.drawCircle(x(hx), y(174.25f), x(0.35f), faint)
            hx += 2.5f
        }
        for (lx in floatArrayOf(26f, 74f)) round(canvas, lx - 1.4f, 171.2f, lx + 1.4f, 178.5f, 0.3f, soft)
    }

    override fun outlines(): List<Path> =
        (0 until 11).map { i ->
            val x0 = coreX0 + i * aw + 0.25f
            Path().apply { addRect(x(x0), y(fuelTop), x(x0 + aw - 0.5f), y(fuelBot), Path.Direction.CW) }
        } + listOf(96f to 99f, 158.4f to 161.4f, 61.5f to 64.2f, 168f to 171.2f).map { (t, b) ->
            Path().apply { addRect(x(18f), y(t), x(82f), y(b), Path.Direction.CW) }
        } + crdm.map { cx -> Path().apply { addRect(x(cx - 1.7f), y(64.2f), x(cx + 1.7f), y(96f), Path.Direction.CW) } }

    override val origin get() = coreCenter

    /** Le réseau : le circuit primaire ; la charge : les traversées (remontées) ; le battement : les tubes guides. */
    override fun routes() = Triple(flowRoutes, thimbleRoutes, guideRoutes)

    // ——— Ce qui vit ———

    private val point = PointF()
    private var lightColor = 0
    private var light: LinearGradient? = null

    private fun spark(canvas: Canvas, ink: Ink, at: PointF, a: Float, radius: Float) {
        if (a <= 0.01f) return
        ink.halo(canvas, at, x(2.1f), (165 * a).toInt())
        ink.fill.color = ink.palette.core
        ink.fill.alpha = (255 * a).coerceIn(0f, 255f).toInt()
        canvas.drawCircle(at.x, at.y, x(radius), ink.fill)
    }

    /** Le bout des barres : sorties d'autant que de batterie. */
    private fun tip(state: FrameState) = fuelTop - 2f + (1f - state.batteryLevel.coerceIn(0f, 1f)) * (fuelBot - fuelTop - 4f)

    override fun drawLive(canvas: Canvas, state: FrameState, ink: Ink) {
        val strength = strength(state)
        val ignition = state.ignition.coerceIn(0f, 1f)
        val s = strength * ignition
        val seconds = state.timeMillis / 1000f
        val palette = ink.palette
        val power = 0.25f + 0.75f * state.batteryLevel.coerceIn(0f, 1f)

        // La lueur du cœur, plus vive quand les barres sont sorties.
        val breathe = 0.85f + 0.15f * sin(seconds * 1.3f) + if (state.charging) 0.1f * sin(seconds * 5f) else 0f
        if (light == null || lightColor != palette.glow) {
            lightColor = palette.glow
            val clear = palette.glow and 0x00FFFFFF
            light = LinearGradient(
                0f, y(fuelTop), 0f, y(fuelBot),
                intArrayOf(clear, clear or (0x33 shl 24), clear or (0x0A shl 24)), floatArrayOf(0f, 0.5f, 1f),
                Shader.TileMode.CLAMP,
            )
        }
        ink.fill.shader = light
        ink.fill.alpha = (255 * (power * breathe).coerceAtMost(1f) * s).toInt()
        canvas.drawRect(x(coreX0), y(fuelTop), x(coreX1), y(fuelBot), ink.fill)
        ink.fill.shader = null
        ink.halo(canvas, coreCenter, x(34f), (30 * power * breathe * s).toInt())

        // Les barres de commande.
        val tip = tip(state)
        crdm.forEach { cx ->
            ink.stroke.color = palette.core
            ink.stroke.alpha = (230 * s).toInt()
            ink.stroke.strokeWidth = 0.9f * density
            seg(canvas, cx, 64.2f, cx, tip, ink.stroke)
            ink.stroke.color = palette.line
            ink.stroke.alpha = (90 * s).toInt()
            ink.stroke.strokeWidth = 0.5f * density
            seg(canvas, cx - 0.8f, 64.2f, cx - 0.8f, tip, ink.stroke)
            seg(canvas, cx + 0.8f, 64.2f, cx + 0.8f, tip, ink.stroke)
            ink.stroke.color = palette.core
            ink.stroke.alpha = (230 * s).toInt()
            ink.stroke.strokeWidth = 0.9f * density
            canvas.drawRoundRect(r(cx - 1.4f, tip - 0.6f, cx + 1.4f, tip + 0.2f), x(0.2f), x(0.2f), ink.stroke)
        }

        // Les fissions : des éclairs dans les crayons, qui en allument d'autres à côté.
        val rate = if (state.charging) 1.6f else 1f
        for (k in 0 until 26) {
            val cycle = 1.7f + (k % 5) * 0.23f
            val clock = seconds * rate / cycle + k * 0.137f
            val phase = clock - floor(clock)
            val seed = k * 7919 + floor(clock).toInt() * 104_729
            val col = (noise(seed) * 11f).toInt().coerceAtMost(10)
            val rod = 1 + (noise(seed + 1) * 5f).toInt().coerceAtMost(4)
            val fx = coreX0 + col * aw + 0.25f + rod * (aw - 0.5f) / 6f
            val fy = fuelTop + 2f + noise(seed + 2) * (fuelBot - fuelTop - 4f)
            // Pas de fission là où les barres sont descendues.
            if (fy < tip && crdm.any { abs(it - fx) < 2.4f }) continue
            point.set(x(fx), y(fy))
            spark(canvas, ink, point, max(0f, 1f - phase * 4f) * power * s, 0.38f)
            val echo = max(0f, 1f - abs(phase - 0.22f) * 6f) * power * 0.8f * s
            if (echo > 0f) for ((side, n) in listOf(-1f to 3, 1f to 4)) {
                point.set(x(fx + side * aw * 0.5f), y(fy + noise(seed + n) * 3f - 1.5f))
                spark(canvas, ink, point, echo, 0.3f)
            }
        }

        // Le circuit primaire : l'eau entre froide, chauffe dans le cœur, ressort chaude.
        val speed = if (state.charging) 0.16f else 0.08f
        flowRoutes.forEachIndexed { i, route ->
            for (j in 0 until 3) {
                val t = (seconds * speed + i * 0.143f + j / 3f) % 1f
                route.at(t, point)
                val ux = point.x / u
                val uy = point.y / (scale * u)
                val hot = when {
                    ux > 83f -> 1f
                    uy < fuelBot && uy > 60f && ux > 18f -> min(1f, (fuelBot - uy) / (fuelBot - fuelTop))
                    else -> 0f
                }
                val a = min(1f, sin(t * PI.toFloat()) * 3f) * (0.35f + 0.65f * hot) * s
                if (hot > 0.3f) {
                    spark(canvas, ink, point, a * 0.9f, 0.3f)
                } else {
                    ink.fill.color = palette.line
                    ink.fill.alpha = (140 * a).toInt()
                    canvas.drawCircle(point.x, point.y, x(0.32f), ink.fill)
                }
            }
        }
    }

    override fun drawLiveBoard(canvas: Canvas, state: FrameState, ink: Ink) {
        val strength = strength(state)
        val ignition = state.ignition.coerceIn(0f, 1f)
        val s = strength * ignition
        val seconds = state.timeMillis / 1000f
        val palette = ink.palette

        // Les indicateurs de position : un segment par dixième de batterie, le dernier respire en charge.
        val lit = ceil(state.batteryLevel.coerceIn(0f, 1f) * 10f).toInt().coerceIn(1, 10)
        val breath = if (state.charging) (sin(seconds * 4.5f) + 1f) / 2f else 1f
        crdm.forEachIndexed { i, cx ->
            for (k in 0 until lit) {
                val a = if (k == lit - 1) 0.35f + 0.65f * breath else 1f
                ink.stroke.color = palette.core
                ink.stroke.alpha = (240 * a * s).toInt()
                ink.stroke.strokeWidth = 0.9f * density
                seg(canvas, cx - 0.9f, 21.6f - k, cx + 0.9f, 21.6f - k, ink.stroke)
            }
            // En charge, les bobines s'allument tour à tour : les barres montent.
            if (state.charging) {
                val c = ((seconds * 3f + i * 0.33f) % 3f).toInt()
                val cy = 24f + c * 4f
                ink.halo(canvas, p(cx, cy + 1.6f), x(4f), (90 * s).toInt())
                ink.stroke.alpha = (240 * s).toInt()
                canvas.drawRoundRect(r(cx - 2.3f, cy, cx + 2.3f, cy + 3.2f), x(0.5f), x(0.5f), ink.stroke)
            }
        }

        // Les traversées d'instrumentation : la mesure descend vers la table.
        thimbles.forEachIndexed { i, tx ->
            val t = (seconds * 0.22f + i * 0.21f) % 1f
            point.set(x(tx), y(161.4f + t * 45f))
            spark(canvas, ink, point, 0.6f * sin(t * PI.toFloat()) * s, 0.3f)
        }
    }
}
