package dev.levilainpetit.wux.wallpaper

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RadialGradient
import android.graphics.Shader
import kotlin.math.abs
import kotlin.math.sign
import kotlin.math.sin
import kotlin.random.Random

/**
 * Grille : un monde numérique aux couleurs du téléphone. Une grille à perte
 * de vue qui défile, des montagnes filaires, un portail lumineux posé sur
 * l'horizon, un vaisseau qui passe lentement dans le ciel, des particules
 * qui flottent, et trois motos de lumière qui tracent leurs murs en virant
 * à angle droit. Incliner le téléphone déplace le décor, les plans proches
 * plus que les lointains.
 */
class GridScene : LiveScene {

    override val colors: IntArray? get() = own

    private var own: IntArray? = null
    private var width = 1
    private var height = 1
    private var density = 1f
    private var horizon = 0f
    private var focal = 1f
    private var scroll = 0f
    private var time = 0f
    private var last = 0L
    private val random = Random(7)

    private val cycles = listOf(
        Cycle(0, -1f, 3f, 0f, 1f),
        Cycle(1, 1.5f, 7f, -1f, 0f),
        Cycle(2, 0.5f, 10f, 1f, 0f),
    )
    private val farRidge = Path()
    private val nearRidge = Path()
    private var facets = FloatArray(0)
    private var stars = FloatArray(0)
    private var motes = FloatArray(0)

    private val grid = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.STROKE }
    private val stroke = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
    }
    private val fill = Paint(Paint.ANTI_ALIAS_FLAG)
    private val sky = Paint()
    private val floor = Paint()
    private val portal = Paint(Paint.ANTI_ALIAS_FLAG)
    private val wall = Path()
    private val craft = Path()
    private var lockShade = Paint()
    private var shaded = 0

    override fun resize(width: Int, height: Int, density: Float) {
        this.width = width
        this.height = height
        this.density = density
        horizon = height * 0.46f
        focal = width * 0.9f
        ridge(farRidge, Random(3), 0.07f, 0.035f)
        ridge(nearRidge, Random(5), 0.045f, 0.02f)
        // Les arêtes des montagnes proches, des sommets vers la base.
        val r = Random(9)
        facets = FloatArray(24 * 4) { i ->
            when (i % 4) {
                0 -> -width * 0.3f + r.nextFloat() * width * 1.6f
                1 -> horizon - height * (0.02f + r.nextFloat() * 0.05f)
                2 -> 0f
                else -> horizon
            }
        }
        for (i in facets.indices step 4) facets[i + 2] = facets[i] + (r.nextFloat() - 0.5f) * width * 0.12f
        stars = FloatArray(70 * 3) { i ->
            when (i % 3) {
                0 -> random.nextFloat() * width
                1 -> random.nextFloat() * horizon * 0.85f
                else -> random.nextFloat()
            }
        }
        motes = FloatArray(40 * 3) { i ->
            when (i % 3) {
                0 -> random.nextFloat() * width
                1 -> horizon + random.nextFloat() * (height - horizon)
                else -> random.nextFloat()
            }
        }
        lockShade = SceneKit.lockShade(height)
        shaded = 0
        // Des murs déjà tracés, dès la première image.
        repeat(80) { simulate(0.05f) }
    }

    override fun draw(canvas: Canvas, frame: LiveFrame) {
        val dt = SceneKit.step(last, frame.timeMillis)
        last = frame.timeMillis
        time += dt
        simulate(dt)
        val strength = SceneKit.strength(frame)
        val accent = SceneKit.neon(frame.palette)
        val second = SceneKit.second(frame.palette)
        val core = frame.palette.core
        val night = SceneKit.night(frame.palette, 0.03f)
        own = intArrayOf(accent, second, night)
        prepare(accent, night)
        val shift = 30f * density

        canvas.drawColor(night)
        canvas.drawRect(0f, 0f, width.toFloat(), horizon, sky)
        drawStars(canvas, frame, strength, core)

        // Au plus loin : le portail, le vaisseau, la crête lointaine.
        canvas.save()
        canvas.translate(-frame.tiltX * shift * 0.2f, -frame.tiltY * shift * 0.15f)
        drawPortal(canvas, strength, accent, core)
        drawCraft(canvas, strength, accent)
        drawRidge(canvas, farRidge, accent, night, 110, strength)
        canvas.restore()

        canvas.save()
        canvas.translate(-frame.tiltX * shift * 0.45f, -frame.tiltY * shift * 0.25f)
        drawRidge(canvas, nearRidge, accent, SceneKit.night(frame.palette, 0.015f), 200, strength)
        stroke.color = accent
        stroke.strokeWidth = 1f * density
        stroke.alpha = (60 * strength).toInt()
        canvas.drawLines(facets, stroke)
        canvas.restore()

        // Le sol, les murs, les particules : ce qui bouge le plus.
        canvas.save()
        canvas.translate(-frame.tiltX * shift, -frame.tiltY * shift * 0.5f)
        canvas.drawRect(-shift, horizon, width + shift, height + shift, floor)
        drawReflection(canvas, strength, accent)
        drawGrid(canvas, strength)
        for (cycle in cycles.sortedByDescending { it.z }) {
            drawCycle(canvas, cycle, strength, if (cycle.index == 1) second else if (cycle.index == 2) SceneKit.mix(core, accent, 0.4f) else accent)
        }
        drawMotes(canvas, dt, strength, accent)
        canvas.restore()

        val line = horizon - frame.tiltY * shift * 0.5f
        stroke.color = accent
        stroke.alpha = (70 * strength).toInt()
        stroke.strokeWidth = 7f * density
        canvas.drawLine(-shift, line, width + shift, line, stroke)
        stroke.color = SceneKit.mix(accent, core, 0.5f)
        stroke.alpha = (240 * strength).toInt()
        stroke.strokeWidth = 1.8f * density
        canvas.drawLine(-shift, line, width + shift, line, stroke)
        if (frame.locked) canvas.drawRect(0f, 0f, width.toFloat(), height * 0.34f, lockShade)
    }

    override fun nextFrameIn(frame: LiveFrame): Long = 33L

    /** Les dégradés, recalculés seulement quand la couleur change. */
    private fun prepare(accent: Int, night: Int) {
        if (shaded == accent) return
        shaded = accent
        sky.shader = LinearGradient(
            0f, 0f, 0f, horizon,
            intArrayOf(night, SceneKit.mix(night, accent, 0.08f), SceneKit.mix(night, accent, 0.28f)),
            floatArrayOf(0f, 0.6f, 1f), Shader.TileMode.CLAMP,
        )
        floor.shader = LinearGradient(
            0f, horizon, 0f, height.toFloat(),
            intArrayOf(SceneKit.mix(night, accent, 0.22f), night, night),
            floatArrayOf(0f, 0.35f, 1f), Shader.TileMode.CLAMP,
        )
        grid.shader = LinearGradient(
            0f, horizon, 0f, height.toFloat(),
            intArrayOf(SceneKit.alpha(accent, 0), SceneKit.alpha(accent, 110), SceneKit.alpha(accent, 255)),
            floatArrayOf(0f, 0.2f, 1f), Shader.TileMode.CLAMP,
        )
        portal.shader = RadialGradient(
            width / 2f, horizon, width * 0.5f,
            intArrayOf(SceneKit.alpha(accent, 120), SceneKit.alpha(accent, 30), Color.TRANSPARENT),
            floatArrayOf(0f, 0.45f, 1f), Shader.TileMode.CLAMP,
        )
    }

    private fun sx(x: Float, z: Float) = width / 2f + x * focal / z
    private fun sy(z: Float, h: Float = 0f) = horizon + (CAMERA - h) * focal / z

    private fun ridge(path: Path, r: Random, tall: Float, low: Float) {
        path.rewind()
        var x = -width * 0.3f
        path.moveTo(x, horizon)
        while (x < width * 1.3f) {
            x += width * (0.04f + r.nextFloat() * 0.07f)
            path.lineTo(x, horizon - height * (low + r.nextFloat() * tall))
        }
        path.lineTo(width * 1.3f, horizon)
        path.close()
    }

    private fun drawRidge(canvas: Canvas, path: Path, accent: Int, fillColor: Int, alpha: Int, strength: Float) {
        fill.shader = null
        fill.color = fillColor
        canvas.drawPath(path, fill)
        stroke.color = accent
        stroke.strokeWidth = 1.3f * density
        stroke.alpha = (alpha * strength).toInt()
        canvas.drawPath(path, stroke)
    }

    private fun drawStars(canvas: Canvas, frame: LiveFrame, strength: Float, core: Int) {
        fill.shader = null
        fill.color = core
        for (i in stars.indices step 3) {
            val twinkle = 0.5f + 0.5f * sin(time * 1.3f + i)
            fill.alpha = (110 * stars[i + 2] * twinkle * strength).toInt()
            canvas.drawCircle(stars[i] - frame.tiltX * 4 * density, stars[i + 1], (0.6f + stars[i + 2]) * density, fill)
        }
    }

    /** Le portail : un anneau posé sur l'horizon, son halo et son faisceau. */
    private fun drawPortal(canvas: Canvas, strength: Float, accent: Int, core: Int) {
        val cx = width / 2f
        val radius = width * 0.2f
        portal.alpha = (255 * strength).toInt()
        canvas.drawRect(0f, horizon - width * 0.5f, width.toFloat(), horizon, portal)
        canvas.save()
        canvas.clipRect(0f, 0f, width.toFloat(), horizon)
        stroke.color = accent
        for ((size, alpha) in listOf(18f to 25, 8f to 60, 2.5f to 230)) {
            stroke.strokeWidth = size * density
            stroke.alpha = (alpha * strength).toInt()
            canvas.drawCircle(cx, horizon, radius, stroke)
        }
        stroke.color = core
        stroke.strokeWidth = 1f * density
        stroke.alpha = (200 * strength).toInt()
        canvas.drawCircle(cx, horizon, radius * 0.92f, stroke)
        canvas.restore()
        // Le faisceau qui monte du portail.
        val pulse = 0.7f + 0.3f * sin(time * 0.9f)
        stroke.color = accent
        for ((size, alpha) in listOf(10f to 22, 3f to 90, 1f to 220)) {
            stroke.strokeWidth = size * density
            stroke.alpha = (alpha * strength * pulse).toInt()
            canvas.drawLine(cx, 0f, cx, horizon - radius, stroke)
        }
    }

    /** Un vaisseau en arche qui traverse lentement le ciel. */
    private fun drawCraft(canvas: Canvas, strength: Float, accent: Int) {
        val span = width * 0.26f
        val progress = (time / 45f) % 1f
        val cx = -span + (width + span * 2) * progress
        val cy = height * 0.17f + sin(time * 0.5f) * 6f * density
        val h = span * 0.42f
        val leg = span * 0.2f
        craft.rewind()
        craft.moveTo(cx - span / 2, cy + h)
        craft.lineTo(cx - span / 2, cy)
        craft.lineTo(cx + span / 2, cy)
        craft.lineTo(cx + span / 2, cy + h)
        craft.lineTo(cx + span / 2 - leg, cy + h)
        craft.lineTo(cx + span / 2 - leg, cy + h * 0.35f)
        craft.lineTo(cx - span / 2 + leg, cy + h * 0.35f)
        craft.lineTo(cx - span / 2 + leg, cy + h)
        craft.close()
        fill.shader = null
        fill.color = Color.rgb(2, 3, 6)
        canvas.drawPath(craft, fill)
        stroke.color = accent
        stroke.strokeWidth = 1.4f * density
        stroke.alpha = (170 * strength).toInt()
        canvas.drawPath(craft, stroke)
        // Ses feux.
        fill.color = accent
        fill.alpha = (230 * strength).toInt()
        canvas.drawRect(cx - span * 0.3f, cy + h * 0.14f, cx + span * 0.3f, cy + h * 0.2f, fill)
    }

    /** Le reflet du portail sur le sol lisse, qui s'efface vers soi. */
    private fun drawReflection(canvas: Canvas, strength: Float, accent: Int) {
        val cx = width / 2f
        val depth = height * 0.3f
        for ((half, alpha) in listOf(width * 0.2f to 26, width * 0.05f to 50, 1.5f * density to 110)) {
            fill.shader = LinearGradient(
                0f, horizon, 0f, horizon + depth,
                SceneKit.alpha(accent, (alpha * strength).toInt()), Color.TRANSPARENT, Shader.TileMode.CLAMP,
            )
            canvas.drawRect(cx - half, horizon, cx + half, horizon + depth, fill)
        }
        fill.shader = null
    }

    private fun drawGrid(canvas: Canvas, strength: Float) {
        grid.alpha = (255 * strength).toInt()
        grid.strokeWidth = 1.1f * density
        // Une maille d'une demi-unité : dense sous les pieds, fine au loin.
        for (i in -LANES * 2..LANES * 2) {
            val x = i * CELL
            canvas.drawLine(sx(x, NEAR), sy(NEAR), sx(x, FAR), sy(FAR), grid)
        }
        val offset = scroll % CELL
        var z = CELL - offset
        while (z < FAR) {
            if (z > NEAR) canvas.drawLine(sx(-LANES.toFloat(), z), sy(z), sx(LANES.toFloat(), z), sy(z), grid)
            z += CELL
        }
    }

    private fun drawMotes(canvas: Canvas, dt: Float, strength: Float, accent: Int) {
        fill.shader = null
        fill.color = accent
        for (i in motes.indices step 3) {
            motes[i + 1] -= dt * (8f + 14f * motes[i + 2]) * density
            if (motes[i + 1] < horizon) motes[i + 1] = height.toFloat()
            val fade = ((motes[i + 1] - horizon) / (height - horizon)).coerceIn(0f, 1f)
            fill.alpha = (140 * fade * strength).toInt()
            canvas.drawCircle(motes[i], motes[i + 1], (0.8f + motes[i + 2] * 1.4f) * density, fill)
        }
    }

    private fun drawCycle(canvas: Canvas, cycle: Cycle, strength: Float, color: Int) {
        val points = cycle.trail + floatArrayOf(cycle.x, cycle.z)
        // Le mur, segment par segment, plus vif vers la moto.
        for (i in 0 until points.size - 1) {
            val (x0, z0) = points[i]
            val (x1, z1) = points[i + 1]
            if (z0 < NEAR || z1 < NEAR) continue
            val age = (i + 1f) / (points.size - 1f)
            wall.rewind()
            wall.moveTo(sx(x0, z0), sy(z0))
            wall.lineTo(sx(x1, z1), sy(z1))
            wall.lineTo(sx(x1, z1), sy(z1, WALL))
            wall.lineTo(sx(x0, z0), sy(z0, WALL))
            wall.close()
            fill.shader = null
            fill.color = color
            fill.alpha = ((25 + 70 * age) * strength).toInt()
            canvas.drawPath(wall, fill)
            stroke.color = color
            stroke.alpha = ((40 + 60 * age) * strength).toInt()
            stroke.strokeWidth = 8f * density
            canvas.drawLine(sx(x0, z0), sy(z0, WALL), sx(x1, z1), sy(z1, WALL), stroke)
            stroke.alpha = ((120 + 135 * age) * strength).toInt()
            stroke.strokeWidth = 2f * density
            canvas.drawLine(sx(x0, z0), sy(z0, WALL), sx(x1, z1), sy(z1, WALL), stroke)
            stroke.alpha = ((60 + 80 * age) * strength).toInt()
            stroke.strokeWidth = 1.2f * density
            canvas.drawLine(sx(x0, z0), sy(z0), sx(x1, z1), sy(z1), stroke)
        }
        // La moto : une silhouette allongée et son halo.
        if (cycle.z > NEAR) {
            val x = sx(cycle.x, cycle.z)
            val y = sy(cycle.z, WALL * 0.45f)
            val r = (focal / cycle.z) * 0.05f
            fill.color = color
            fill.alpha = (45 * strength).toInt()
            canvas.drawCircle(x, y, r * 2.4f, fill)
            fill.alpha = (200 * strength).toInt()
            canvas.drawRoundRect(x - r * 1.8f, y - r * 0.9f, x + r * 1.8f, y + r * 0.9f, r, r, fill)
            fill.color = Color.WHITE
            fill.alpha = (255 * strength).toInt()
            canvas.drawCircle(x, y, r * 0.6f, fill)
        }
    }

    /** Fait avancer le monde vers soi et les motos sur la grille, de [dt] secondes. */
    private fun simulate(dt: Float) {
        if (dt <= 0f) return
        val approach = SCROLL * dt
        scroll += approach
        for (cycle in cycles) {
            for (p in cycle.trail) p[1] -= approach
            cycle.z -= approach
            cycle.x += cycle.dx * SPEED * dt
            cycle.z += cycle.dz * SPEED * dt
            cycle.untilTurn -= dt
            val out = (cycle.dx != 0f && abs(cycle.x) > 2.4f && sign(cycle.x) == cycle.dx) ||
                (cycle.dz < 0f && cycle.z < 2.4f) || (cycle.dx != 0f && cycle.z < 2f) || (cycle.dz > 0f && cycle.z > 14f)
            if (out || cycle.untilTurn <= 0f) turn(cycle)
            trim(cycle)
        }
    }

    private fun turn(cycle: Cycle) {
        cycle.trail += floatArrayOf(cycle.x, cycle.z)
        if (cycle.dx != 0f) {
            cycle.dx = 0f
            cycle.dz = if (cycle.z < 8f) 1f else -1f
        } else {
            cycle.dz = 0f
            cycle.dx = if (abs(cycle.x) < 0.5f) (if (random.nextBoolean()) 1f else -1f) else -sign(cycle.x)
        }
        cycle.untilTurn = 1f + random.nextFloat() * 2.5f
    }

    /** Le mur ne garde que ses [TRAIL] dernières unités. */
    private fun trim(cycle: Cycle) {
        var length = 0f
        var px = cycle.x
        var pz = cycle.z
        for (i in cycle.trail.indices.reversed()) {
            val p = cycle.trail[i]
            val segment = abs(p[0] - px) + abs(p[1] - pz)
            if (length + segment > TRAIL) {
                val keep = (TRAIL - length) / segment
                p[0] = px + (p[0] - px) * keep
                p[1] = pz + (p[1] - pz) * keep
                repeat(i) { cycle.trail.removeAt(0) }
                return
            }
            length += segment
            px = p[0]
            pz = p[1]
        }
    }

    private class Cycle(val index: Int, var x: Float, var z: Float, var dx: Float, var dz: Float) {
        val trail = mutableListOf(floatArrayOf(x, z))
        var untilTurn = 1.5f + index
    }

    private companion object {
        const val CAMERA = 1f
        const val WALL = 0.35f
        const val NEAR = 0.45f
        const val FAR = 60f
        const val LANES = 24
        const val CELL = 0.5f
        const val SCROLL = 0.5f
        const val SPEED = 2.2f
        const val TRAIL = 9f
    }
}
