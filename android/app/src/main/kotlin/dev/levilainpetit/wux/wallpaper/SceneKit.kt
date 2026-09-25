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

    /** [color] tourné de [degrees] sur le cercle des teintes. */
    fun hue(color: Int, degrees: Float, saturation: Float = 1f, value: Float = 1f): Int {
        val hsv = FloatArray(3)
        Color.colorToHSV(color, hsv)
        hsv[0] = ((hsv[0] + degrees) % 360f + 360f) % 360f
        hsv[1] = (hsv[1] * saturation).coerceIn(0f, 1f)
        hsv[2] = (hsv[2] * value).coerceIn(0f, 1f)
        return Color.HSVToColor(hsv)
    }

    /** La couleur d'accent, plus saturée : les néons des scènes. */
    fun neon(palette: CircuitPalette): Int = hue(palette.line, 0f, 1.35f, 1.05f)

    /** Une seconde couleur, à l'opposé de l'accent sur le cercle des teintes. */
    fun second(palette: CircuitPalette): Int = hue(palette.line, 160f, 1.4f, 1.05f)

    /** Une troisième, entre les deux. */
    fun third(palette: CircuitPalette): Int = hue(palette.line, -70f, 1.3f, 1.05f)

    /** Un fond très sombre, teinté par l'accent. */
    fun night(palette: CircuitPalette, value: Float = 0.06f): Int {
        val hsv = FloatArray(3)
        Color.colorToHSV(palette.line, hsv)
        hsv[1] = (hsv[1] * 0.7f).coerceIn(0f, 1f)
        hsv[2] = value
        return Color.HSVToColor(hsv)
    }

    /** Entre [a] (0) et [b] (1). */
    fun mix(a: Int, b: Int, t: Float): Int = Color.argb(
        (Color.alpha(a) + (Color.alpha(b) - Color.alpha(a)) * t).toInt(),
        (Color.red(a) + (Color.red(b) - Color.red(a)) * t).toInt(),
        (Color.green(a) + (Color.green(b) - Color.green(a)) * t).toInt(),
        (Color.blue(a) + (Color.blue(b) - Color.blue(a)) * t).toInt(),
    )

    /** Secondes écoulées depuis l'image précédente, bornées (reprise après une pause). */
    fun step(last: Long, now: Long): Float =
        if (last == 0L) 0f else ((now - last) / 1000f).coerceIn(0f, 0.1f)
}
