package dev.levilainpetit.wux.sky

import java.time.Instant
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.asin
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/** Hauteur et azimut (degrés ; azimut 0 au nord, 90 à l'est). */
data class Horizontal(val altitude: Double, val azimuth: Double)

/**
 * Positions du Soleil et de la Lune, d'après les éléments orbitaux de Paul
 * Schlyter (« How to compute planetary positions ») et les principales
 * perturbations de la Lune : à quelques minutes d'arc près.
 */
object Sky {

    private const val RAD = PI / 180
    /** Position de la Lune (du Soleil si [moon] est faux) à [at], vue de [latitude], [longitude]. */
    fun horizontal(moon: Boolean, at: Instant, latitude: Double, longitude: Double): Horizontal {
        val d = days(at)
        val (ra, dec) = equatorial(moon, d)
        val sun = sunElements(d)
        val gmst0 = normalize(sun.second + sun.first + 180.0)
        val ut = (at.epochSecond % 86_400L).toDouble() / 3600.0
        val lst = gmst0 + ut * 15.0 + longitude
        val ha = (lst - ra) * RAD
        val lat = latitude * RAD
        val decR = dec * RAD
        val sinAlt = sin(lat) * sin(decR) + cos(lat) * cos(decR) * cos(ha)
        val alt = asin(sinAlt.coerceIn(-1.0, 1.0))
        val az = atan2(sin(ha), cos(ha) * sin(lat) - sin(decR) / cos(decR) * cos(lat)) / RAD + 180.0
        return Horizontal(alt / RAD, normalize(az))
    }

    /** Jours depuis le 0 janvier 2000 à 0 h TU (l'époque de Schlyter). */
    private fun days(at: Instant) = (at.epochSecond - EPOCH) / 86_400.0

    /** Longueur du périhélie (w) et anomalie moyenne (M) du Soleil, en degrés. */
    private fun sunElements(d: Double) = Pair(282.9404 + 4.70935e-5 * d, normalize(356.0470 + 0.9856002585 * d))

    /** Ascension droite et déclinaison (degrés). */
    private fun equatorial(moon: Boolean, d: Double): Pair<Double, Double> {
        val ecl = (23.4393 - 3.563e-7 * d) * RAD
        // Le Soleil vu de la Terre.
        val (ws, ms) = sunElements(d)
        val es = 0.016709 - 1.151e-9 * d
        val sunE = kepler(ms, es)
        val sxv = cos(sunE) - es
        val syv = sqrt(1 - es * es) * sin(sunE)
        val sv = atan2(syv, sxv) / RAD
        val sr = sqrt(sxv * sxv + syv * syv)
        val sunLon = (sv + ws) * RAD
        val xs = sr * cos(sunLon)
        val ys = sr * sin(sunLon)
        // La Lune est géocentrique.
        val (xg, yg, zg) = if (moon) {
            moonEcliptic(d).let { (lon, lat, r) ->
                Triple(r * cos(lon * RAD) * cos(lat * RAD), r * sin(lon * RAD) * cos(lat * RAD), r * sin(lat * RAD))
            }
        } else {
            Triple(xs, ys, 0.0)
        }
        val xe = xg
        val ye = yg * cos(ecl) - zg * sin(ecl)
        val ze = yg * sin(ecl) + zg * cos(ecl)
        return Pair(normalize(atan2(ye, xe) / RAD), atan2(ze, sqrt(xe * xe + ye * ye)) / RAD)
    }

    /**
     * L'angle de phase de la Lune à [at] (degrés) : son écart en longitude au
     * Soleil, 0 à la nouvelle lune, 90 au premier quartier, 180 à la pleine lune.
     */
    fun moonAngle(at: Instant): Double {
        val d = days(at)
        return normalize(moonEcliptic(d).first - sunLongitude(d))
    }

    /** Longitude écliptique vraie du Soleil (degrés). */
    private fun sunLongitude(d: Double): Double {
        val (ws, ms) = sunElements(d)
        val es = 0.016709 - 1.151e-9 * d
        val e = kepler(ms, es)
        return normalize(atan2(sqrt(1 - es * es) * sin(e), cos(e) - es) / RAD + ws)
    }

    /**
     * Longitude, latitude écliptiques (degrés) et distance (rayons terrestres)
     * de la Lune, avec ses principales perturbations (évection, variation,
     * équation annuelle…) : à quelques minutes d'arc, les phases à quelques
     * minutes près.
     */
    private fun moonEcliptic(d: Double): Triple<Double, Double, Double> {
        val el = moonElements(d)
        val (x, y, z) = orbit(el)
        var lon = atan2(y, x) / RAD
        var lat = atan2(z, sqrt(x * x + y * y)) / RAD
        var r = sqrt(x * x + y * y + z * z)
        val (ws, ms) = sunElements(d)
        val mm = normalize(el.m)
        val lm = normalize(el.n + el.w + el.m)
        val dd = normalize(lm - (ms + ws))
        val f = normalize(lm - el.n)
        fun s(deg: Double) = sin(deg * RAD)
        fun c(deg: Double) = cos(deg * RAD)
        lon += -1.274 * s(mm - 2 * dd) + 0.658 * s(2 * dd) - 0.186 * s(ms) - 0.059 * s(2 * mm - 2 * dd) -
            0.057 * s(mm - 2 * dd + ms) + 0.053 * s(mm + 2 * dd) + 0.046 * s(2 * dd - ms) + 0.041 * s(mm - ms) -
            0.035 * s(dd) - 0.031 * s(mm + ms) - 0.015 * s(2 * f - 2 * dd) + 0.011 * s(mm - 4 * dd)
        lat += -0.173 * s(f - 2 * dd) - 0.055 * s(mm - f - 2 * dd) - 0.046 * s(mm + f - 2 * dd) +
            0.033 * s(f + 2 * dd) + 0.017 * s(2 * mm + f)
        r += -0.58 * c(mm - 2 * dd) - 0.46 * c(2 * dd)
        return Triple(normalize(lon), lat, r)
    }

    /** N, i, w (degrés), a, e, M (degrés). */
    private class Elements(val n: Double, val i: Double, val w: Double, val a: Double, val e: Double, val m: Double)

    private fun moonElements(d: Double) =
        Elements(125.1228 - 0.0529538083 * d, 5.1454, 318.0634 + 0.1643573223 * d, 60.2666, 0.054900, 115.3654 + 13.0649929509 * d)

    /** Position écliptique (x, y, z) sur l'orbite décrite par [el]. */
    private fun orbit(el: Elements): Triple<Double, Double, Double> {
        val e = kepler(normalize(el.m), el.e)
        val xv = el.a * (cos(e) - el.e)
        val yv = el.a * sqrt(1 - el.e * el.e) * sin(e)
        val v = atan2(yv, xv)
        val r = sqrt(xv * xv + yv * yv)
        val n = el.n * RAD
        val i = el.i * RAD
        val vw = v + el.w * RAD
        return Triple(
            r * (cos(n) * cos(vw) - sin(n) * sin(vw) * cos(i)),
            r * (sin(n) * cos(vw) + cos(n) * sin(vw) * cos(i)),
            r * (sin(vw) * sin(i)),
        )
    }

    /** L'anomalie excentrique (radians), de l'anomalie moyenne [m] (degrés). */
    private fun kepler(m: Double, e: Double): Double {
        val mr = m * RAD
        var ea = mr + e * sin(mr) * (1 + e * cos(mr))
        repeat(8) {
            val delta = (ea - e * sin(ea) - mr) / (1 - e * cos(ea))
            ea -= delta
            if (abs(delta) < 1e-9) return ea
        }
        return ea
    }

    private fun normalize(degrees: Double) = ((degrees % 360.0) + 360.0) % 360.0

    /** 2000-01-00 (31 décembre 1999) à 0 h TU, en secondes Unix. */
    private const val EPOCH = 946_598_400L

}
