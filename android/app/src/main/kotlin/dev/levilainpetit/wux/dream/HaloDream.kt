package dev.levilainpetit.wux.dream

import android.os.Handler
import android.os.Looper
import android.service.dreams.DreamService
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import dev.levilainpetit.wux.R
import dev.levilainpetit.wux.calendar.AgendaBuilder
import dev.levilainpetit.wux.calendar.CalendarRepository
import dev.levilainpetit.wux.weather.Weather
import dev.levilainpetit.wux.widgets.NextAlarm
import kotlin.math.roundToInt

/**
 * Écran de veille Halo : pendant la charge ou sur un socle, une grande
 * horloge néon sur le décor Circuit, avec la prochaine alarme, le prochain
 * événement et la météo.
 *
 * Contre le marquage de l'écran, le bloc de texte se déplace un peu chaque
 * minute ; un toucher réveille le téléphone.
 */
class HaloDream : DreamService() {

    private val handler = Handler(Looper.getMainLooper())
    private var content: View? = null
    private var step = 0

    private val minute = object : Runnable {
        override fun run() {
            refresh()
            handler.postDelayed(this, 60_000L)
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        isInteractive = false
        isFullscreen = true
        isScreenBright = false
        val root = FrameLayout(this)
        root.addView(CircuitView(this), FrameLayout.LayoutParams(-1, -1))
        val info = LayoutInflater.from(this).inflate(R.layout.dream_halo, root, false)
        root.addView(info)
        content = info
        setContentView(root)
    }

    override fun onDreamingStarted() {
        super.onDreamingStarted()
        handler.post(minute)
    }

    override fun onDreamingStopped() {
        super.onDreamingStopped()
        handler.removeCallbacks(minute)
    }

    private fun refresh() {
        val view = content ?: return
        val alarm = NextAlarm.label(this)
        view.findViewById<TextView>(R.id.dream_alarm).apply {
            text = alarm.orEmpty()
            visibility = if (alarm == null) View.GONE else View.VISIBLE
        }
        val event = nextEvent()
        view.findViewById<TextView>(R.id.dream_event).apply {
            text = event.orEmpty()
            visibility = if (event == null) View.GONE else View.VISIBLE
        }
        val weather = Weather.forecast(this)?.let { "${it.temperature.roundToInt()}° · ${getString(Weather.label(it.code))}" }
        view.findViewById<TextView>(R.id.dream_weather).apply {
            text = weather.orEmpty()
            visibility = if (weather == null) View.GONE else View.VISIBLE
        }
        // Un petit tour, un pas par minute, pour ne jamais marquer l'écran.
        val d = resources.displayMetrics.density
        val offsets = listOf(0 to 0, 6 to 4, 10 to 10, 4 to 14, -4 to 10, -8 to 4)
        val (dx, dy) = offsets[step++ % offsets.size]
        view.translationX = dx * d
        view.translationY = dy * d
    }

    /** « 14:00 · Réunion », l'événement en cours ou le suivant d'aujourd'hui. */
    private fun nextEvent(): String? {
        val repository = CalendarRepository(this)
        if (!repository.hasPermission()) return null
        return runCatching {
            AgendaBuilder.build(this, repository, 1, null, showAllDay = false)
                .firstOrNull()?.lines?.firstOrNull()
                ?.let { "${it.time} · ${it.title}" }
        }.getOrNull()
    }
}
