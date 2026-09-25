package dev.levilainpetit.wux.wallpaper

import kotlin.math.PI
import kotlin.math.asin
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.tan

/** Une étoile : ascension droite (heures), déclinaison (degrés), magnitude. */
class Star(val name: String, val ra: Double, val dec: Double, val magnitude: Double)

/**
 * Les étoiles les plus brillantes et les tracés des constellations connues
 * (coordonnées J2000 arrondies, à un dixième de degré près), et le calcul de
 * leur position dans le ciel d'un lieu et d'une heure.
 */
object Stars {

    val ALL = listOf(
        Star("Sirius", 6.752, -16.72, -1.46), Star("Canopus", 6.399, -52.70, -0.74),
        Star("Arcturus", 14.261, 19.18, -0.05), Star("RigilKent", 14.660, -60.83, -0.27),
        Star("Vega", 18.616, 38.78, 0.03), Star("Capella", 5.278, 46.00, 0.08),
        Star("Rigel", 5.242, -8.20, 0.13), Star("Procyon", 7.655, 5.22, 0.34),
        Star("Achernar", 1.629, -57.24, 0.46), Star("Betelgeuse", 5.919, 7.41, 0.50),
        Star("Hadar", 14.064, -60.37, 0.61), Star("Altair", 19.846, 8.87, 0.76),
        Star("Acrux", 12.443, -63.10, 0.76), Star("Aldebaran", 4.599, 16.51, 0.86),
        Star("Antares", 16.490, -26.43, 0.96), Star("Spica", 13.420, -11.16, 0.97),
        Star("Pollux", 7.755, 28.03, 1.14), Star("Fomalhaut", 22.961, -29.62, 1.16),
        Star("Deneb", 20.690, 45.28, 1.25), Star("Mimosa", 12.795, -59.69, 1.25),
        Star("Regulus", 10.140, 11.97, 1.35), Star("Adhara", 6.977, -28.97, 1.50),
        Star("Castor", 7.577, 31.89, 1.58), Star("Gacrux", 12.519, -57.11, 1.63),
        Star("Shaula", 17.560, -37.10, 1.62), Star("Bellatrix", 5.419, 6.35, 1.64),
        Star("Elnath", 5.438, 28.61, 1.65), Star("Alnilam", 5.604, -1.20, 1.69),
        Star("Alnitak", 5.679, -1.94, 1.74), Star("Alioth", 12.900, 55.96, 1.77),
        Star("Dubhe", 11.062, 61.75, 1.79), Star("Mirfak", 3.405, 49.86, 1.79),
        Star("Wezen", 7.140, -26.39, 1.83), Star("Sargas", 17.622, -43.00, 1.86),
        Star("KausAustralis", 18.403, -34.38, 1.85), Star("Alkaid", 13.792, 49.31, 1.86),
        Star("Menkalinan", 5.992, 44.95, 1.90), Star("Alhena", 6.629, 16.40, 1.93),
        Star("Polaris", 2.530, 89.26, 1.98), Star("Mirzam", 6.378, -17.96, 1.98),
        Star("Alphard", 9.460, -8.66, 1.98), Star("Hamal", 2.120, 23.46, 2.00),
        Star("Algieba", 10.333, 19.84, 2.08), Star("Nunki", 18.921, -26.30, 2.05),
        Star("Alpheratz", 0.140, 29.09, 2.06), Star("Mirach", 1.162, 35.62, 2.05),
        Star("Saiph", 5.796, -9.67, 2.06), Star("Kochab", 14.845, 74.16, 2.08),
        Star("Rasalhague", 17.582, 12.56, 2.08), Star("Algol", 3.136, 40.96, 2.10),
        Star("Almach", 2.065, 42.33, 2.10), Star("Denebola", 11.818, 14.57, 2.13),
        Star("Schedar", 0.675, 56.54, 2.24), Star("Mintaka", 5.533, -0.30, 2.23),
        Star("Alphecca", 15.578, 26.71, 2.23), Star("Sadr", 20.370, 40.26, 2.23),
        Star("Eltanin", 17.943, 51.49, 2.23), Star("Caph", 0.153, 59.15, 2.27),
        Star("Mizar", 13.399, 54.93, 2.23), Star("Merak", 11.031, 56.38, 2.37),
        Star("Izar", 14.750, 27.07, 2.37), Star("Enif", 21.736, 9.88, 2.39),
        Star("Scheat", 23.063, 28.08, 2.42), Star("Phecda", 11.897, 53.69, 2.44),
        Star("Markab", 23.079, 15.21, 2.49), Star("GammaCas", 0.945, 60.72, 2.47),
        Star("Dschubba", 16.006, -22.62, 2.29), Star("Algenib", 0.220, 15.18, 2.83),
        Star("Ruchbah", 1.430, 60.24, 2.68), Star("Segin", 1.907, 63.67, 3.37),
        Star("Megrez", 12.257, 57.03, 3.31), Star("Zosma", 11.235, 20.52, 2.56),
        Star("Tarazed", 19.771, 10.61, 2.72), Star("Alshain", 19.922, 6.41, 3.71),
        Star("Albireo", 19.512, 27.96, 3.05), Star("GienahCyg", 20.770, 33.97, 2.48),
        Star("DeltaCyg", 19.750, 45.13, 2.87), Star("Sheliak", 18.835, 33.36, 3.52),
        Star("Sulafat", 18.982, 32.69, 3.25), Star("KausMedia", 18.350, -29.83, 2.72),
        Star("KausBorealis", 18.466, -25.42, 2.81), Star("Ascella", 19.043, -29.88, 2.60),
        Star("Alnasl", 18.097, -30.42, 2.99), Star("PhiSgr", 18.761, -26.99, 3.17),
        Star("TauSgr", 19.116, -27.67, 3.32), Star("Alcyone", 3.791, 24.11, 2.87),
        Star("Chort", 11.237, 15.43, 3.33), Star("Adhafera", 10.278, 23.42, 3.43),
        Star("Rasalas", 9.879, 26.01, 3.88), Star("EtaLeo", 10.122, 16.76, 3.48),
        Star("Acrab", 16.091, -19.81, 2.62), Star("PiSco", 15.981, -26.11, 2.89),
        Star("SigmaSco", 16.353, -25.59, 2.89), Star("TauSco", 16.598, -28.22, 2.82),
        Star("EpsSco", 16.836, -34.29, 2.29), Star("MuSco", 16.864, -38.05, 3.00),
        Star("EtaSco", 17.202, -43.24, 3.33), Star("IotaSco", 17.793, -40.13, 3.03),
        Star("KappaSco", 17.708, -39.03, 2.41), Star("Lesath", 17.513, -37.30, 2.70),
        Star("Mebsuta", 6.732, 25.13, 3.06), Star("Tejat", 6.383, 22.51, 2.88),
        Star("Wasat", 7.335, 21.98, 3.53), Star("DeltaCru", 12.252, -58.75, 2.79),
        Star("Pherkad", 15.345, 71.83, 3.00),
    )

    /** Les tracés des constellations, étoile à étoile. */
    val LINES = listOf(
        // Orion
        "Betelgeuse" to "Bellatrix", "Bellatrix" to "Mintaka", "Mintaka" to "Alnilam", "Alnilam" to "Alnitak",
        "Alnitak" to "Saiph", "Saiph" to "Rigel", "Rigel" to "Mintaka", "Betelgeuse" to "Alnitak",
        // Grande Ourse
        "Dubhe" to "Merak", "Merak" to "Phecda", "Phecda" to "Megrez", "Megrez" to "Dubhe",
        "Megrez" to "Alioth", "Alioth" to "Mizar", "Mizar" to "Alkaid",
        // Petite Ourse (bout)
        "Kochab" to "Pherkad",
        // Cassiopée
        "Caph" to "Schedar", "Schedar" to "GammaCas", "GammaCas" to "Ruchbah", "Ruchbah" to "Segin",
        // Cygne
        "Deneb" to "Sadr", "Sadr" to "Albireo", "GienahCyg" to "Sadr", "Sadr" to "DeltaCyg",
        // Lyre
        "Vega" to "Sheliak", "Sheliak" to "Sulafat", "Sulafat" to "Vega",
        // Aigle
        "Tarazed" to "Altair", "Altair" to "Alshain",
        // Lion
        "Regulus" to "EtaLeo", "EtaLeo" to "Algieba", "Algieba" to "Adhafera", "Adhafera" to "Rasalas",
        "Algieba" to "Zosma", "Zosma" to "Denebola", "Denebola" to "Chort", "Chort" to "Regulus",
        // Scorpion
        "Acrab" to "Dschubba", "Dschubba" to "PiSco", "Dschubba" to "SigmaSco", "SigmaSco" to "Antares",
        "Antares" to "TauSco", "TauSco" to "EpsSco", "EpsSco" to "MuSco", "MuSco" to "EtaSco",
        "EtaSco" to "Sargas", "Sargas" to "IotaSco", "IotaSco" to "KappaSco", "KappaSco" to "Shaula",
        "Shaula" to "Lesath",
        // Pégase et Andromède
        "Markab" to "Scheat", "Scheat" to "Alpheratz", "Alpheratz" to "Algenib", "Algenib" to "Markab",
        "Alpheratz" to "Mirach", "Mirach" to "Almach",
        // Gémeaux
        "Castor" to "Mebsuta", "Mebsuta" to "Tejat", "Pollux" to "Wasat", "Wasat" to "Alhena",
        // Croix du Sud
        "Acrux" to "Gacrux", "Mimosa" to "DeltaCru",
        // Sagittaire (la théière)
        "Alnasl" to "KausMedia", "KausMedia" to "KausAustralis", "KausAustralis" to "Alnasl",
        "KausMedia" to "KausBorealis", "KausBorealis" to "PhiSgr", "PhiSgr" to "KausMedia",
        "PhiSgr" to "Nunki", "Nunki" to "TauSgr", "TauSgr" to "Ascella", "Ascella" to "PhiSgr",
        "Ascella" to "KausAustralis",
        // Taureau, Grand Chien, Cocher
        "Aldebaran" to "Elnath", "Sirius" to "Mirzam", "Sirius" to "Wezen", "Wezen" to "Adhara",
        "Capella" to "Menkalinan",
    )

    private const val RAD = PI / 180

    /** Jours depuis J2000,0. */
    fun days(millis: Long) = millis / 86_400_000.0 + 2_440_587.5 - 2_451_545.0

    /**
     * Hauteur et azimut (degrés ; azimut depuis le nord, vers l'est) d'un
     * point du ciel ([raHours], [decDeg]) vu de ([latDeg], [lonDeg]).
     */
    fun horizontal(raHours: Double, decDeg: Double, latDeg: Double, lonDeg: Double, d: Double): Pair<Double, Double> {
        val gmst = 280.46061837 + 360.98564736629 * d
        val hourAngle = (gmst + lonDeg - raHours * 15) * RAD
        val dec = decDeg * RAD
        val lat = latDeg * RAD
        val altitude = asin(sin(dec) * sin(lat) + cos(dec) * cos(lat) * cos(hourAngle))
        val azimuth = atan2(-cos(dec) * sin(hourAngle), sin(dec) * cos(lat) - cos(dec) * sin(lat) * cos(hourAngle))
        return altitude / RAD to ((azimuth / RAD) + 360) % 360
    }

    /** Position de la Lune (ascension droite en heures, déclinaison), à un degré près environ. */
    fun moon(d: Double): Pair<Double, Double> {
        val l = (218.316 + 13.176396 * d) * RAD
        val m = (134.963 + 13.064993 * d) * RAD
        val f = (93.272 + 13.229350 * d) * RAD
        val lambda = l + 6.289 * RAD * sin(m)
        val beta = 5.128 * RAD * sin(f)
        return equatorial(lambda, beta)
    }

    /** Position du Soleil (ascension droite en heures, déclinaison). */
    fun sun(d: Double): Pair<Double, Double> {
        val l = (280.460 + 0.9856474 * d) * RAD
        val g = (357.528 + 0.9856003 * d) * RAD
        val lambda = l + (1.915 * sin(g) + 0.020 * sin(2 * g)) * RAD
        return equatorial(lambda, 0.0)
    }

    private fun equatorial(lambda: Double, beta: Double): Pair<Double, Double> {
        val epsilon = 23.439 * RAD
        val ra = atan2(sin(lambda) * cos(epsilon) - tan(beta) * sin(epsilon), cos(lambda))
        val dec = asin(sin(beta) * cos(epsilon) + cos(beta) * sin(epsilon) * sin(lambda))
        return (((ra / RAD) + 360) % 360) / 15 to dec / RAD
    }
}
