package dev.levilainpetit.wux.dream

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Canvas
import android.os.BatteryManager
import android.os.SystemClock
import android.view.View
import dev.levilainpetit.wux.wallpaper.CircuitPainter
import dev.levilainpetit.wux.wallpaper.CircuitPalette
import dev.levilainpetit.wux.wallpaper.CircuitScene
import dev.levilainpetit.wux.wallpaper.FrameState
import dev.levilainpetit.wux.wallpaper.Pulse
import dev.levilainpetit.wux.wallpaper.WallpaperSettings
import kotlin.random.Random

/**
 * Le décor Circuit dans une vue, pour l'écran de veille : immobile (le
 * téléphone est posé), la batterie qui charge, et de temps en temps une
 * impulsion. Allumé composant par composant à son apparition.
 */
class CircuitView(context: Context) : View(context) {

    private val state = FrameState()
    private val palette = CircuitPalette.of(context)
    private val random = Random(SystemClock.uptimeMillis())
    private var scene: CircuitScene? = null
    private var painter: CircuitPainter? = null
    private var lastFrame = 0L
    private var ignitionStart = 0L
    private var nextPulse = 0L

    init {
        state.intensity = WallpaperSettings.intensity(context)
        context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))?.let {
            val level = it.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
            val scale = it.getIntExtra(BatteryManager.EXTRA_SCALE, 100)
            if (level >= 0 && scale > 0) state.batteryLevel = level / scale.toFloat()
            state.charging = it.getIntExtra(BatteryManager.EXTRA_PLUGGED, 0) != 0
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        scene?.recycle()
        if (w <= 0 || h <= 0) return
        val built = CircuitScene.build(w, h, resources.displayMetrics.density)
        scene = built
        painter = CircuitPainter(built)
        ignitionStart = SystemClock.uptimeMillis()
        state.ignition = 0f
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        scene?.recycle()
        scene = null
        painter = null
    }

    override fun onDraw(canvas: Canvas) {
        val scene = scene ?: return
        val painter = painter ?: return
        val now = SystemClock.uptimeMillis()
        val elapsed = if (lastFrame == 0L) 0f else (now - lastFrame).coerceAtMost(200L) / 1000f
        lastFrame = now
        state.timeMillis = now
        if (state.ignition < 1f) state.ignition = ((now - ignitionStart) / IGNITION_MILLIS).coerceIn(0f, 1f)

        if (now >= nextPulse) {
            nextPulse = now + 2500 + random.nextLong(3500)
            val route = if (state.charging && random.nextBoolean()) scene.dataRoutes.random(random).reversed() else scene.innerRoutes.random(random)
            val speed = 60f * scene.width / 100f / route.length.coerceAtLeast(1f)
            state.pulses += Pulse(route, speed)
        }
        state.pulses.forEach { it.progress += it.speed * elapsed }
        state.pulses.removeAll { it.progress > 1.1f }

        painter.draw(canvas, state, palette)
        // Assez pour la respiration de la batterie et les impulsions, sans plus.
        postInvalidateDelayed(FRAME_MILLIS)
    }

    private companion object {
        const val FRAME_MILLIS = 50L
        const val IGNITION_MILLIS = 1600f
    }
}
