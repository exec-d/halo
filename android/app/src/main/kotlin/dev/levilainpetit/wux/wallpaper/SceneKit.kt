package dev.levilainpetit.wux.wallpaper

import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Shader

/** Petits outils communs aux scènes. */
internal object SceneKit {

    /** Force d'une scène selon l'intensité choisie : jamais éteinte. */
    fun strength(frame: LiveFrame) = 0.45f + 0.55f * frame.intensity

    /** [color] avec l'opacité [alpha] (0–255). */
    fun alpha(color: Int, alpha: Int): Int = (color and 0x00FFFFFF) or (alpha.coerceIn(0, 255) shl 24)

    /** Assombrit le haut de l'écran verrouillé, sous l'horloge et les notifications. */
    fun lockShade(height: Int) = Paint().apply {
        shader = LinearGradient(0f, 0f, 0f, height * 0.34f, Color.argb(170, 0, 0, 0), Color.TRANSPARENT, Shader.TileMode.CLAMP)
    }

    /** Secondes écoulées depuis l'image précédente, bornées (reprise après une pause). */
    fun step(last: Long, now: Long): Float =
        if (last == 0L) 0f else ((now - last) / 1000f).coerceIn(0f, 0.1f)
}
