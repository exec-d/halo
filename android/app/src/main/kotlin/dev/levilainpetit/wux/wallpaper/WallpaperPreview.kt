package dev.levilainpetit.wux.wallpaper

import android.app.WallpaperManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.BatteryManager
import java.io.ByteArrayOutputStream

/** Aperçu du fond d'écran pour l'application, et son application. */
object WallpaperPreview {

    /**
     * Une image fixe du fond d'écran, à [widthPx] × [heightPx], en PNG : tout
     * allumé, au vrai niveau de batterie, avec quelques impulsions en route.
     */
    fun render(context: Context, widthPx: Int, heightPx: Int, kind: String = CIRCUIT): ByteArray {
        val intensity = WallpaperSettings.intensity(context)
        val metrics = context.resources.displayMetrics
        // Même rendu qu'à l'écran, réduit : les traits gardent leur proportion.
        val density = metrics.density * widthPx / metrics.widthPixels.coerceAtLeast(1)
        val scene = CircuitScene.build(widthPx, heightPx, density)
        val state = FrameState().apply { this.intensity = intensity }
        context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))?.let {
            val level = it.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
            val scale = it.getIntExtra(BatteryManager.EXTRA_SCALE, 100)
            if (level >= 0 && scale > 0) state.batteryLevel = level / scale.toFloat()
            state.charging = it.getIntExtra(BatteryManager.EXTRA_PLUGGED, 0) != 0
        }
        scene.networkRoutes.forEachIndexed { i, route -> state.pulses += Pulse(route, 0f, 0.3f + i * 0.2f) }
        scene.dataRoutes.firstOrNull()?.let { state.pulses += Pulse(it, 0f, 0.55f) }
        val bitmap = Bitmap.createBitmap(widthPx, heightPx, Bitmap.Config.ARGB_8888)
        CircuitPainter(scene).draw(Canvas(bitmap), state, CircuitPalette.of(context))
        scene.recycle()
        return ByteArrayOutputStream().use { out ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            bitmap.recycle()
            out.toByteArray()
        }
    }

    fun isActive(context: Context, kind: String = CIRCUIT): Boolean =
        WallpaperManager.getInstance(context).wallpaperInfo?.component == component(context, kind)

    /** Le fond Halo appliqué (`circuit`), ou `null`. */
    fun activeKind(context: Context): String? = KINDS.firstOrNull { isActive(context, it) }

    /** L'écran système qui propose d'appliquer le fond (accueil, verrouillage ou les deux). */
    fun applyIntent(context: Context, kind: String = CIRCUIT): Intent =
        Intent(WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER)
            .putExtra(WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT, component(context, kind))

    // Un seul fond pour l'instant ; `kind` choisira parmi les suivants.
    @Suppress("UNUSED_PARAMETER")
    private fun component(context: Context, kind: String) =
        ComponentName(context, HaloWallpaperService::class.java)

    const val CIRCUIT = "circuit"
    private val KINDS = listOf(CIRCUIT)
}
