package dev.levilainpetit.wux.sky

import android.graphics.Bitmap
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * La Lune dessinée pixel par pixel, telle qu'on la voit : une sphère éclairée
 * de côté par le Soleil selon l'angle de phase, ses mers sombres et ses
 * cratères clairs à leur place, la lumière cendrée sur la partie dans
 * l'ombre. En blanc plus ou moins opaque, teinté par la mise en page.
 */
object MoonPainter {

    /** Une mer : centre (x vers l'est du ciel à droite, y vers le nord), rayons, assombrissement. */
    private class Patch(val x: Double, val y: Double, val rx: Double, val ry: Double, val depth: Double)

    /** Les grandes mers de la face visible, en coordonnées du disque (rayon 1, nord en haut). */
    private val MARIA = listOf(
        Patch(-0.30, 0.44, 0.30, 0.25, 0.34), // Pluies (Imbrium)
        Patch(0.20, 0.38, 0.17, 0.16, 0.36), // Sérénité
        Patch(0.36, 0.10, 0.22, 0.18, 0.36), // Tranquillité
        Patch(0.72, 0.29, 0.11, 0.10, 0.42), // Crises
        Patch(0.62, -0.12, 0.13, 0.20, 0.3), // Fécondité
        Patch(0.42, -0.30, 0.10, 0.10, 0.28), // Nectar
        Patch(-0.20, -0.36, 0.19, 0.15, 0.28), // Nuées
        Patch(-0.52, -0.42, 0.11, 0.10, 0.3), // Humeurs
        Patch(-0.62, 0.10, 0.28, 0.46, 0.3), // Océan des Tempêtes
        Patch(-0.36, 0.14, 0.18, 0.14, 0.22), // Îles
        Patch(0.02, 0.73, 0.44, 0.07, 0.24), // Froid
        Patch(0.03, 0.20, 0.10, 0.08, 0.24), // Vapeurs
    )

    /** Les cratères clairs les plus visibles : Tycho, Copernic, Kepler, Aristarque. */
    private val CRATERS = listOf(
        Patch(-0.13, -0.70, 0.05, 0.05, -0.16),
        Patch(-0.33, 0.16, 0.05, 0.05, -0.2),
        Patch(-0.60, 0.13, 0.035, 0.035, -0.14),
        Patch(-0.74, 0.40, 0.03, 0.03, -0.25),
    )

    /**
     * La Lune à [angle] (degrés de phase) sur [size] pixels de côté. Dans
     * l'hémisphère sud ([southern]), elle est vue tête en bas : la partie
     * éclairée d'une lune croissante est à gauche. [outline] cerne le disque
     * (pour les petites icônes).
     */
    fun draw(angle: Double, size: Int, southern: Boolean = false, outline: Boolean = false): Bitmap {
        val n = size.coerceAtLeast(2)
        val pixels = IntArray(n * n)
        val rad = angle * Math.PI / 180
        // D'où vient la lumière : vers l'observateur à la pleine lune, derrière à la nouvelle,
        // à droite en croissant (hémisphère nord).
        val sx = sin(rad)
        val sz = -cos(rad)
        val radius = n / 2.0 - 0.5
        val edge = 1.2 / radius
        for (py in 0 until n) {
            for (px in 0 until n) {
                var x = (px + 0.5 - n / 2.0) / radius
                var y = -(py + 0.5 - n / 2.0) / radius
                val rr = x * x + y * y
                if (rr > 1 + edge) continue
                val z = sqrt((1 - rr).coerceAtLeast(0.0))
                // Le bord du disque, adouci sur un pixel.
                val rim = ((1 + edge - sqrt(rr)) / (2 * edge)).coerceIn(0.0, 1.0)
                val light = sx * x + sz * z
                // Terminateur doux, et un éclairage presque uniforme (la Lune n'est pas une boule mate).
                val lit = smooth(-0.04, 0.10, light) * (0.82 + 0.18 * smooth(0.0, 0.6, light))
                if (southern) {
                    x = -x
                    y = -y
                }
                val albedo = albedo(x, y) * (0.88 + 0.12 * z)
                val earthshine = 0.13
                var value = (albedo * (lit + earthshine * (1 - lit))).coerceIn(0.0, 1.0)
                // En petit, un liseré pour que la nouvelle lune reste un disque.
                if (outline) value = maxOf(value, 0.45 * smooth(1 - 3 * edge, 1 - edge, sqrt(rr)))
                value *= rim
                pixels[py * n + px] = ((value * 255).toInt() shl 24) or 0xFFFFFF
            }
        }
        val bitmap = Bitmap.createBitmap(n, n, Bitmap.Config.ARGB_8888)
        bitmap.setPixels(pixels, 0, n, 0, 0, n, n)
        return bitmap
    }

    /** La clarté du sol en (x, y) : les terres hautes claires, les mers sombres, quelques cratères brillants. */
    private fun albedo(x: Double, y: Double): Double {
        // Les bords des mers ne sont pas des cercles : un bruit les déforme.
        val wobble = noise(x * 4 + 11, y * 4 + 3) - 0.5
        var a = 0.9
        for (m in MARIA) a -= m.depth * blob(x + wobble * 0.12, y - wobble * 0.08, m)
        // Le relief : des taches fines, à plusieurs échelles, toujours les mêmes.
        a += 0.10 * (noise(x * 9, y * 9) - 0.5) + 0.06 * (noise(x * 23 + 5, y * 23 + 7) - 0.5)
        for (c in CRATERS) a -= c.depth * blob(x, y, c)
        return a.coerceIn(0.3, 1.0)
    }

    private fun blob(x: Double, y: Double, p: Patch): Double {
        val dx = (x - p.x) / p.rx
        val dy = (y - p.y) / p.ry
        return smooth(1.3, 0.5, sqrt(dx * dx + dy * dy))
    }

    /** Un bruit de valeur lissé, entre 0 et 1. */
    private fun noise(x: Double, y: Double): Double {
        val ix = floor(x).toInt()
        val iy = floor(y).toInt()
        val fx = x - ix
        val fy = y - iy
        val u = fx * fx * (3 - 2 * fx)
        val v = fy * fy * (3 - 2 * fy)
        val a = hash(ix, iy) + (hash(ix + 1, iy) - hash(ix, iy)) * u
        val b = hash(ix, iy + 1) + (hash(ix + 1, iy + 1) - hash(ix, iy + 1)) * u
        return a + (b - a) * v
    }

    private fun hash(x: Int, y: Int): Double {
        var h = x * 374_761_393 + y * 668_265_263
        h = (h xor (h ushr 13)) * 1_274_126_177
        return ((h xor (h ushr 16)) and 0xFFFF) / 65_535.0
    }

    private fun smooth(from: Double, to: Double, v: Double): Double {
        val t = ((v - from) / (to - from)).coerceIn(0.0, 1.0)
        return t * t * (3 - 2 * t)
    }
}
