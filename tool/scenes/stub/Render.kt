package dev.levilainpetit.wux.wallpaper

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import java.awt.RenderingHints
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO

/** Palettes Material You d'essai : ambre, bleu, vert. */
private val palettes = mapOf(
    "amber" to CircuitPalette(Color.rgb(255, 236, 205), Color.rgb(242, 193, 120), Color.rgb(217, 161, 91)),
    "blue" to CircuitPalette(Color.rgb(214, 227, 255), Color.rgb(170, 199, 255), Color.rgb(90, 140, 230)),
    "green" to CircuitPalette(Color.rgb(200, 245, 210), Color.rgb(140, 214, 160), Color.rgb(70, 170, 110)),
)

private val scenes = mapOf<String, () -> LiveScene>(
    "grid" to { GridScene() },
    "megacity" to { MegacityScene() },
    "code" to { CodeScene() },
    "neon" to { NeonScene() },
    "sentinel" to { SentinelScene() },
)

/**
 * `Render <dossier> [scènes]` : chaque scène, dans chaque palette, après 3 s
 * et 6,5 s d'animation. `Render --thumbs <dossier res/drawable-nodpi>` : les
 * miniatures du sélecteur de fonds d'Android, en palette ambre.
 */
fun main(args: Array<String>) {
    if (args.firstOrNull() == "--thumbs") {
        val out = File(args[1])
        for ((name, make) in scenes) {
            val image = animate(make(), palettes.getValue("amber"), 720, 1600, 2f, listOf(6500)).single()
            // Le sélecteur montre du 9:16 : on recadre au milieu, puis on réduit.
            val crop = image.getSubimage(0, (1600 - 1280) / 2, 720, 1280)
            val small = BufferedImage(360, 640, BufferedImage.TYPE_INT_RGB)
            small.createGraphics().apply {
                setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC)
                drawImage(crop, 0, 0, 360, 640, null)
                dispose()
            }
            ImageIO.write(small, "png", File(out, "wallpaper_thumb_$name.png"))
        }
        return
    }
    val out = File(args[0]).apply { mkdirs() }
    val only = args.getOrNull(1)?.split(',')
    for ((name, make) in scenes) {
        if (only != null && name !in only) continue
        for ((pn, palette) in palettes) {
            val shots = listOf(3000, 6500)
            animate(make(), palette, 540, 1200, 1.3125f, shots).forEachIndexed { i, image ->
                ImageIO.write(image, "png", File(out, "${name}_${pn}_${shots[i]}.png"))
            }
        }
    }
}

/** Fait tourner [scene] image par image et garde celles des instants [shots] (ms). */
private fun animate(scene: LiveScene, palette: CircuitPalette, w: Int, h: Int, density: Float, shots: List<Int>): List<BufferedImage> {
    scene.resize(w, h, density)
    scene.refresh()
    val frame = LiveFrame().apply {
        this.palette = palette
        intensity = WallpaperSettings.NORMAL
        timeMillis = 1_700_000_000_000L
    }
    val scratch = Canvas(Bitmap.createBitmap(4, 4, Bitmap.Config.ARGB_8888))
    val images = mutableListOf<BufferedImage>()
    var t = 0
    for (shot in shots) {
        while (t < shot) {
            frame.timeMillis += 40
            t += 40
            frame.tiltX = kotlin.math.sin(t / 1400.0).toFloat() * 0.4f
            scene.draw(scratch, frame)
        }
        val bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        scene.draw(Canvas(bitmap), frame)
        images += bitmap.image
    }
    return images
}
