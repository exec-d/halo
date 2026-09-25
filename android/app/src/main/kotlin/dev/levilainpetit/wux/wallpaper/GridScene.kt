package dev.levilainpetit.wux.wallpaper

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Shader
import kotlin.math.abs
import kotlin.math.sign
import kotlin.random.Random

/**
 * Grille : une grille lumineuse à perte de vue, qui défile lentement, et deux
 * motos de lumière qui y tracent leurs murs, virant à angle droit. Incliner
 * le téléphone déplace le décor, les plans proches plus que les lointains.
 */
class GridScene : LiveScene {

    override val colors = intArrayOf(CYAN, ORANGE, BACKGROUND)

    private var width = 1
    private var height = 1
    private var density = 1f
    private var horizon = 0f
    private var focal = 1f
    private var scroll = 0f
    private var last = 0L
    private val random = Random(7)

    private val cycles = listOf(Cycle(CYAN, -1f, 3f, 0f, 1f), Cycle(ORANGE, 1.5f, 7f, -1f, 0f))
    private val ridge = Path()

    private val grid = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.STROKE }
    private val stroke = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
    }
    private val fill = Paint(Paint.ANTI_ALIAS_FLAG)
    private val glow = Paint()
    private val wall = Path()
    private var lockShade = Paint()

    override fun resize(width: Int, height: Int, density: Float) {
        this.width = width
        this.height = height
        this.density = density
        horizon = height * 0.44f
        focal = width * 0.9f
        grid.shader = LinearGradient(
            0f, horizon, 0f, height.toFloat(),
            intArrayOf(SceneKit.alpha(CYAN, 0), SceneKit.alpha(CYAN, 90), SceneKit.alpha(CYAN, 230)),
            floatArrayOf(0f, 0.25f, 1f), Shader.TileMode.CLAMP,
        )
        glow.shader = LinearGradient(
            0f, horizon - height * 0.22f, 0f, horizon,
            SceneKit.alpha(CYAN, 0), SceneKit.alpha(CYAN, 80), Shader.TileMode.CLAMP,
        )
        ridge.rewind()
        val r = Random(3)
        var x = -width * 0.3f
        ridge.moveTo(x, horizon)
        while (x < width * 1.3f) {
            x += width * (0.05f + r.nextFloat() * 0.08f)
            ridge.lineTo(x, horizon - height * (0.01f + r.nextFloat() * 0.06f))
        }
        ridge.lineTo(width * 1.3f, horizon)
        lockShade = SceneKit.lockShade(height)
        // Des murs déjà tracés, dès la première image.
        repeat(80) { simulate(0.05f) }
    }

    override fun draw(canvas: Canvas, frame: LiveFrame) {
        simulate(SceneKit.step(last, frame.timeMillis))
        last = frame.timeMillis
        val strength = SceneKit.strength(frame)
        val shift = 30f * density
        canvas.drawColor(BACKGROUND)

        // Lueur et crête lointaines, qui bougent peu.
        canvas.save()
        canvas.translate(-frame.tiltX * shift * 0.3f, -frame.tiltY * shift * 0.2f)
        canvas.drawRect(-width.toFloat(), horizon - height * 0.22f, width * 2f, horizon, glow)
        stroke.color = CYAN
        stroke.alpha = (120 * strength).toInt()
        stroke.strokeWidth = 1.2f * density
        canvas.drawPath(ridge, stroke)
        canvas.restore()

        // Le sol et les murs, qui bougent davantage.
        canvas.save()
        canvas.translate(-frame.tiltX * shift, -frame.tiltY * shift * 0.5f)
        drawGrid(canvas, strength)
        for (cycle in cycles) drawCycle(canvas, cycle, strength)
        canvas.restore()

        stroke.color = CYAN
        stroke.alpha = (230 * strength).toInt()
        stroke.strokeWidth = 2f * density
        canvas.drawLine(-shift, horizon - frame.tiltY * shift * 0.5f, width + shift, horizon - frame.tiltY * shift * 0.5f, stroke)
        if (frame.locked) canvas.drawRect(0f, 0f, width.toFloat(), height * 0.34f, lockShade)
    }

    override fun nextFrameIn(frame: LiveFrame): Long = 33L

    private fun sx(x: Float, z: Float) = width / 2f + x * focal / z
    private fun sy(z: Float, h: Float = 0f) = horizon + (CAMERA - h) * focal / z

    private fun drawGrid(canvas: Canvas, strength: Float) {
        grid.alpha = (255 * strength).toInt()
        grid.strokeWidth = 1.2f * density
        for (i in -LANES..LANES) {
            canvas.drawLine(sx(i.toFloat(), NEAR), sy(NEAR), sx(i.toFloat(), FAR), sy(FAR), grid)
        }
        val offset = scroll % 1f
        var z = 1f - offset
        while (z < FAR) {
            if (z > NEAR) canvas.drawLine(sx(-LANES.toFloat(), z), sy(z), sx(LANES.toFloat(), z), sy(z), grid)
            z += 1f
        }
    }

    private fun drawCycle(canvas: Canvas, cycle: Cycle, strength: Float) {
        val points = cycle.trail + floatArrayOf(cycle.x, cycle.z)
        // Le mur : une bande entre le sol et sa crête, plus une arête vive.
        wall.rewind()
        for (i in 0 until points.size - 1) {
            val (x0, z0) = points[i]
            val (x1, z1) = points[i + 1]
            if (z0 < NEAR || z1 < NEAR) continue
            wall.moveTo(sx(x0, z0), sy(z0))
            wall.lineTo(sx(x1, z1), sy(z1))
            wall.lineTo(sx(x1, z1), sy(z1, WALL))
            wall.lineTo(sx(x0, z0), sy(z0, WALL))
            wall.close()
        }
        fill.color = cycle.color
        fill.alpha = (70 * strength).toInt()
        canvas.drawPath(wall, fill)
        wall.rewind()
        var started = false
        for ((x, z) in points) {
            if (z < NEAR) {
                started = false
                continue
            }
            if (started) wall.lineTo(sx(x, z), sy(z, WALL)) else wall.moveTo(sx(x, z), sy(z, WALL))
            started = true
        }
        stroke.color = cycle.color
        stroke.alpha = (60 * strength).toInt()
        stroke.strokeWidth = 7f * density
        canvas.drawPath(wall, stroke)
        stroke.alpha = (255 * strength).toInt()
        stroke.strokeWidth = 1.8f * density
        canvas.drawPath(wall, stroke)

        // La moto, un point éclatant au bout du mur.
        if (cycle.z > NEAR) {
            val x = sx(cycle.x, cycle.z)
            val y = sy(cycle.z, WALL * 0.5f)
            val r = (focal / cycle.z) * 0.06f
            fill.alpha = (80 * strength).toInt()
            canvas.drawCircle(x, y, r * 2.5f, fill)
            fill.color = Color.WHITE
            fill.alpha = (255 * strength).toInt()
            canvas.drawCircle(x, y, r * 0.8f, fill)
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
            val out = (cycle.dx != 0f && abs(cycle.x) > 2.2f && sign(cycle.x) == cycle.dx) ||
                (cycle.dz < 0f && cycle.z < 2.4f) || (cycle.dx != 0f && cycle.z < 2f) || (cycle.dz > 0f && cycle.z > 12f)
            if (out || cycle.untilTurn <= 0f) turn(cycle)
            trim(cycle)
        }
    }

    private fun turn(cycle: Cycle) {
        cycle.trail += floatArrayOf(cycle.x, cycle.z)
        if (cycle.dx != 0f) {
            cycle.dx = 0f
            cycle.dz = if (cycle.z < 7f) 1f else -1f
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

    private class Cycle(val color: Int, var x: Float, var z: Float, var dx: Float, var dz: Float) {
        val trail = mutableListOf(floatArrayOf(x, z))
        var untilTurn = 1.5f
    }

    private companion object {
        val CYAN = Color.rgb(41, 231, 255)
        val ORANGE = Color.rgb(255, 138, 31)
        val BACKGROUND = Color.rgb(2, 4, 10)
        const val CAMERA = 1f
        const val WALL = 0.35f
        const val NEAR = 0.45f
        const val FAR = 60f
        const val LANES = 24
        const val SCROLL = 0.5f
        const val SPEED = 2.2f
        const val TRAIL = 9f
    }
}
