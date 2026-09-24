package dev.levilainpetit.wux.wallpaper

import android.content.Context

/** Réglages du fond d'écran, écrits par l'application et relus par le fond. */
object WallpaperSettings {
    const val DISCREET = 0.45f
    const val NORMAL = 0.7f
    const val VIVID = 1f

    private const val PREFS = "halo_wallpaper"
    private const val INTENSITY = "intensity"

    /** « discreet », « normal » ou « vivid » : l'identifiant partagé avec Dart. */
    fun intensityName(context: Context): String =
        prefs(context).getString(INTENSITY, null) ?: "discreet"

    fun setIntensityName(context: Context, name: String) {
        prefs(context).edit().putString(INTENSITY, name).apply()
    }

    fun intensity(context: Context): Float = when (intensityName(context)) {
        "vivid" -> VIVID
        "normal" -> NORMAL
        else -> DISCREET
    }

    private fun prefs(context: Context) = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
}
