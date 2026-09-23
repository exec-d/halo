package dev.levilainpetit.wux.widgets

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.provider.Settings
import android.util.SizeF
import android.widget.RemoteViews
import dev.levilainpetit.wux.R

/**
 * Contrôles : lampe torche (bascule directement), Wi-Fi, Bluetooth, son et
 * appareil photo (ouvrent le panneau ou l'application correspondants).
 */
class ControlsWidget : NeonWidget() {

    override fun build(context: Context, size: SizeF): RemoteViews {
        val torchOn = settings(context).getBoolean(TORCH_STATE, false)
        val views = RemoteViews(context.packageName, R.layout.widget_controls)
        views.removeAllViews(R.id.controls_row)
        val wifi = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            Intent(Settings.Panel.ACTION_WIFI)
        } else {
            Intent(Settings.ACTION_WIFI_SETTINGS)
        }
        val volume = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            Intent(Settings.Panel.ACTION_VOLUME)
        } else {
            Intent(Settings.ACTION_SOUND_SETTINGS)
        }
        val buttons = listOf(
            Triple(if (torchOn) R.drawable.icon_flashlight_on else R.drawable.icon_flashlight_off, R.string.controls_torch, torchToggle(context)),
            Triple(R.drawable.icon_wifi, R.string.controls_wifi, activity(context, wifi, 41)),
            Triple(R.drawable.icon_bluetooth, R.string.controls_bluetooth, activity(context, Intent(Settings.ACTION_BLUETOOTH_SETTINGS), 42)),
            Triple(R.drawable.icon_volume, R.string.controls_sound, activity(context, volume, 43)),
            Triple(R.drawable.icon_camera, R.string.controls_camera, activity(context, Intent(MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA), 44)),
        )
        buttons.forEachIndexed { i, (icon, label, action) ->
            views.addView(
                R.id.controls_row,
                RemoteViews(context.packageName, R.layout.controls_button).apply {
                    setImageViewResource(R.id.control_icon, icon)
                    setTextViewText(R.id.control_label, context.getString(label))
                    setContentDescription(R.id.control_button, context.getString(label))
                    // La lampe allumée prend la couleur la plus vive.
                    if (i == 0 && torchOn) setInt(R.id.control_icon, "setColorFilter", context.getColor(R.color.clock_core))
                    setOnClickPendingIntent(R.id.control_button, action)
                },
            )
        }
        return views
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == ACTION_TORCH) {
            toggleTorch(context)
            return
        }
        super.onReceive(context, intent)
    }

    /**
     * Le système ne dit l'état de la lampe qu'à un rappel : on s'y inscrit, le
     * premier appel donne l'état actuel, on bascule et on se désinscrit.
     */
    private fun toggleTorch(context: Context) {
        val camera = context.getSystemService(CameraManager::class.java) ?: return
        val id = camera.cameraIdList.firstOrNull {
            camera.getCameraCharacteristics(it).get(CameraCharacteristics.FLASH_INFO_AVAILABLE) == true
        } ?: return
        val pending = goAsync()
        val handler = Handler(Looper.getMainLooper())
        val callback = object : CameraManager.TorchCallback() {
            var done = false
            override fun onTorchModeChanged(cameraId: String, enabled: Boolean) {
                if (cameraId != id || done) return
                done = true
                camera.unregisterTorchCallback(this)
                runCatching { camera.setTorchMode(id, !enabled) }
                settings(context).edit().putBoolean(TORCH_STATE, !enabled).apply()
                renderAll(context)
                pending.finish()
            }

            override fun onTorchModeUnavailable(cameraId: String) {
                if (cameraId != id || done) return
                done = true
                camera.unregisterTorchCallback(this)
                pending.finish()
            }
        }
        camera.registerTorchCallback(callback, handler)
        // Filet : si aucun rappel ne vient, on rend la main.
        handler.postDelayed({
            if (!callback.done) {
                callback.done = true
                camera.unregisterTorchCallback(callback)
                pending.finish()
            }
        }, 2_000)
    }

    private fun torchToggle(context: Context): PendingIntent = PendingIntent.getBroadcast(
        context,
        40,
        Intent(context, ControlsWidget::class.java).setAction(ACTION_TORCH),
        PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
    )

    private companion object {
        const val ACTION_TORCH = "dev.levilainpetit.wux.action.TORCH"
        const val TORCH_STATE = "controls.torch_on"
    }
}
