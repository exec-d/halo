package dev.levilainpetit.wux.wallpaper

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.DashPathEffect
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PointF
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.RadialGradient
import android.graphics.RectF
import android.graphics.Shader
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.cos
import kotlin.math.exp
import kotlin.math.hypot
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin

/**
 * Iron Man : le téléphone porte l'armure, et le réacteur arc bat en son
 * centre.
 *
 * - le cadre devient une ceinture de plaques boulonnées, les boutons des
 *   vérins blindés, la caméra frontale prend un iris, le port USB un
 *   logement renforcé ; des gaines courent le long des flancs ;
 * - les plaques épousent le téléphone : le casque et ses voyants, les
 *   clavicules, les pectoraux autour du logement du réacteur, les lamelles
 *   des côtes, les abdominaux, le sternum en vertèbres et deux vérins ;
 *   entre elles, une maille hexagonale et des conduits d'énergie ;
 * - le réacteur : logement à dix pans, condensateurs, boîtier vissé,
 *   couronne graduée, dix bobines à spires et leurs fils, brides, cœur.
 *
 * Ce qui vit : le réacteur bat (deux coups, puis un temps) ; à chaque
 * battement les arêtes tournées vers lui s'éclairent, une onde parcourt la
 * maille et file dans les conduits. Une bobine par dixième de batterie. En
 * charge, le courant monte du port USB au réacteur, les anneaux tournent et
 * les vérins se détendent. Avec du réseau, les voyants clignotent vite et
 * les impulsions viennent des coins. Un reflet balaie l'armure.
 *
 * Les cotes sont celles de la maquette : un centième de la largeur, et une
 * hauteur de 216 unités ramenée à celle de l'écran par [y].
 */
class ArcCore(kit: CoreKit) : CoreArt(kit) {

    override val wholePhone get() = true

    private val scale = kit.h / 216f
    private fun x(v: Float) = v * u
    private fun y(v: Float) = v * scale * u
    private fun p(px: Float, py: Float) = PointF(x(px), y(py))

    private class Plate(val pts: List<PointF>, val kind: Kind) {
        enum class Kind { PLATE, RIB, SPINE }
        val c = PointF(pts.sumOf { it.x.toDouble() }.toFloat() / pts.size, pts.sumOf { it.y.toDouble() }.toFloat() / pts.size)
        val d = hypot(c.x - CX, c.y - CY)
    }

    private fun pt(px: Float, py: Float) = PointF(px, py)
    private fun mirror(pts: List<PointF>) = pts.map { pt(100f - it.x, it.y) }.reversed()

    /** Les plaques, de l'arrière vers l'avant, en unités. */
    private val plates: List<Plate> = buildList {
        for (k in 0 until 7) {
            val y0 = 40f + k * 13.5f
            val rib = listOf(pt(9.4f, y0), pt(19f, y0 + 3f), pt(19f, y0 + 15f), pt(9.4f, y0 + 12f))
            add(Plate(rib, Plate.Kind.RIB))
            add(Plate(mirror(rib), Plate.Kind.RIB))
        }
        ABS.forEach { (y0, y1, x0) ->
            val c = 2.6f
            val ab = listOf(pt(x0 + c, y0), pt(46.5f, y0), pt(46.5f, y1 - c), pt(46.5f - c, y1), pt(x0, y1), pt(x0, y0 + c))
            add(Plate(ab, Plate.Kind.PLATE))
            add(Plate(mirror(ab), Plate.Kind.PLATE))
        }
        val pec = listOf(
            pt(16f, 40f), pt(30f, 45.5f), pt(46.5f, 47f), pt(44.5f, 64f), pt(31f, 74f), pt(26.5f, 98f),
            pt(31f, 122f), pt(44.5f, 131f), pt(46.5f, 136.5f), pt(30f, 138f), pt(17f, 133f), pt(14.5f, 86f),
        )
        add(Plate(pec, Plate.Kind.PLATE))
        add(Plate(mirror(pec), Plate.Kind.PLATE))
        val clavicle = listOf(pt(9.4f, 24f), pt(28f, 31f), pt(45f, 35f), pt(46.5f, 43.5f), pt(30f, 42.5f), pt(9.4f, 35.5f))
        add(Plate(clavicle, Plate.Kind.PLATE))
        add(Plate(mirror(clavicle), Plate.Kind.PLATE))
        add(Plate(listOf(
            pt(9.4f, 11f), pt(37f, 11f), pt(41f, 15.5f), pt(59f, 15.5f), pt(63f, 11f), pt(90.6f, 11f), pt(90.6f, 21.5f),
            pt(70f, 29f), pt(57f, 31.5f), pt(43f, 31.5f), pt(30f, 29f), pt(9.4f, 21.5f),
        ), Plate.Kind.PLATE))
        add(Plate(listOf(pt(46.5f, 128f), pt(53.5f, 128f), pt(53.5f, 198f), pt(46.5f, 198f)), Plate.Kind.SPINE))
    }

    /** Les conduits d'énergie : du réacteur vers la caméra, les coins, les flancs et le port. */
    private val pipes: List<List<PointF>> = listOf(
        listOf(pt(CX, CY - 28.5f), pt(CX, 33f), pt(CX, 11f)),
        listOf(pt(CX - 20f, CY - 20f), pt(31f, 74f), pt(30f, 45.5f), pt(16f, 40f), pt(9.4f, 24f), pt(7f, 14f)),
        listOf(pt(CX + 20f, CY - 20f), pt(69f, 74f), pt(70f, 45.5f), pt(84f, 40f), pt(90.6f, 24f), pt(93f, 14f)),
        listOf(pt(CX - 28.5f, CY), pt(14.5f, 98f), pt(7f, 98f)),
        listOf(pt(CX + 28.5f, CY), pt(85.5f, 98f), pt(93f, 98f)),
        listOf(pt(CX - 20f, CY + 20f), pt(31f, 122f), pt(30f, 138f), pt(17f, 140f), pt(12f, 198f), pt(12f, 204f)),
        listOf(pt(CX + 20f, CY + 20f), pt(69f, 122f), pt(70f, 138f), pt(83f, 140f), pt(88f, 198f), pt(88f, 204f)),
        listOf(pt(CX, CY + 28.5f), pt(CX, 128f), pt(CX, 203f)),
    )
    private fun px(list: List<PointF>) = list.map { p(it.x, it.y) }
    private val pipeRoutes = pipes.map { CircuitScene.Route(px(it)) }

    private fun path(points: List<PointF>, close: Boolean = true) = Path().apply {
        moveTo(x(points[0].x), y(points[0].y))
        for (i in 1 until points.size) lineTo(x(points[i].x), y(points[i].y))
        if (close) close()
    }

    /** Un polygone réduit vers son centre : le chanfrein intérieur d'une plaque. */
    private fun inset(pts: List<PointF>, k: Float): List<PointF> {
        val cx = pts.sumOf { it.x.toDouble() }.toFloat() / pts.size
        val cy = pts.sumOf { it.y.toDouble() }.toFloat() / pts.size
        return pts.map { pt(cx + (it.x - cx) * k, cy + (it.y - cy) * (1f - (1f - k) * 0.6f)) }
    }

    private fun inside(px: Float, py: Float, pts: List<PointF>): Boolean {
        var hit = false
        var j = pts.size - 1
        for (i in pts.indices) {
            val a = pts[i]
            val b = pts[j]
            if ((a.y > py) != (b.y > py) && px < (b.x - a.x) * (py - a.y) / (b.y - a.y) + a.x) hit = !hit
            j = i
        }
        return hit
    }

    /** Les cellules de la maille qu'on voit entre les plaques : centre et distance au réacteur. */
    private val cells: List<FloatArray> = buildList {
        for (row in 0 until 62) for (col in 0 until 30) {
            val hx = 9.4f + col * 2.9f + (row % 2) * 1.45f
            val hy = 12f + row * 3.1f
            if (hx > 90.6f || hy > 206f) continue
            if (hypot(hx - CX, hy - CY) < 29f) continue
            if (plates.any { inside(hx, hy, it.pts) }) continue
            add(floatArrayOf(hx, hy, hypot(hx - CX, hy - CY)))
        }
    }
    private fun hexagon(cx: Float, cy: Float, r: Float) = Path().apply {
        for (k in 0..6) {
            val a = PI.toFloat() / 6f + k * PI.toFloat() / 3f
            val hx = x(cx + cos(a) * r)
            val hy = y(cy + sin(a) * r)
            if (k == 0) moveTo(hx, hy) else lineTo(hx, hy)
        }
    }
    private val cellPaths = cells.map { hexagon(it[0], it[1], 1.55f) }

    /** Les arêtes tournées vers le réacteur, et combien elles lui font face. */
    private class Edge(val a: PointF, val b: PointF, val face: Float)

    private val facing: List<Edge> = plates.flatMap { pl ->
        pl.pts.indices.mapNotNull { i ->
            val a0 = pl.pts[i]
            val a1 = pl.pts[(i + 1) % pl.pts.size]
            val mx = (a0.x + a1.x) / 2f
            val my = (a0.y + a1.y) / 2f
            var nx = a1.y - a0.y
            var ny = a0.x - a1.x
            val nl = hypot(nx, ny).takeIf { it > 0f } ?: 1f
            nx /= nl
            ny /= nl
            if ((mx - pl.c.x) * nx + (my - pl.c.y) * ny < 0f) {
                nx = -nx
                ny = -ny
            }
            val dl = hypot(CX - mx, CY - my).takeIf { it > 0f } ?: 1f
            val face = max(0f, (nx * (CX - mx) + ny * (CY - my)) / dl) * max(0f, 1f - dl / 90f)
            if (face > 0.08f) Edge(p(a0.x, a0.y), p(a1.x, a1.y), face) else null
        }
    }

    /** Les grilles d'aération des grandes plaques, tournées vers le réacteur. */
    private val grilles: List<Pair<Float, FloatArray>> = plates.filter { it.kind == Plate.Kind.PLATE && it.d in 20f..70f }.flatMap { pl ->
        val dx = (CX - pl.c.x) / pl.d
        val dy = (CY - pl.c.y) / pl.d
        (-2..2).map { k ->
            val bx = pl.c.x + dx * 3f - dy * k * 1.3f
            val by = pl.c.y + dy * 3f + dx * k * 1.3f
            max(0f, 1f - pl.d / 70f) to floatArrayOf(x(bx), y(by), x(bx + dx * 4f), y(by + dy * 4f))
        }
    }

    private val center = p(CX, CY)
    private val port = p(50f, 209.2f)

    private fun ring(r: Float, a: Float) = PointF(center.x + cos(a) * x(r), center.y + sin(a) * x(r))

    private fun coil(k: Int): Path {
        val a0 = -PI.toFloat() / 2f + k * PI.toFloat() / 5f + 0.07f
        val a1 = a0 + PI.toFloat() / 5f - 0.14f
        return Path().apply {
            ring(11f, a0 + 0.03f).let { moveTo(it.x, it.y) }
            ring(18.8f, a0).let { lineTo(it.x, it.y) }
            ring(18.8f, a1).let { lineTo(it.x, it.y) }
            ring(11f, a1 - 0.03f).let { lineTo(it.x, it.y) }
            close()
        }
    }
    private val coils = (0 until 10).map { coil(it) }
    private val box = RectF()

    private fun arc(canvas: Canvas, r: Float, a0: Float, a1: Float, paint: Paint) {
        box.set(center.x - x(r), center.y - x(r), center.x + x(r), center.y + x(r))
        canvas.drawArc(box, Math.toDegrees(a0.toDouble()).toFloat(), Math.toDegrees((a1 - a0).toDouble()).toFloat(), false, paint)
    }

    private fun seg(canvas: Canvas, x0: Float, y0: Float, x1: Float, y1: Float, paint: Paint) =
        canvas.drawLine(x(x0), y(y0), x(x1), y(y1), paint)

    private val clear = Paint(Paint.ANTI_ALIAS_FLAG).apply { xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR) }

    // ——— Devant : l'armure ———

    override fun drawBoard(canvas: Canvas, line: Paint, thin: Paint, fill: Paint) {
        val fine = Paint(thin).apply { strokeWidth = 0.5f * density }
        val faint = Paint(fine).apply { alpha = 90 }
        val soft = Paint(thin).apply { alpha = 170 }
        val metal = Paint(fill).apply {
            shader = RadialGradient(center.x, center.y, x(120f), intArrayOf(Color.argb(40, 255, 255, 255), Color.argb(15, 255, 255, 255), Color.argb(4, 255, 255, 255)), floatArrayOf(0f, 0.35f, 1f), Shader.TileMode.CLAMP)
        }
        val edge = Paint(line).apply { strokeWidth = 1.1f * density }

        // La ceinture du cadre, son bord intérieur, ses coupes et leurs vis.
        val belt = Path().apply {
            fillType = Path.FillType.EVEN_ODD
            addRoundRect(RectF(x(3f), y(3f), x(97f), y(213f)), x(11f), x(11f), Path.Direction.CW)
            addRoundRect(RectF(x(9.4f), y(11f), x(90.6f), y(206f)), x(5f), x(5f), Path.Direction.CW)
        }
        canvas.drawPath(belt, metal)
        canvas.drawRoundRect(RectF(x(4.6f), y(4.6f), x(95.4f), y(211.4f)), x(9.6f), x(9.6f), faint)
        canvas.drawRoundRect(RectF(x(9.4f), y(11f), x(90.6f), y(206f)), x(5f), x(5f), line)
        for (cy in floatArrayOf(40f, 80f, 120f, 160f, 190f)) {
            seg(canvas, 3.2f, cy, 9.4f, cy, soft)
            seg(canvas, 96.8f, cy, 90.6f, cy, soft)
            canvas.drawCircle(x(6.3f), y(cy + 2.4f), x(0.6f), soft)
            canvas.drawCircle(x(93.7f), y(cy + 2.4f), x(0.6f), soft)
        }
        for (cx in floatArrayOf(30f, 70f)) {
            seg(canvas, cx, 3f, cx, 11f, soft)
            seg(canvas, cx, 213f, cx, 206f, soft)
        }
        // Les vérins des boutons : leurs anneaux (le bouton est dessiné avec le cadre).
        for ((y0, y1) in listOf(43.2f to 52.9f, 61.6f to 82.1f)) {
            var ry = y0 + 2f
            while (ry < y1 - 1f) {
                seg(canvas, 95.8f, ry, 97.6f, ry, fine)
                ry += 2f
            }
        }
        // L'iris de la caméra frontale.
        val cam = p(50f, 0.034f * 216f)
        canvas.drawCircle(cam.x, cam.y, x(4.6f), clear)
        canvas.drawCircle(cam.x, cam.y, x(4.6f), line)
        canvas.drawCircle(cam.x, cam.y, x(3.6f), fine)
        for (k in 0 until 6) {
            val a = k * PI.toFloat() / 3f
            canvas.drawLine(cam.x + cos(a) * x(2.3f), cam.y + sin(a) * x(2.3f), cam.x + cos(a + 0.9f) * x(3.5f), cam.y + sin(a + 0.9f) * x(3.5f), fine)
        }
        // Le logement du port USB.
        val usb = RectF(x(37f), y(205f), x(63f), y(213.4f))
        canvas.drawRoundRect(usb, x(2f), x(2f), clear)
        canvas.drawRoundRect(usb, x(2f), x(2f), line)
        canvas.drawRoundRect(RectF(x(41f), y(207.3f), x(59f), y(211.5f)), x(2f), x(2f), thin)
        for (sx in floatArrayOf(38.8f, 61.2f)) canvas.drawCircle(x(sx), y(209.2f), x(0.6f), soft)

        // La maille, entre les plaques ; les gaines des flancs.
        cellPaths.forEach { canvas.drawPath(it, faint) }
        for (gx in floatArrayOf(6.4f, 93.6f)) {
            seg(canvas, gx - 0.9f, 30f, gx - 0.9f, 186f, soft)
            seg(canvas, gx + 0.9f, 30f, gx + 0.9f, 186f, soft)
            var gy = 31f
            while (gy < 186f) {
                seg(canvas, gx - 0.9f, gy, gx + 0.9f, gy, faint)
                gy += 2.4f
            }
        }

        // Les plaques : effacées dessous, éclairées, puis leurs arêtes et leurs détails.
        val dashed = Paint(fine).apply { alpha = 50; pathEffect = DashPathEffect(floatArrayOf(x(1.2f), x(0.8f)), 0f) }
        plates.forEach { pl ->
            val shape = path(pl.pts)
            canvas.drawPath(shape, clear)
            canvas.drawPath(shape, metal)
            val near = max(0f, 1f - pl.d / 70f)
            edge.alpha = (255 * min(1f, 0.6f + 0.3f * near)).toInt()
            canvas.drawPath(shape, edge)
            fine.alpha = (255 * (0.24f + 0.15f * near)).toInt()
            canvas.drawPath(path(inset(pl.pts, if (pl.kind == Plate.Kind.RIB) 0.82f else 0.9f)), fine)
            if (pl.kind == Plate.Kind.PLATE) canvas.drawPath(path(inset(pl.pts, 0.74f)), dashed)
            if (pl.kind != Plate.Kind.RIB) pl.pts.forEachIndexed { i, q ->
                if (i % 2 == 1) return@forEachIndexed
                val bx = pl.c.x + (q.x - pl.c.x) * 0.86f
                val by = pl.c.y + (q.y - pl.c.y) * 0.9f
                val bolt = Path().apply {
                    for (k in 0..6) {
                        val a = k * PI.toFloat() / 3f + 0.3f
                        val hx = x(bx + cos(a) * 0.7f)
                        val hy = y(by + sin(a) * 0.7f)
                        if (k == 0) moveTo(hx, hy) else lineTo(hx, hy)
                    }
                }
                soft.alpha = 150
                canvas.drawPath(bolt, soft)
                seg(canvas, bx - 0.4f, by - 0.2f, bx + 0.4f, by + 0.2f, soft)
            }
            if (pl.kind == Plate.Kind.RIB) {
                val hinge = if (pl.pts[1].x > 50f) pl.pts[0] else pl.pts[1]
                soft.alpha = 150
                canvas.drawCircle(x(hinge.x + if (hinge.x > 50f) 0.6f else -0.6f), y(hinge.y + 1.2f), x(0.55f), soft)
                val m0 = pt((pl.pts[0].x + pl.pts[3].x) / 2f, (pl.pts[0].y + pl.pts[3].y) / 2f)
                val m1 = pt((pl.pts[1].x + pl.pts[2].x) / 2f, (pl.pts[1].y + pl.pts[2].y) / 2f)
                faint.alpha = 76
                seg(canvas, m0.x, m0.y, m1.x, m1.y, faint)
                faint.alpha = 90
            }
        }
        soft.alpha = 170
        grilles.forEach { (near, s) -> canvas.drawLine(s[0], s[1], s[2], s[3], Paint(fine).apply { alpha = (90 + 60 * near).toInt() }) }
        // Les nervures des abdominaux, les fentes du casque.
        fine.alpha = 72
        ABS.forEach { (y0, y1, x0) ->
            for (k in 1 until 4) {
                val ry = y0 + k * (y1 - y0) / 4f
                seg(canvas, x0 + 4f, ry, 43f, ry, fine)
                seg(canvas, 57f, ry, 100f - x0 - 4f, ry, fine)
            }
        }
        for (k in 0 until 6) seg(canvas, 44f + k * 2.4f, 19f, 44f + k * 2.4f, 27f, soft)
        for (s in floatArrayOf(1f, -1f)) for (k in 0 until 4) {
            val sx = 50f + s * (24f + k * 3f)
            seg(canvas, sx, 14f, sx - s * 2f, 22f, Paint(fine).apply { alpha = 100 })
        }
        // Les cylindres des vérins.
        for (vx in floatArrayOf(11.6f, 88.4f)) {
            val cyl = RectF(x(vx - 1.4f), y(142f), x(vx + 1.4f), y(168f))
            canvas.drawRect(cyl, clear)
            canvas.drawRect(cyl, thin)
            var ry = 145f
            while (ry < 166f) {
                seg(canvas, vx - 1.4f, ry, vx + 1.4f, ry, faint)
                ry += 3f
            }
        }
        // Les conduits, dans leurs rainures.
        val groove = Paint(clear).apply { style = Paint.Style.STROKE; strokeWidth = x(0.55f) }
        val pipe = Paint(fine).apply { alpha = 82 }
        pipes.forEach {
            canvas.drawPath(path(it, false), groove)
            canvas.drawPath(path(it, false), pipe)
        }
        // Le sternum en vertèbres.
        var vy = 130f
        while (vy < 197f) {
            val v = RectF(x(46.9f), y(vy), x(53.1f), y(vy + 4.2f))
            canvas.drawRect(v, clear)
            canvas.drawRect(v, soft)
            seg(canvas, 48.2f, vy + 2.1f, 51.8f, vy + 2.1f, faint)
            vy += 5.6f
        }
        // Les voyants du casque.
        for (lx in floatArrayOf(15.5f, 84.5f)) canvas.drawCircle(x(lx), y(16.5f), x(1.2f), soft)

        drawReactor(canvas, line, thin, fine, soft, metal)
    }

    private fun drawReactor(canvas: Canvas, line: Paint, thin: Paint, fine: Paint, soft: Paint, metal: Paint) {
        // Le logement à dix pans, ses vis, ses condensateurs.
        val socket = Path().apply {
            for (k in 0 until 10) {
                val q = ring(28.5f, -PI.toFloat() / 2f + k * PI.toFloat() / 5f + PI.toFloat() / 10f)
                if (k == 0) moveTo(q.x, q.y) else lineTo(q.x, q.y)
            }
            close()
        }
        canvas.drawPath(socket, clear)
        canvas.drawPath(socket, metal)
        canvas.drawPath(socket, Paint(line).apply { strokeWidth = 1.3f * density })
        for (k in 0 until 10) {
            val a = -PI.toFloat() / 2f + k * PI.toFloat() / 5f
            ring(27.2f, a).let { canvas.drawCircle(it.x, it.y, x(0.6f), soft) }
            canvas.save()
            ring(26.4f, a).let { canvas.translate(it.x, it.y) }
            canvas.rotate(Math.toDegrees(a.toDouble()).toFloat())
            canvas.drawRect(-x(0.9f), -x(1.8f), x(0.9f), x(1.8f), soft)
            canvas.drawLine(-x(0.9f), 0f, x(0.9f), 0f, fine)
            canvas.restore()
        }
        // Le boîtier, ses bagues et ses vis.
        canvas.drawCircle(center.x, center.y, x(24.5f), clear)
        canvas.drawCircle(center.x, center.y, x(24.5f), Paint(line).apply { strokeWidth = 1.4f * density })
        canvas.drawCircle(center.x, center.y, x(22.8f), thin)
        canvas.drawCircle(center.x, center.y, x(21f), thin)
        for (k in 0 until 12) ring(23.6f, k / 12f * 2f * PI.toFloat() + 0.26f).let { canvas.drawCircle(it.x, it.y, x(0.55f), soft) }
        // Les bobines éteintes et leurs spires (les allumées vivent).
        val dim = Paint(thin).apply { alpha = 90 }
        val dimFine = Paint(fine).apply { alpha = 46 }
        for (k in 0 until 10) {
            canvas.drawPath(coils[k], dim)
            val a0 = -PI.toFloat() / 2f + k * PI.toFloat() / 5f + 0.07f
            val a1 = a0 + PI.toFloat() / 5f - 0.14f
            for (w in 0 until 8) arc(canvas, 11.9f + w * 0.85f, a0 + 0.05f, a1 - 0.05f, dimFine)
        }
        // L'anneau intérieur, ses rayons, sa graduation fine ; les brides du cœur.
        canvas.drawCircle(center.x, center.y, x(10.2f), line)
        canvas.drawCircle(center.x, center.y, x(8.6f), thin)
        for (k in 0 until 10) {
            val a = -PI.toFloat() / 2f + (k + 0.5f) * PI.toFloat() / 5f
            val q0 = ring(8.6f, a)
            val q1 = ring(10.2f, a)
            canvas.drawLine(q0.x, q0.y, q1.x, q1.y, soft)
        }
        for (k in 0 until 40) {
            val a = k / 40f * 2f * PI.toFloat()
            val q0 = ring(9.1f, a)
            val q1 = ring(9.6f, a)
            canvas.drawLine(q0.x, q0.y, q1.x, q1.y, dimFine)
        }
        for (k in 0 until 3) {
            canvas.save()
            canvas.translate(center.x, center.y)
            canvas.rotate(90f + k * 120f)
            val clamp = RectF(x(6.4f), -x(1f), x(9f), x(1f))
            canvas.drawRect(clamp, clear)
            canvas.drawRect(clamp, thin)
            canvas.restore()
        }
        canvas.drawCircle(center.x, center.y, x(6.2f), clear)
    }

    override fun drawStatic(canvas: Canvas, line: Paint, thin: Paint, fill: Paint) = Unit

    /** Tout vit devant, avec l'armure : rien au milieu. */
    override fun drawLive(canvas: Canvas, state: FrameState, ink: Ink) = Unit

    override fun outlines(): List<Path> =
        listOf(Path().apply { addCircle(center.x, center.y, x(24.5f), Path.Direction.CW) }) + plates.map { path(it.pts) }

    override val origin get() = center

    /** Le réseau : des coins vers le réacteur ; la charge : du port au réacteur ; le battement : du réacteur vers les bords. */
    override fun routes() = Triple(
        listOf(pipeRoutes[1].reversed(), pipeRoutes[2].reversed()),
        listOf(pipeRoutes[7]),
        pipeRoutes,
    )

    // ——— Ce qui vit ———

    private fun period(state: FrameState) = if (state.charging) 0.8f else 1.3f

    /** Le battement : deux coups rapprochés, puis un temps. */
    private fun beat(state: FrameState): Float {
        val period = period(state)
        val t = (state.timeMillis / 1000f % period) / period
        val a = (t - 0.08f) / 0.05f
        val b = (t - 0.26f) / 0.06f
        return exp(-a * a) + 0.6f * exp(-b * b)
    }

    private val point = PointF()
    private var shaderColor = 0
    private var coilShader: RadialGradient? = null
    private var glint: LinearGradient? = null
    private val glintPath = Path().apply { addRoundRect(RectF(x(3f), y(3f), x(97f), y(213f)), x(11f), x(11f), Path.Direction.CW) }

    private fun spark(canvas: Canvas, ink: Ink, at: PointF, a: Float, radius: Float = 0.4f) {
        if (a <= 0.01f) return
        ink.halo(canvas, at, x(2.1f), (165 * a).toInt())
        ink.fill.color = ink.palette.core
        ink.fill.alpha = (255 * min(1f, a)).toInt()
        canvas.drawCircle(at.x, at.y, x(radius), ink.fill)
    }

    override fun drawLiveBoard(canvas: Canvas, state: FrameState, ink: Ink) {
        val s = strength(state) * state.ignition.coerceIn(0f, 1f)
        val seconds = state.timeMillis / 1000f
        val palette = ink.palette
        val stroke = ink.stroke
        val beat = beat(state)
        val phase = (seconds % period(state)) / period(state)
        val lit = ceil(state.batteryLevel.coerceIn(0f, 1f) * 10f).toInt().coerceIn(1, 10)
        val net = state.pulses.isNotEmpty()

        // La lueur du réacteur sur l'armure.
        ink.halo(canvas, center, x(48f), ((36 + 30 * beat) * s).toInt())

        // L'onde dans la maille.
        val wave = phase * 140f
        stroke.strokeWidth = 0.5f * density
        cells.forEachIndexed { i, c ->
            val lift = max(0f, 1f - abs(c[2] - wave) / 7f) * max(0f, 1f - c[2] / 140f)
            if (lift < 0.05f) return@forEachIndexed
            stroke.color = if (lift > 0.3f) palette.core else palette.line
            stroke.alpha = (255 * 0.5f * lift * s).toInt()
            canvas.drawPath(cellPaths[i], stroke)
        }

        // Les arêtes tournées vers le réacteur s'éclairent à chaque battement ; les grilles aussi.
        facing.forEach { e ->
            stroke.color = if (e.face > 0.45f) palette.core else palette.line
            stroke.alpha = (255 * min(1f, 0.5f * e.face * (0.5f + beat)) * s).toInt()
            stroke.strokeWidth = (0.8f + 0.8f * e.face) * density
            canvas.drawLine(e.a.x, e.a.y, e.b.x, e.b.y, stroke)
        }
        stroke.strokeWidth = 0.6f * density
        grilles.forEach { (near, g) ->
            stroke.color = palette.core
            stroke.alpha = (255 * 0.4f * near * beat * s).toInt()
            canvas.drawLine(g[0], g[1], g[2], g[3], stroke)
        }

        // Les tiges des vérins : elles se détendent au rythme du cœur en charge.
        val reach = if (state.charging) beat * 2.2f else 0f
        for (vx in floatArrayOf(11.6f, 88.4f)) {
            stroke.color = palette.core
            stroke.alpha = (230 * s).toInt()
            stroke.strokeWidth = 0.9f * density
            seg(canvas, vx, 168f, vx, 190f + reach, stroke)
            stroke.color = palette.line
            stroke.alpha = (204 * s).toInt()
            stroke.strokeWidth = 0.8f * density
            canvas.drawRect(x(vx - 1.8f), y(190f + reach), x(vx + 1.8f), y(192.4f + reach), stroke)
        }

        // L'onde de chaque battement dans les conduits ; le réseau vient des coins.
        val run = phase * 1.6f
        if (run < 1f) pipeRoutes.forEach {
            it.at(run, point)
            spark(canvas, ink, point, 0.8f * (1f - run * 0.5f) * s)
        }
        if (net) for (i in 1..2) {
            val t = (seconds * 0.9f + i * 0.37f) % 1f
            pipeRoutes[i].at(1f - t, point)
            spark(canvas, ink, point, sin(t * PI.toFloat()) * s)
        }
        // En charge : le courant entre par le port et monte au réacteur.
        if (state.charging) {
            stroke.color = palette.core
            stroke.alpha = (255 * (0.5f + 0.3f * beat) * s).toInt()
            stroke.strokeWidth = 1.2f * density
            canvas.drawPath(path(pipes[7], false), stroke)
            for (j in 0 until 4) {
                val t = (seconds * 0.6f + j / 4f) % 1f
                pipeRoutes[7].at(1f - t, point)
                spark(canvas, ink, point, s, 0.45f)
            }
            ink.halo(canvas, port, x(8f), ((100 + 76 * beat) * s).toInt())
        }

        // Le conduit central luit entre les vertèbres.
        var vy = 130f
        while (vy < 197f) {
            val live = if (state.charging) max(0f, sin(seconds * 8f + (vy - 130f) / 7f)) else beat * 0.4f
            stroke.color = palette.core
            stroke.alpha = (255 * min(1f, 0.35f + 0.6f * live) * s).toInt()
            stroke.strokeWidth = 0.9f * density
            seg(canvas, 49f, vy + 4.6f, 51f, vy + 4.6f, stroke)
            vy += 5.6f
        }
        // Les voyants : ils clignotent en alternance, vite avec du réseau.
        for ((lx, offset) in listOf(15.5f to 0f, 84.5f to 0.5f)) {
            val on = if ((seconds * (if (net) 3f else 0.8f) + offset) % 1f < 0.5f) 1f else 0.2f
            val at = p(lx, 16.5f)
            ink.halo(canvas, at, x(2.2f), (128 * on * s).toInt())
            ink.fill.color = palette.core
            ink.fill.alpha = (255 * on * s).toInt()
            canvas.drawCircle(at.x, at.y, x(0.6f), ink.fill)
        }

        drawLiveReactor(canvas, state, ink, s, beat, lit)

        // Un reflet qui balaie l'armure de temps en temps.
        val gl = ((seconds / 7f) % 1f) * 2.6f - 0.8f
        if (gl > -0.6f && gl < 1.6f) {
            if (glint == null) glint = LinearGradient(-x(18f), 0f, x(18f), x(40f), intArrayOf(Color.TRANSPARENT, Color.argb(16, 255, 255, 255), Color.TRANSPARENT), null, Shader.TileMode.CLAMP)
            canvas.save()
            canvas.clipPath(glintPath)
            canvas.translate(x(gl * 100f), 0f)
            ink.fill.shader = glint
            ink.fill.alpha = (255 * s).toInt()
            canvas.drawRect(-x(60f), 0f, x(60f), y(216f), ink.fill)
            ink.fill.shader = null
            canvas.restore()
        }
    }

    private fun drawLiveReactor(canvas: Canvas, state: FrameState, ink: Ink, s: Float, beat: Float, lit: Int) {
        val palette = ink.palette
        val stroke = ink.stroke
        val seconds = state.timeMillis / 1000f
        // La couronne graduée, qui tourne plus vite en charge.
        val spin = seconds * if (state.charging) 1.4f else 0.15f
        stroke.color = palette.line
        stroke.alpha = (178 * s).toInt()
        stroke.strokeWidth = 0.6f * density
        for (k in 0 until 72) {
            val a = spin + k / 72f * 2f * PI.toFloat()
            val q0 = ring(19.6f, a)
            val q1 = ring(if (k % 6 != 0) 20.3f else 20.8f, a)
            canvas.drawLine(q0.x, q0.y, q1.x, q1.y, stroke)
        }
        // Les bobines allumées : une par dixième de batterie, chacune à son rythme.
        if (coilShader == null || shaderColor != palette.glow) {
            shaderColor = palette.glow
            coilShader = RadialGradient(center.x, center.y, x(19f), intArrayOf(palette.glow, palette.line and 0x00FFFFFF or (0x30 shl 24)), floatArrayOf(0.5f, 1f), Shader.TileMode.CLAMP)
        }
        for (k in 0 until lit) {
            val flick = 0.78f + 0.22f * sin(seconds * (3f + k * 0.7f) + k)
            val last = if (k == lit - 1 && state.charging) 0.35f + 0.65f * (sin(seconds * 4.5f) + 1f) / 2f else 1f
            val a = flick * last * s
            ink.fill.shader = coilShader
            ink.fill.alpha = (140 * a).toInt()
            canvas.drawPath(coils[k], ink.fill)
            ink.fill.shader = null
            stroke.color = palette.core
            stroke.alpha = (242 * a).toInt()
            stroke.strokeWidth = 1f * density
            canvas.drawPath(coils[k], stroke)
            stroke.alpha = (166 * a).toInt()
            stroke.strokeWidth = 0.5f * density
            val a0 = -PI.toFloat() / 2f + k * PI.toFloat() / 5f + 0.07f
            val a1 = a0 + PI.toFloat() / 5f - 0.14f
            for (w in 0 until 8) arc(canvas, 11.9f + w * 0.85f, a0 + 0.05f, a1 - 0.05f, stroke)
            val lead = -PI.toFloat() / 2f + (k + 0.5f) * PI.toFloat() / 5f
            for (o in floatArrayOf(-0.04f, 0.04f)) {
                val q0 = ring(18.6f, lead + o)
                val q1 = ring(19.4f, lead + o)
                canvas.drawLine(q0.x, q0.y, q1.x, q1.y, stroke)
            }
        }
        // Le cœur : il bat.
        ink.halo(canvas, center, x(14f), ((140 + 100 * beat) * s).toInt())
        ink.fill.color = palette.core
        ink.fill.alpha = (255 * (0.85f + 0.15f * beat) * s).toInt()
        canvas.drawCircle(center.x, center.y, x(5.6f + 0.5f * beat), ink.fill)
        stroke.color = DARK
        stroke.alpha = (140 * s).toInt()
        stroke.strokeWidth = 0.7f * density
        canvas.drawCircle(center.x, center.y, x(3.7f), stroke)
        canvas.drawCircle(center.x, center.y, x(1.7f), stroke)
        stroke.color = palette.core
        stroke.alpha = (230 * s).toInt()
        stroke.strokeWidth = 0.8f * density
        for (k in 0 until 3) {
            val a = -PI.toFloat() / 2f + k * PI.toFloat() * 2f / 3f + spin * 0.5f
            val q0 = ring(6.6f, a)
            val q1 = ring(8.4f, a)
            canvas.drawLine(q0.x, q0.y, q1.x, q1.y, stroke)
        }
    }

    private companion object {
        const val CX = 50f
        const val CY = 98f
        val DARK = Color.rgb(58, 26, 12)

        /** Les abdominaux : haut, bas et bord extérieur de chaque rangée. */
        val ABS = listOf(Triple(140f, 158f, 14f), Triple(160f, 178f, 16f), Triple(180f, 196f, 19f))
    }
}
