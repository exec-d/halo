package dev.levilainpetit.wux.theme

import android.content.Context
import android.graphics.Color
import android.provider.Settings
import dev.levilainpetit.wux.R
import dev.levilainpetit.wux.wallpaper.CircuitPalette
import dev.levilainpetit.wux.wallpaper.WallpaperPreview
import org.json.JSONObject

/**
 * Un thème Halo : un fond d'écran animé, la palette dans laquelle il se
 * dessine (et qu'il annonce à Android, d'où les couleurs du système et des
 * widgets), et trois sons originaux. Son identifiant est celui de son fond
 * (`circuit`, `flux`, `arc`, `quantum`…), partagé avec Dart (`themes_screen.dart`).
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
        HaloTheme(
            WallpaperPreview.QUANTUM,
            CircuitPalette(core = Color.rgb(241, 235, 255), line = Color.rgb(183, 156, 255), glow = Color.rgb(124, 77, 255)),
            mapOf(
                ThemeSounds.Kind.RING to (R.raw.theme_quantum_ring to R.string.theme_sound_quantum_ring),
                ThemeSounds.Kind.NOTIFICATION to (R.raw.theme_quantum_notification to R.string.theme_sound_quantum_notification),
                ThemeSounds.Kind.ALARM to (R.raw.theme_quantum_alarm to R.string.theme_sound_quantum_alarm),
            ),
        ),
        HaloTheme(
            WallpaperPreview.NEURAL,
            CircuitPalette(core = Color.rgb(255, 234, 248), line = Color.rgb(255, 138, 216), glow = Color.rgb(224, 64, 251)),
            mapOf(
                ThemeSounds.Kind.RING to (R.raw.theme_neural_ring to R.string.theme_sound_neural_ring),
                ThemeSounds.Kind.NOTIFICATION to (R.raw.theme_neural_notification to R.string.theme_sound_neural_notification),
                ThemeSounds.Kind.ALARM to (R.raw.theme_neural_alarm to R.string.theme_sound_neural_alarm),
            ),
        ),
        HaloTheme(
            WallpaperPreview.ATOM,
            CircuitPalette(core = Color.rgb(255, 251, 224), line = Color.rgb(255, 228, 92), glow = Color.rgb(255, 196, 0)),
            mapOf(
                ThemeSounds.Kind.RING to (R.raw.theme_atom_ring to R.string.theme_sound_atom_ring),
                ThemeSounds.Kind.NOTIFICATION to (R.raw.theme_atom_notification to R.string.theme_sound_atom_notification),
                ThemeSounds.Kind.ALARM to (R.raw.theme_atom_alarm to R.string.theme_sound_atom_alarm),
            ),
        ),
        HaloTheme(
            WallpaperPreview.VAULT,
            CircuitPalette(core = Color.rgb(238, 255, 224), line = Color.rgb(166, 255, 99), glow = Color.rgb(67, 209, 46)),
            mapOf(
                ThemeSounds.Kind.RING to (R.raw.theme_vault_ring to R.string.theme_sound_vault_ring),
                ThemeSounds.Kind.NOTIFICATION to (R.raw.theme_vault_notification to R.string.theme_sound_vault_notification),
                ThemeSounds.Kind.ALARM to (R.raw.theme_vault_alarm to R.string.theme_sound_vault_alarm),
            ),
        ),
        HaloTheme(
            WallpaperPreview.GHOST,
            CircuitPalette(core = Color.rgb(230, 255, 251), line = Color.rgb(92, 245, 218), glow = Color.rgb(0, 191, 165)),
            mapOf(
                ThemeSounds.Kind.RING to (R.raw.theme_ghost_ring to R.string.theme_sound_ghost_ring),
                ThemeSounds.Kind.NOTIFICATION to (R.raw.theme_ghost_notification to R.string.theme_sound_ghost_notification),
                ThemeSounds.Kind.ALARM to (R.raw.theme_ghost_alarm to R.string.theme_sound_ghost_alarm),
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

    /**
     * D'où le système tire ses couleurs (Fond d'écran et style › Couleurs) :
     * `home_wallpaper`, `lock_wallpaper`, `preset` (une couleur de base, qui
     * ignore le fond), ou `null` si Android ne le dit pas.
     */
    fun colorSource(context: Context): String? = runCatching {
        val json = Settings.Secure.getString(context.contentResolver, "theme_customization_overlay_packages")
            ?: return@runCatching null
        JSONObject(json).optString("android.theme.customization.color_source").takeIf { it.isNotEmpty() }
    }.getOrNull()

    private fun prefs(context: Context) = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
}
