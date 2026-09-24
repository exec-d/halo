package dev.levilainpetit.wux.widgets

import android.content.Context
import android.content.Intent
import dev.levilainpetit.wux.R
import dev.levilainpetit.wux.calendar.AgendaDay
import dev.levilainpetit.wux.calendar.AgendaLine
import dev.levilainpetit.wux.weather.Air
import dev.levilainpetit.wux.weather.CurvePoint
import dev.levilainpetit.wux.weather.Day
import dev.levilainpetit.wux.weather.Forecast
import dev.levilainpetit.wux.weather.Hour
import dev.levilainpetit.wux.weather.Place
import dev.levilainpetit.wux.weather.Pollen
import dev.levilainpetit.wux.weather.PollenDay
import dev.levilainpetit.wux.weather.RainHour
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.YearMonth
import java.util.Locale

/**
 * Données d'exemple des aperçus de la liste des widgets : chaque widget y est
 * montré rempli, même sans autorisation ni données sur le téléphone.
 */
object SampleData {

    private const val CYAN = 0xFF3FD8F0.toInt()
    private const val YELLOW = 0xFFF2C14E.toInt()
    private const val PINK = 0xFFF06292.toInt()

    fun agenda(context: Context): List<AgendaDay> {
        val today = LocalDate.now()
        val range = context.getString(R.string.agenda_range, "%s", "%s")
        fun line(title: String, from: String?, to: String?, location: String, color: Int) = AgendaLine(
            eventId = 0,
            begin = 0,
            end = 0,
            time = if (from == null) context.getString(R.string.agenda_all_day) else range.format(from, to),
            title = title,
            location = location,
            color = color,
        )
        val locale = Locale.getDefault()
        return listOf(
            AgendaDay(
                today,
                context.getString(R.string.agenda_today).uppercase(locale),
                listOf(
                    line("Bureau", null, null, "", CYAN),
                    line("Standup", "09:30", "09:45", "Visio", CYAN),
                    line("Déjeuner avec Léa", "12:30", "14:00", "Le Comptoir", PINK),
                    line("Karaté des enfants", "18:00", "19:00", "Gymnase", YELLOW),
                ),
            ),
            AgendaDay(
                today.plusDays(1),
                context.getString(R.string.agenda_tomorrow).uppercase(locale),
                listOf(
                    line("École", "08:20", "16:30", "", YELLOW),
                    line("Revue de sprint", "10:00", "11:00", "Salle Jupiter", CYAN),
                    line("Dentiste", "17:15", "17:45", "", PINK),
                ),
            ),
        )
    }

    private fun tile(label: String, value: String, detail: String, progress: Int?) =
        SystemTile(label, value, detail, progress, Intent())

    fun systemBasic(context: Context) = listOf(
        tile(context.getString(R.string.system_battery), "82 %", context.getString(R.string.system_charging), 82),
        tile(context.getString(R.string.system_network), context.getString(R.string.system_wifi), context.getString(R.string.system_signal, 4), 100),
        tile(context.getString(R.string.system_storage), "64 Go", context.getString(R.string.system_free_of, "128 Go"), 50),
    )

    fun systemAdvanced(context: Context) = listOf(
        listOf(
            tile(context.getString(R.string.system_cellular), "Orange", context.getString(R.string.system_signal, 3), 75),
            tile(context.getString(R.string.system_wifi_label), context.getString(R.string.system_connected), "5 GHz · 866 Mb/s", 100),
            tile(context.getString(R.string.system_bluetooth), context.getString(R.string.system_on), "", null),
            tile(context.getString(R.string.system_battery), "82 %", context.getString(R.string.system_charging) + " · 31 °C", 82),
        ),
        listOf(
            tile(context.getString(R.string.system_memory), "3,1 Go", context.getString(R.string.system_free_of, "8 Go"), 61),
            tile(context.getString(R.string.system_storage), "64 Go", context.getString(R.string.system_free_of, "128 Go"), 50),
            tile(context.getString(R.string.system_location), context.getString(R.string.system_on), "", null),
            tile(context.getString(R.string.system_sound), context.getString(R.string.system_vibrate), "", 40),
        ),
    )

    val place = Place("Lyon, Auvergne-Rhône-Alpes, France", 45.76, 4.84)

    fun forecast(): Forecast {
        val now = LocalDateTime.now().withMinute(0).withSecond(0).withNano(0)
        val codes = intArrayOf(2, 2, 3, 3, 61, 61)
        val hours = (1..6).map { i ->
            val time = now.plusHours(i.toLong())
            Hour(time, 18.0 - i * 0.7, codes[i - 1], time.hour in 7..20)
        }
        val today = LocalDate.now()
        return Forecast(
            fetchedAt = System.currentTimeMillis(),
            time = now,
            temperature = 18.4,
            apparent = 17.2,
            code = 2,
            isDay = now.hour in 7..20,
            wind = 12.0,
            windDirection = 225,
            humidity = 72,
            uv = 4.0,
            curve = (0 until 24).map { i ->
                val time = now.plusHours(i.toLong())
                CurvePoint(
                    time,
                    doubleArrayOf(18.0, 17.0, 16.0, 15.0, 14.0, 13.0, 13.0, 12.0, 12.0, 13.0, 15.0, 18.0,
                        20.0, 22.0, 24.0, 25.0, 26.0, 26.0, 25.0, 23.0, 21.0, 20.0, 19.0, 18.0)[i],
                    intArrayOf(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 10, 30, 60, 70, 40, 20, 5, 0, 0, 0, 0)[i],
                    time.hour in 7..20,
                )
            },
            hours = hours,
            rain = (0 until 12).map { i ->
                RainHour(now.plusHours(i.toLong()), intArrayOf(5, 10, 20, 45, 70, 80, 60, 35, 20, 10, 5, 5)[i], if (i in 4..6) 0.8 else 0.0)
            },
            rainSoon = List(8) { 0.0 },
            days = listOf(
                Day(today, 2, 21.0, 11.0, LocalTime.of(7, 42), LocalTime.of(19, 48)),
                Day(today.plusDays(1), 61, 17.0, 10.0, LocalTime.of(7, 43), LocalTime.of(19, 46)),
            ),
        )
    }

    fun air(): Air {
        val today = LocalDate.now()
        fun day(offset: Long, grass: Double, birch: Double, mugwort: Double, ragweed: Double) = PollenDay(
            today.plusDays(offset),
            mapOf(
                Pollen.GRASS to grass,
                Pollen.BIRCH to birch,
                Pollen.ALDER to 0.0,
                Pollen.OLIVE to 0.0,
                Pollen.MUGWORT to mugwort,
                Pollen.RAGWEED to ragweed,
            ),
        )
        return Air(aqi = 25, days = listOf(day(0, 3.0, 0.0, 6.0, 14.0), day(1, 4.0, 0.0, 8.0, 18.0), day(2, 2.0, 0.0, 5.0, 9.0)))
    }

    fun busyDays(month: YearMonth): Set<LocalDate> =
        listOf(3, 8, 11, 15, 16, 22, 23, 29).filter { it <= month.lengthOfMonth() }.map { month.atDay(it) }.toSet()
}
