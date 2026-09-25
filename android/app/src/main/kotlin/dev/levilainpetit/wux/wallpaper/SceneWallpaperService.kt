package dev.levilainpetit.wux.wallpaper

import android.app.KeyguardManager
import android.app.WallpaperColors
import android.graphics.Color
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.service.wallpaper.WallpaperService
import android.view.SurfaceHolder
import androidx.annotation.RequiresApi
import kotlin.math.abs
import kotlin.math.asin
import kotlin.math.atan2

/**
 * Base des fonds d'écran animés autres que Circuit (Grille, Mégapole, Code,
 * Néon, Sentinelle) : cadence des images, capteurs
 * (seulement quand le fond est visible), intensité, couleurs du téléphone.
 */
abstract class SceneWallpaperService : WallpaperService() {

    abstract fun createScene(): LiveScene

    override fun onCreateEngine(): Engine = SceneEngine()

    inner class SceneEngine : Engine(), SensorEventListener {
        private val scene = createScene()
        private val handler = Handler(Looper.getMainLooper())
        private val frame = LiveFrame()
        private var visible = false
        private var ready = false
        private var scheduled = false
        private var lastFrame = 0L

        private var targetX = 0f
        private var targetY = 0f
        private var baseX = Float.NaN
        private var baseY = Float.NaN
        private var targetAzimuth = Float.NaN
        private var targetAltitude = 30f
        private var targetRoll = 0f
        private val rotation = FloatArray(9)

        private val sensors by lazy { getSystemService(SensorManager::class.java) }
        private val keyguard by lazy { getSystemService(KeyguardManager::class.java) }
        private val draw = Runnable {
            scheduled = false
            drawFrame()
        }

        override fun onSurfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
            super.onSurfaceChanged(holder, format, width, height)
            if (width <= 0 || height <= 0) return
            scene.resize(width, height, resources.displayMetrics.density)
            ready = true
            request(0)
        }

        override fun onSurfaceDestroyed(holder: SurfaceHolder) {
            super.onSurfaceDestroyed(holder)
            ready = false
            stop()
        }

        override fun onVisibilityChanged(visible: Boolean) {
            this.visible = visible
            if (!visible) {
                stop()
                return
            }
            frame.palette = CircuitPalette.of(this@SceneWallpaperService)
            frame.intensity = WallpaperSettings.intensity(this@SceneWallpaperService)
            scene.refresh()
            sensors?.let { manager ->
                val sensor = if (scene.usesOrientation) {
                    manager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
                        ?: manager.getDefaultSensor(Sensor.TYPE_GAME_ROTATION_VECTOR)
                } else {
                    manager.getDefaultSensor(Sensor.TYPE_GRAVITY) ?: manager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
                }
                sensor?.let { manager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI) }
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) notifyColorsChanged()
            request(0)
        }

        override fun onDestroy() {
            super.onDestroy()
            stop()
        }

        private fun stop() {
            sensors?.unregisterListener(this)
            handler.removeCallbacks(draw)
            scheduled = false
        }

        override fun onSensorChanged(event: SensorEvent) {
            if (event.sensor.type == Sensor.TYPE_ROTATION_VECTOR || event.sensor.type == Sensor.TYPE_GAME_ROTATION_VECTOR) {
                SensorManager.getRotationMatrixFromVector(rotation, event.values)
                // La direction qui sort du dos du téléphone (-Z), dans le repère
                // est / nord / zénith.
                val east = -rotation[2]
                val north = -rotation[5]
                val up = -rotation[8]
                targetAzimuth = Math.toDegrees(atan2(east, north).toDouble()).toFloat().let { (it + 360f) % 360f }
                targetAltitude = Math.toDegrees(asin(up.coerceIn(-1f, 1f).toDouble())).toFloat()
                // L'axe « haut » du téléphone, pour garder l'horizon droit.
                targetRoll = Math.toDegrees(atan2(-rotation[6].toDouble(), rotation[7].toDouble())).toFloat()
                frame.hasOrientation = true
            } else {
                val gx = event.values[0]
                val gy = event.values[1]
                if (baseX.isNaN()) {
                    baseX = gx
                    baseY = gy
                }
                baseX += (gx - baseX) * 0.006f
                baseY += (gy - baseY) * 0.006f
                targetX = ((gx - baseX) / 2.2f).coerceIn(-1f, 1f)
                targetY = (-(gy - baseY) / 2.2f).coerceIn(-1f, 1f)
            }
            if (moving()) request(0)
        }

        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit

        private fun moving(): Boolean {
            val tilt = abs(targetX - frame.tiltX) > 0.004f || abs(targetY - frame.tiltY) > 0.004f
            val orientation = !targetAzimuth.isNaN() &&
                (abs(angle(targetAzimuth - frame.azimuth)) > 0.2f || abs(targetAltitude - frame.altitude) > 0.2f)
            return tilt || orientation
        }

        @RequiresApi(Build.VERSION_CODES.O_MR1)
        override fun onComputeColors(): WallpaperColors {
            val own = scene.colors
            return if (own != null) {
                WallpaperColors(Color.valueOf(own[0]), Color.valueOf(own[1]), Color.valueOf(own[2]))
            } else {
                WallpaperColors(Color.valueOf(frame.palette.glow), Color.valueOf(frame.palette.line), Color.valueOf(CircuitPainter.BACKGROUND))
            }
        }

        private fun request(delay: Long) {
            if (!visible || !ready || scheduled) return
            scheduled = true
            val wait = maxOf(delay, 33L - (SystemClock.uptimeMillis() - lastFrame)).coerceAtLeast(0L)
            handler.postDelayed(draw, wait)
        }

        private fun drawFrame() {
            if (!visible || !ready) return
            lastFrame = SystemClock.uptimeMillis()
            frame.timeMillis = System.currentTimeMillis()
            frame.tiltX += (targetX - frame.tiltX) * 0.2f
            frame.tiltY += (targetY - frame.tiltY) * 0.2f
            if (!targetAzimuth.isNaN()) {
                frame.azimuth = (frame.azimuth + angle(targetAzimuth - frame.azimuth) * 0.2f + 360f) % 360f
                frame.altitude += (targetAltitude - frame.altitude) * 0.2f
                frame.roll += angle(targetRoll - frame.roll) * 0.2f
            }
            frame.locked = keyguard?.isKeyguardLocked == true
            val holder = surfaceHolder
            val canvas = runCatching { holder.lockHardwareCanvas() }.getOrNull() ?: return
            try {
                scene.draw(canvas, frame)
            } finally {
                holder.unlockCanvasAndPost(canvas)
            }
            val next = if (moving()) 33L else scene.nextFrameIn(frame)
            if (next != null) request(next)
        }

        /** Un écart d'angle ramené entre -180 et 180 degrés. */
        private fun angle(delta: Float): Float = ((delta + 540f) % 360f) - 180f
    }
}
