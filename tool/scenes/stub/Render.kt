package dev.levilainpetit.wux.wallpaper

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import java.awt.RenderingHints
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO

/** Palettes Material You d'essai : cuivre, bleu, vert. */
private val palettes = mapOf(
    "copper" to CircuitPalette(Color.rgb(255, 233, 223), Color.rgb(233, 167, 142), Color.rgb(214, 128, 98)),
    "blue" to CircuitPalette(Color.rgb(214, 227, 255), Color.rgb(170, 199, 255), Color.rgb(90, 140, 230)),
    "green" to CircuitPalette(Color.rgb(200, 245, 210), Color.rgb(140, 214, 160), Color.rgb(70, 170, 110)),
)

/** Les fonds : Circuit, et ses variantes (convecteur temporel, réacteur arc). */
private val scenes = mapOf("circuit" to CircuitScene.Core.BATTERY, "flux" to CircuitScene.Core.FLUX, "arc" to CircuitScene.Core.ARC)

/** Les miniatures que `--thumbs` refait (celle de Circuit est faite à part). */
private val thumbs = listOf("flux", "arc")

/**
 * `Render <dossier> [fonds]` : chaque fond, dans chaque palette, à trois
 * instants, plus en « Discret » et en charge (palette cuivre).
 * `Render --thumbs <dossier res/drawable-nodpi>` : les miniatures du
 * sélecteur de fonds d'Android.
 */
fun main(args: Array<String>) {
    if (args.firstOrNull() == "--thumbs") {
        val out = File(args[1])
        for (name in thumbs) {
            val image = render(scenes.getValue(name), palettes.getValue("copper"), 720, 1600, 2f, 830L)
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
    if (args.firstOrNull() == "--frames") {
        // `--frames <dossier> <fond> <images> <pas ms> [charge]` : une séquence,
        // l'allumage de l'écran au début (1,3 s), pour faire une animation.
        val out = File(args[1]).apply { mkdirs() }
        val core = scenes.getValue(args[2])
        val count = args[3].toInt()
        val step = args[4].toLong()
        val charging = args.getOrNull(5) == "charge"
        val scene = CircuitScene.build(540, 1200, 1.3125f, core)
        val painter = CircuitPainter(scene)
        val state = FrameState().apply {
            intensity = WallpaperSettings.NORMAL
            batteryLevel = 0.68f
            this.charging = charging
        }
        for (i in 0 until count) {
            val t = i * step
            state.timeMillis = 10_000L + t
            state.ignition = (t / 1300f).coerceAtMost(1f)
            state.tiltX = kotlin.math.sin(t / 1400.0).toFloat() * 0.3f
            val bitmap = Bitmap.createBitmap(540, 1200, Bitmap.Config.ARGB_8888)
            painter.draw(Canvas(bitmap), state, palettes.getValue("copper"))
            ImageIO.write(bitmap.image, "png", File(out, "frame_%03d.png".format(i)))
        }
        return
    }
    val out = File(args[0]).apply { mkdirs() }
    val only = args.getOrNull(1)?.takeIf { it.isNotEmpty() }?.split(',')
    for ((name, core) in scenes) {
        if (only != null && name !in only) continue
        for ((pn, palette) in palettes) {
            for (time in listOf(300L, 600L, 830L)) {
                ImageIO.write(render(core, palette, 540, 1200, 1.3125f, time), "png", File(out, "${name}_${pn}_$time.png"))
            }
        }
        val copper = palettes.getValue("copper")
        ImageIO.write(render(core, copper, 540, 1200, 1.3125f, 830L, WallpaperSettings.DISCREET), "png", File(out, "${name}_copper_discreet.png"))
        ImageIO.write(render(core, copper, 540, 1200, 1.3125f, 600L, charging = true), "png", File(out, "${name}_copper_charging.png"))
    }
}

/** Une image du fond à l'instant [time] (ms), tout allumé, avec quelques impulsions en route. */
private fun render(
    core: CircuitScene.Core,
    palette: CircuitPalette,
    w: Int,
    h: Int,
    density: Float,
    time: Long,
    level: Float = WallpaperSettings.NORMAL,
    charging: Boolean = false,
): BufferedImage {
    val scene = CircuitScene.build(w, h, density, core)
    val state = FrameState().apply {
        intensity = level
        timeMillis = time
        batteryLevel = 0.68f
        this.charging = charging
        tiltX = 0.15f
    }
    scene.networkRoutes.forEachIndexed { i, route -> state.pulses += Pulse(route, 0f, 0.3f + i * 0.2f) }
    scene.dataRoutes.firstOrNull()?.let { state.pulses += Pulse(it, 0f, 0.55f) }
    val bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
    CircuitPainter(scene).draw(Canvas(bitmap), state, palette)
    return bitmap.image
}
