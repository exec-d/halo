package dev.levilainpetit.wux.theme

import android.content.Context
import android.graphics.Color
import dev.levilainpetit.wux.R
import dev.levilainpetit.wux.wallpaper.CircuitPalette
import dev.levilainpetit.wux.wallpaper.WallpaperPreview

/**
 * Un thème Halo : un fond d'écran animé, la palette dans laquelle il se
 * dessine (et qu'il annonce à Android, d'où les couleurs du système et des
 * widgets), et trois sons originaux. Son identifiant est celui de son fond
 * (`circuit`, `flux`, `arc`), partagé avec Dart (`themes_screen.dart`).
 */
class HaloTheme(
    val id: String,
    val palette: CircuitPalette,
    /** Les sons (res/raw, tool/theme_sounds.py) et leur nom dans les réglages d'Android. */
    val sounds: Map<ThemeSounds.Kind, Pair<Int, Int>>,
)

object HaloThemes {

    val ALL = listOf(
        HaloTheme(
            WallpaperPreview.CIRCUIT,
            CircuitPalette(core = Color.rgb(230, 246, 255), line = Color.rgb(138, 210, 255), glow = Color.rgb(47, 155, 255)),
            mapOf(
                ThemeSounds.Kind.RING to (R.raw.theme_circuit_ring to R.string.theme_sound_circuit_ring),
                ThemeSounds.Kind.NOTIFICATION to (R.raw.theme_circuit_notification to R.string.theme_sound_circuit_notification),
                ThemeSounds.Kind.ALARM to (R.raw.theme_circuit_alarm to R.string.theme_sound_circuit_alarm),
            ),
        ),
        HaloTheme(
            WallpaperPreview.FLUX,
            CircuitPalette(core = Color.rgb(255, 241, 218), line = Color.rgb(255, 179, 71), glow = Color.rgb(255, 106, 43)),
            mapOf(
                ThemeSounds.Kind.RING to (R.raw.theme_flux_ring to R.string.theme_sound_flux_ring),
                ThemeSounds.Kind.NOTIFICATION to (R.raw.theme_flux_notification to R.string.theme_sound_flux_notification),
                ThemeSounds.Kind.ALARM to (R.raw.theme_flux_alarm to R.string.theme_sound_flux_alarm),
            ),
        ),
        HaloTheme(
            WallpaperPreview.ARC,
            CircuitPalette(core = Color.rgb(255, 244, 224), line = Color.rgb(242, 193, 78), glow = Color.rgb(232, 67, 58)),
            mapOf(
                ThemeSounds.Kind.RING to (R.raw.theme_arc_ring to R.string.theme_sound_arc_ring),
                ThemeSounds.Kind.NOTIFICATION to (R.raw.theme_arc_notification to R.string.theme_sound_arc_notification),
                ThemeSounds.Kind.ALARM to (R.raw.theme_arc_alarm to R.string.theme_sound_arc_alarm),
            ),
        ),
    )

    private const val PREFS = "halo_theme"
    private const val ACTIVE = "active"

    fun byId(id: String?): HaloTheme? = ALL.firstOrNull { it.id == id }

    /** Le thème choisi dans Halo, même si son fond n'est pas (encore) appliqué. */
    fun chosen(context: Context): HaloTheme? = byId(prefs(context).getString(ACTIVE, null))

    fun choose(context: Context, id: String) {
        prefs(context).edit().putString(ACTIVE, id).apply()
    }

    /** Le thème en place : choisi, et son fond est celui du téléphone. */
    fun active(context: Context): HaloTheme? =
        chosen(context)?.takeIf { WallpaperPreview.isActive(context, it.id) }

    /**
     * La palette du fond [kind] : celle du thème choisi s'il porte sur ce fond,
     * sinon celle du téléphone (Material You).
     */
    fun palette(context: Context, kind: String): CircuitPalette =
        chosen(context)?.takeIf { it.id == kind }?.palette ?: CircuitPalette.of(context)

    private fun prefs(context: Context) = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
}
