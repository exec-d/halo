package dev.levilainpetit.wux.sky

import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.asin
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/** Un astre suivi : la Lune et les cinq planètes visibles à l'œil nu. */
enum class Body { MOON, VENUS, JUPITER, MARS, SATURN, MERCURY }

/** Hauteur et azimut (degrés ; azimut 0 au nord, 90 à l'est). */
data class Horizontal(val altitude: Double, val azimuth: Double)

/**
 * Ce qu'un astre fait pendant la nuit observée : quand il est visible (plus
 * de quelques degrés au-dessus de l'horizon, ciel noir), sa position au
 * début de sa visibilité et sa hauteur maximale.
 */
data class Sighting(
    val body: Body,
    val from: ZonedDateTime,
    val until: ZonedDateTime,
    val start: Horizontal,
    val highest: Double,
    /** Déjà visible au début de la nuit observée (sinon, il se lève pendant). */
    val atStart: Boolean,
    /** Encore visible à la fin de la nuit observée. */
    val atEnd: Boolean,
)

/** La nuit observée : de la tombée de la nuit (ou maintenant) à 1 h, ou jusqu'à l'aube. */
data class Night(val from: ZonedDateTime, val until: ZonedDateTime, val sunset: ZonedDateTime?, val sightings: List<Sighting>)

/**
 * Positions du Soleil, de la Lune et des planètes, à la minute d'arc près,
 * d'après les éléments orbitaux de Paul Schlyter (« How to compute planetary
 * positions ») : sans perturbations, largement assez pour dire où regarder.
 */
object Sky {

    private const val RAD = PI / 180
    private const val STEP_MINUTES = 10L

    /** La nuit à observer à partir de [now], vue de [latitude], [longitude]. */
    fun night(now: ZonedDateTime, latitude: Double, longitude: Double): Night {
        val zone = now.zone
        fun sunAlt(t: ZonedDateTime) = horizontal(null, t.toInstant(), latitude, longitude).altitude
        // Le début : maintenant s'il fait déjà nuit, sinon la tombée de la nuit.
        var from = now.withSecond(0).withNano(0)
        var guard = 0
        while (sunAlt(from) > DUSK && guard++ < 24 * 6) from = from.plusMinutes(STEP_MINUTES)
        // La fin : 1 h du matin pour une soirée, l'aube pour une fin de nuit.
        val oneAm = from.toLocalDate().let { if (from.hour < 5) it else it.plusDays(1) }.atTime(1, 0).atZone(zone)
        var until = from
        guard = 0
        while (sunAlt(until) <= DUSK && guard++ < 16 * 6) until = until.plusMinutes(STEP_MINUTES)
        if (from.hour >= 5 && until.isAfter(oneAm)) until = oneAm
        if (!until.isAfter(from)) until = from.plusHours(1)
        // Le coucher du soleil qui précède (pour l'en-tête).
        var sunset: ZonedDateTime? = null
        var t = from
        guard = 0
        while (guard++ < 12 * 6) {
            val before = t.minusMinutes(STEP_MINUTES)
            if (sunAlt(before) > HORIZON_SUN && sunAlt(t) <= HORIZON_SUN) {
                sunset = t
                break
            }
            t = before
        }
        val sightings = Body.entries.mapNotNull { sighting(it, from, until, latitude, longitude) }
        return Night(from, until, sunset, sightings)
    }

    private fun sighting(body: Body, from: ZonedDateTime, until: ZonedDateTime, latitude: Double, longitude: Double): Sighting? {
        val minimum = if (body == Body.MERCURY || body == Body.VENUS) 4.0 else 8.0
        var first: ZonedDateTime? = null
        var last: ZonedDateTime? = null
        var start: Horizontal? = null
        var highest = -90.0
        var t = from
        while (!t.isAfter(until)) {
            val h = horizontal(body, t.toInstant(), latitude, longitude)
            if (h.altitude >= minimum) {
                if (first == null) {
                    first = t
                    start = h
                }
                last = t
                highest = maxOf(highest, h.altitude)
            }
            t = t.plusMinutes(STEP_MINUTES)
        }
        if (first == null || last == null || start == null) return null
        // Moins de vingt minutes de visibilité : pas la peine d'en parler.
        if (Duration.between(first, last).toMinutes() < 20) return null
        return Sighting(
            body = body,
            from = first,
            until = last,
            start = start,
            highest = highest,
            atStart = !first.isAfter(from),
            atEnd = Duration.between(last, until).toMinutes() < STEP_MINUTES,
        )
    }

    /** Position de [body] (le Soleil si `null`) à [at], vue de [latitude], [longitude]. */
    fun horizontal(body: Body?, at: Instant, latitude: Double, longitude: Double): Horizontal {
        val d = days(at)
        val (ra, dec) = equatorial(body, d)
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
    private fun equatorial(body: Body?, d: Double): Pair<Double, Double> {
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
        val (xg, yg, zg) = when (body) {
            null -> Triple(xs, ys, 0.0)
            else -> {
                val el = elements(body, d)
                val (xh, yh, zh) = orbit(el)
                // La Lune est déjà géocentrique ; les planètes, héliocentriques.
                if (body == Body.MOON) Triple(xh, yh, zh) else Triple(xh + xs, yh + ys, zh)
            }
        }
        val xe = xg
        val ye = yg * cos(ecl) - zg * sin(ecl)
        val ze = yg * sin(ecl) + zg * cos(ecl)
        return Pair(normalize(atan2(ye, xe) / RAD), atan2(ze, sqrt(xe * xe + ye * ye)) / RAD)
    }

    /** N, i, w (degrés), a, e, M (degrés). */
    private class Elements(val n: Double, val i: Double, val w: Double, val a: Double, val e: Double, val m: Double)

    private fun elements(body: Body, d: Double): Elements = when (body) {
        Body.MOON -> Elements(125.1228 - 0.0529538083 * d, 5.1454, 318.0634 + 0.1643573223 * d, 60.2666, 0.054900, 115.3654 + 13.0649929509 * d)
        Body.MERCURY -> Elements(48.3313 + 3.24587e-5 * d, 7.0047 + 5.00e-8 * d, 29.1241 + 1.01444e-5 * d, 0.387098, 0.205635 + 5.59e-10 * d, 168.6562 + 4.0923344368 * d)
        Body.VENUS -> Elements(76.6799 + 2.46590e-5 * d, 3.3946 + 2.75e-8 * d, 54.8910 + 1.38374e-5 * d, 0.723330, 0.006773 - 1.302e-9 * d, 48.0052 + 1.6021302244 * d)
        Body.MARS -> Elements(49.5574 + 2.11081e-5 * d, 1.8497 - 1.78e-8 * d, 286.5016 + 2.92961e-5 * d, 1.523688, 0.093405 + 2.516e-9 * d, 18.6021 + 0.5240207766 * d)
        Body.JUPITER -> Elements(100.4542 + 2.76854e-5 * d, 1.3030 - 1.557e-7 * d, 273.8777 + 1.64505e-5 * d, 5.20256, 0.048498 + 4.469e-9 * d, 19.8950 + 0.0830853001 * d)
        Body.SATURN -> Elements(113.6634 + 2.38980e-5 * d, 2.4886 - 1.081e-7 * d, 339.3939 + 2.97661e-5 * d, 9.55475, 0.055546 - 9.499e-9 * d, 316.9670 + 0.0334442282 * d)
    }

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

    /** Le Soleil à cette hauteur (degrés) : la nuit est assez noire pour les planètes. */
    private const val DUSK = -5.0

    /** Le bord du Soleil touche l'horizon (réfraction comprise). */
    private const val HORIZON_SUN = -0.833

    /** 2000-01-00 (31 décembre 1999) à 0 h TU, en secondes Unix. */
    private const val EPOCH = 946_598_400L

    /** Pour les tests et les aperçus : la même nuit dans [zone]. */
    fun night(at: Instant, zone: ZoneId, latitude: Double, longitude: Double) = night(at.atZone(zone), latitude, longitude)
}
