package dev.levilainpetit.wux.sky

import java.time.Duration
import java.time.Instant
import java.time.ZonedDateTime
import kotlin.math.PI
import kotlin.math.cos

/** Les huit moments du cycle, dans l'ordre. */
enum class Phase { NEW, WAXING_CRESCENT, FIRST_QUARTER, WAXING_GIBBOUS, FULL, WANING_GIBBOUS, LAST_QUARTER, WANING_CRESCENT }

/**
 * La Lune à un instant : son angle de phase (0 nouvelle lune, 180 pleine
 * lune), la part éclairée, son âge depuis la nouvelle lune et le nom de la
 * phase.
 */
data class Lunation(val angle: Double, val fraction: Double, val age: Double, val phase: Phase) {
    val waxing get() = angle < 180
}

/**
 * La Lune ce jour-là : sa phase, les quatre phases principales à venir, et
 * sa prochaine sortie : levée maintenant ([up]) jusqu'à [set], ou de [rise]
 * à [set].
 */
data class MoonDay(
    val now: Lunation,
    val next: List<Pair<Phase, ZonedDateTime>>,
    val up: Boolean,
    val rise: ZonedDateTime?,
    val set: ZonedDateTime?,
)

object Moon {

    /** Durée moyenne d'une lunaison (jours). */
    const val SYNODIC = 29.530589

    /** Autour d'une phase principale (degrés, un peu moins d'un jour) : on l'appelle par son nom. */
    private const val PRINCIPAL = 6.0

    fun at(instant: Instant): Lunation {
        val angle = Sky.moonAngle(instant)
        val phase = when {
            angle < PRINCIPAL || angle > 360 - PRINCIPAL -> Phase.NEW
            angle < 90 - PRINCIPAL -> Phase.WAXING_CRESCENT
            angle <= 90 + PRINCIPAL -> Phase.FIRST_QUARTER
            angle < 180 - PRINCIPAL -> Phase.WAXING_GIBBOUS
            angle <= 180 + PRINCIPAL -> Phase.FULL
            angle < 270 - PRINCIPAL -> Phase.WANING_GIBBOUS
            angle <= 270 + PRINCIPAL -> Phase.LAST_QUARTER
            else -> Phase.WANING_CRESCENT
        }
        return Lunation(angle, (1 - cos(angle * PI / 180)) / 2, angle / 360 * SYNODIC, phase)
    }

    /**
     * Tout ce que le widget montre, à partir de [now]. Sans lieu, ni lever ni
     * coucher.
     */
    fun day(now: ZonedDateTime, latitude: Double?, longitude: Double?): MoonDay {
        val lunation = at(now.toInstant())
        if (latitude == null || longitude == null) return MoonDay(lunation, principals(now, 4), false, null, null)
        val up = Sky.horizontal(true, now.toInstant(), latitude, longitude).altitude >= HORIZON
        val rising = if (up) null else crossing(now, latitude, longitude, up = true)
        val setting = crossing(rising ?: now, latitude, longitude, up = false)
        return MoonDay(lunation, principals(now, 4), up, rising, setting)
    }

    /** Les [count] prochaines phases principales (nouvelle lune, quartiers, pleine lune), à la minute. */
    fun principals(from: ZonedDateTime, count: Int): List<Pair<Phase, ZonedDateTime>> {
        val found = mutableListOf<Pair<Phase, ZonedDateTime>>()
        var t = from
        var before = Sky.moonAngle(t.toInstant())
        var guard = 0
        while (found.size < count && guard++ < 4 * 40) {
            val next = t.plusHours(6)
            val after = Sky.moonAngle(next.toInstant())
            // Un quart de tour franchi entre t et next : on resserre à la minute.
            val quarter = (after / 90).toInt()
            if (quarter != (before / 90).toInt()) {
                val target = quarter * 90.0
                var lo = t
                var hi = next
                while (Duration.between(lo, hi).toMinutes() > 1) {
                    val mid = lo.plusSeconds(Duration.between(lo, hi).seconds / 2)
                    if (ahead(Sky.moonAngle(mid.toInstant()), target)) hi = mid else lo = mid
                }
                val phase = when (quarter % 4) {
                    0 -> Phase.NEW
                    1 -> Phase.FIRST_QUARTER
                    2 -> Phase.FULL
                    else -> Phase.LAST_QUARTER
                }
                found += phase to hi.withSecond(0).withNano(0)
            }
            t = next
            before = after
        }
        return found
    }

    /** L'angle [angle] a dépassé [target] (en tournant, 360 revenant à 0). */
    private fun ahead(angle: Double, target: Double) = ((angle - target + 540) % 360) - 180 >= 0

    /**
     * Le prochain lever (ou coucher) de la Lune dans les 26 heures, à la
     * minute : le moment où son bord franchit l'horizon (réfraction et
     * parallaxe comprises).
     */
    private fun crossing(now: ZonedDateTime, latitude: Double, longitude: Double, up: Boolean): ZonedDateTime? {
        fun alt(t: ZonedDateTime) = Sky.horizontal(true, t.toInstant(), latitude, longitude).altitude - HORIZON
        var t = now
        var a = alt(t)
        repeat(26 * 6) {
            val next = t.plusMinutes(10)
            val b = alt(next)
            if (if (up) a < 0 && b >= 0 else a >= 0 && b < 0) {
                // Interpolation linéaire entre les deux pas : assez à la minute.
                val minutes = (10 * a / (a - b)).coerceIn(0.0, 10.0)
                return t.plusSeconds((minutes * 60).toLong()).withSecond(0).withNano(0)
            }
            t = next
            a = b
        }
        return null
    }

    /** Hauteur géocentrique du centre quand le bord apparaît : parallaxe (0,95°) moins réfraction et demi-diamètre. */
    private const val HORIZON = 0.125
}
