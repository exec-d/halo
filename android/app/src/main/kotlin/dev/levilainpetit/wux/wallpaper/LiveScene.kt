package dev.levilainpetit.wux.wallpaper

import android.graphics.Canvas
import android.graphics.Color

/** Ce qu'une scène reçoit pour dessiner une image. */
class LiveFrame {
    /** Inclinaison, de -1 à 1, par rapport à la position de repos. */
    var tiltX = 0f
    var tiltY = 0f

    /** Direction regardée à travers le téléphone (degrés) : azimut depuis le nord, hauteur. */
    var azimuth = 180f
    var altitude = 30f

    /** Rotation du téléphone autour de l'axe de visée (degrés). */
    var roll = 0f
    var hasOrientation = false

    var timeMillis = 0L
    var intensity = WallpaperSettings.DISCREET
    var locked = false
    var palette = CircuitPalette(Color.WHITE, Color.WHITE, Color.WHITE)
}

/** Une scène de fond d'écran : elle dessine, et dit si elle s'anime encore. */
interface LiveScene {
    /** Vrai si la scène suit l'orientation dans l'espace (boussole) plutôt que l'inclinaison. */
    val usesOrientation: Boolean get() = false

    fun resize(width: Int, height: Int, density: Float)

    /**
     * Les couleurs propres de la scène (principale, secondaire, fond), que
     * le système reprend pour ses thèmes ; `null` : celles du téléphone.
     */
    val colors: IntArray? get() = null

    /** Données lentes (météo, position) relues quand le fond redevient visible. */
    fun refresh() {}

    fun draw(canvas: Canvas, frame: LiveFrame)

    /** Délai avant l'image suivante (ms), ou `null` : immobile jusqu'au prochain mouvement. */
    fun nextFrameIn(frame: LiveFrame): Long?
}
