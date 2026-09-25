package dev.levilainpetit.wux.wallpaper

import android.app.KeyguardManager
import android.app.WallpaperColors
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.net.TrafficStats
import android.os.BatteryManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.service.wallpaper.WallpaperService
import android.view.SurfaceHolder
import androidx.annotation.RequiresApi
import dev.levilainpetit.wux.system.BatteryHistory
import dev.levilainpetit.wux.widgets.SystemStatus
import kotlin.math.abs
import kotlin.math.log10
import kotlin.math.roundToInt
import kotlin.random.Random

/**
 * Fond d'écran animé « Circuit » : l'intérieur du téléphone en schéma néon.
 *
 * - les plans glissent selon l'inclinaison du téléphone (capteur de gravité) ;
 * - la batterie dessinée suit le vrai niveau et respire pendant la charge ;
 * - des impulsions courent sur les pistes quand le réseau échange des données,
 *   et les antennes s'allument selon la force du signal ;
 * - à l'allumage de l'écran, composants et pistes s'illuminent un à un
 *   depuis le processeur.
 *
 * Pour la batterie, rien ne tourne quand le fond n'est pas visible, et l'on ne
 * dessine en continu que pendant une animation ; au repos, seule une nouvelle
 * inclinaison, une impulsion ou un changement d'état réveille le dessin.
 */
class HaloWallpaperService : WallpaperService() {

    override fun onCreateEngine(): Engine = CircuitEngine()

    inner class CircuitEngine : Engine(), SensorEventListener {
        private val handler = Handler(Looper.getMainLooper())
        private val state = FrameState()
        private val random = Random(SystemClock.uptimeMillis())

        private var scene: CircuitScene? = null
        private var painter: CircuitPainter? = null
        private var palette = CircuitPalette.of(this@HaloWallpaperService)
        private var visible = false
        private var frameScheduled = false

        private var ignitionStart = 0L
        private var pendingIgnition = true
        private var lastFrame = 0L

        private var targetX = 0f
        private var targetY = 0f
        private var baseX = Float.NaN
        private var baseY = Float.NaN

        private var lastBytes = -1L
        private var nextHeartbeat = 0L
        private var signalCheckedAt = 0L

        private val sensors by lazy { getSystemService(SensorManager::class.java) }
        private val keyguard by lazy { getSystemService(KeyguardManager::class.java) }

        private val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                when (intent.action) {
                    Intent.ACTION_SCREEN_ON -> pendingIgnition = true
                    Intent.ACTION_BATTERY_CHANGED -> {
                        readBattery(intent)
                        requestFrame()
                    }
                }
            }
        }

        private val frame = Runnable {
            frameScheduled = false
            drawFrame()
        }

        private val sampler = object : Runnable {
            override fun run() {
                sampleTraffic()
                handler.postDelayed(this, SAMPLE_MILLIS)
            }
        }

        override fun onCreate(surfaceHolder: SurfaceHolder) {
            super.onCreate(surfaceHolder)
            val filter = IntentFilter().apply {
                addAction(Intent.ACTION_SCREEN_ON)
                addAction(Intent.ACTION_BATTERY_CHANGED)
            }
            val sticky = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                registerReceiver(receiver, filter, Context.RECEIVER_NOT_EXPORTED)
            } else {
                registerReceiver(receiver, filter)
            }
            sticky?.let { readBattery(it) }
        }

        override fun onDestroy() {
            super.onDestroy()
            unregisterReceiver(receiver)
            stop()
            scene?.recycle()
            scene = null
        }

        override fun onSurfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
            super.onSurfaceChanged(holder, format, width, height)
            if (width <= 0 || height <= 0) return
            if (scene?.width == width && scene?.height == height) return
            scene?.recycle()
            val built = CircuitScene.build(width, height, resources.displayMetrics.density)
            scene = built
            painter = CircuitPainter(built)
            state.pulses.clear()
            requestFrame()
        }

        override fun onSurfaceDestroyed(holder: SurfaceHolder) {
            super.onSurfaceDestroyed(holder)
            stop()
        }

        override fun onVisibilityChanged(visible: Boolean) {
            this.visible = visible
            if (visible) {
                refreshPalette()
                state.intensity = WallpaperSettings.intensity(this@HaloWallpaperService)
                sensors?.let { manager ->
                    val sensor = manager.getDefaultSensor(Sensor.TYPE_GRAVITY)
                        ?: manager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
                    sensor?.let { manager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI) }
                }
                lastBytes = -1L
                handler.post(sampler)
                if (pendingIgnition && !isPreview) {
                    pendingIgnition = false
                    ignitionStart = SystemClock.uptimeMillis()
                    state.ignition = 0f
                }
                requestFrame()
            } else {
                stop()
            }
        }

        private fun stop() {
            sensors?.unregisterListener(this)
            handler.removeCallbacks(sampler)
            handler.removeCallbacks(frame)
            frameScheduled = false
        }

        // ——— Capteur : l'inclinaison, par rapport à la position habituelle ———

        override fun onSensorChanged(event: SensorEvent) {
            val gx = event.values[0]
            val gy = event.values[1]
            if (baseX.isNaN()) {
                baseX = gx
                baseY = gy
            }
            // La position de repos suit lentement la main : c'est un
            // changement d'inclinaison qui fait bouger, pas une posture.
            baseX += (gx - baseX) * BASE_FOLLOW
            baseY += (gy - baseY) * BASE_FOLLOW
            targetX = ((gx - baseX) / TILT_RANGE).coerceIn(-1f, 1f)
            targetY = (-(gy - baseY) / TILT_RANGE).coerceIn(-1f, 1f)
            if (abs(targetX - state.tiltX) > TILT_EPSILON || abs(targetY - state.tiltY) > TILT_EPSILON) {
                requestFrame()
            }
        }

        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit

        // ——— État du téléphone ———

        private fun readBattery(intent: Intent) {
            val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
            val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, 100)
            if (level >= 0 && scale > 0) state.batteryLevel = level / scale.toFloat()
            state.charging = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, 0) != 0
            // Tant que le fond est là, la courbe du widget Batterie profite de chaque changement.
            BatteryHistory.record(this@HaloWallpaperService, (state.batteryLevel * 100).roundToInt(), state.charging)
        }

        private fun sampleTraffic() {
            val scene = scene ?: return
            val now = SystemClock.uptimeMillis()
            val bytes = TrafficStats.getTotalRxBytes() + TrafficStats.getTotalTxBytes()
            if (bytes >= 0 && lastBytes >= 0) {
                val delta = bytes - lastBytes
                // 1 impulsion vers 2 Ko, jusqu'à 5 au-delà de quelques Mo.
                val count = if (delta < 2048) 0 else minOf(5, (log10(delta / 1024.0) * 1.6).toInt() + 1)
                repeat(count) { spawn(scene.networkRoutes.random(random), fast = delta > 512 * 1024) }
            }
            lastBytes = bytes
            if (state.charging && random.nextFloat() < 0.6f) {
                spawn(scene.dataRoutes.random(random).reversed(), fast = false)
            }
            if (now >= nextHeartbeat) {
                nextHeartbeat = now + 4000 + random.nextLong(3000)
                spawn(scene.innerRoutes.random(random), fast = false)
            }
            if (now - signalCheckedAt > SIGNAL_MILLIS) {
                signalCheckedAt = now
                state.signal = (SystemStatus.signalLevel(this@HaloWallpaperService) ?: 0) / 4f
            }
        }

        private fun spawn(route: CircuitScene.Route, fast: Boolean) {
            if (state.pulses.size >= MAX_PULSES) return
            // Vitesse en fraction du parcours par seconde : ~1,2 s par parcours.
            val unitsPerSecond = (if (fast) 95f else 60f) * (scene?.width ?: 1) / 100f
            val speed = unitsPerSecond / route.length.coerceAtLeast(1f)
            state.pulses += Pulse(route, speed, progress = -random.nextFloat() * 0.3f)
            requestFrame()
        }

        private fun refreshPalette() {
            val next = CircuitPalette.of(this@HaloWallpaperService)
            if (next != palette) {
                palette = next
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) notifyColorsChanged()
            }
        }

        @RequiresApi(Build.VERSION_CODES.O_MR1)
        override fun onComputeColors(): WallpaperColors =
            // Les couleurs viennent déjà du téléphone : les annoncer telles
            // quelles garde Material You stable.
            WallpaperColors(Color.valueOf(palette.glow), Color.valueOf(palette.line), Color.valueOf(CircuitPainter.BACKGROUND))

        // ——— Dessin ———

        private fun requestFrame() {
            if (!visible || frameScheduled) return
            frameScheduled = true
            val wait = (FRAME_MILLIS - (SystemClock.uptimeMillis() - lastFrame)).coerceAtLeast(0L)
            handler.postDelayed(frame, wait)
        }

        private fun drawFrame() {
            val painter = painter ?: return
            if (!visible) return
            val now = SystemClock.uptimeMillis()
            val elapsed = if (lastFrame == 0L) 0f else ((now - lastFrame).coerceAtMost(100L)) / 1000f
            lastFrame = now

            state.timeMillis = now
            state.tiltX += (targetX - state.tiltX) * TILT_SMOOTHING
            state.tiltY += (targetY - state.tiltY) * TILT_SMOOTHING
            state.ignition = if (state.ignition < 1f) ((now - ignitionStart) / IGNITION_MILLIS.toFloat()).coerceIn(0f, 1f) else 1f
            state.locked = keyguard?.isKeyguardLocked == true
            state.pulses.forEach { it.progress += it.speed * elapsed }
            state.pulses.removeAll { it.progress > 1.1f }

            val holder = surfaceHolder
            val canvas = runCatching { holder.lockHardwareCanvas() }.getOrNull() ?: return
            try {
                painter.draw(canvas, state, palette)
            } finally {
                holder.unlockCanvasAndPost(canvas)
            }

            val moving = abs(targetX - state.tiltX) > TILT_EPSILON || abs(targetY - state.tiltY) > TILT_EPSILON
            if (moving || state.ignition < 1f || state.pulses.isNotEmpty() || state.charging) requestFrame()
        }
    }

    private companion object {
        const val FRAME_MILLIS = 33L
        const val SAMPLE_MILLIS = 1000L
        const val SIGNAL_MILLIS = 5000L
        const val IGNITION_MILLIS = 1300L
        const val MAX_PULSES = 14

        /** Écart de gravité (m/s²) qui donne le décalage maximal. */
        const val TILT_RANGE = 2.2f
        const val TILT_EPSILON = 0.004f
        const val TILT_SMOOTHING = 0.2f

        /** Part de l'écart rattrapée par la position de repos à chaque mesure. */
        const val BASE_FOLLOW = 0.006f
    }
}
