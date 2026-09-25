package dev.levilainpetit.wux.wallpaper

/** Ce que l'appli fournit à une scène, réduit à ce dont les scènes ont besoin. */
data class CircuitPalette(val core: Int, val line: Int, val glow: Int)

object WallpaperSettings {
    const val DISCREET = 0.45f
    const val NORMAL = 0.7f
    const val VIVID = 1f
}

object CircuitPainter {
    val BACKGROUND = android.graphics.Color.rgb(5, 7, 13)
}
