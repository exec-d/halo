package dev.levilainpetit.wux.widgets

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.util.SizeF
import android.text.format.DateFormat
import android.view.View
import android.view.ViewGroup
import android.widget.TextClock
import android.widget.FrameLayout
import android.widget.RemoteViews
import java.io.ByteArrayOutputStream
import java.util.Calendar
import java.util.TimeZone
import kotlin.math.roundToInt

/**
 * Un widget capable de produire sa mise en page pour une taille donnée (dp).
 * [sample] : avec des données d'exemple plutôt que les vraies, pour la liste
 * des widgets, qui doit être belle même sans autorisation ni données.
 */
interface PreviewableWidget {
    fun preview(context: Context, size: SizeF, sample: Boolean): RemoteViews
}

/**
 * Aperçus de l'application : le vrai widget, dessiné dans l'application puis
 * envoyé à Flutter en PNG. Même mise en page, mêmes polices, mêmes couleurs
 * que sur l'écran d'accueil, sans rien dupliquer côté Dart.
 */
object WidgetPreviews {

    /** Identifiants partagés avec `lib/src/home_widgets/catalog.dart`. */
    private fun widget(id: String): PreviewableWidget? = when (id) {
        "clock" -> ClockWidgetProvider()
        "agenda_one_column" -> OneColumnAgendaWidget()
        "agenda_two_columns" -> TwoColumnAgendaWidget()
        "system" -> SystemWidget()
        "system_advanced" -> AdvancedSystemWidget()
        "world_clock" -> WorldClockWidget()
        "countdown" -> CountdownWidget()
        "controls" -> ControlsWidget()
        "month" -> MonthWidget()
        "weather" -> WeatherWidget()
        "sun_moon" -> SunMoonWidget()
        "rain" -> RainWidget()
        "allergy" -> AllergyWidget()
        else -> null
    }

    /** À appeler sur le fil principal : il construit des vues. */
    fun render(context: Context, id: String, widthDp: Float, heightDp: Float, sample: Boolean): ByteArray? {
        val views = widget(id)?.preview(context, SizeF(widthDp, heightDp), sample) ?: return null
        val density = context.resources.displayMetrics.density
        val width = (widthDp * density).roundToInt()
        val height = (heightDp * density).roundToInt()
        val parent = FrameLayout(context)
        val view = views.apply(context, parent)
        parent.addView(view, FrameLayout.LayoutParams(width, height))
        fillClocks(view)
        parent.measure(
            View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY),
            View.MeasureSpec.makeMeasureSpec(height, View.MeasureSpec.EXACTLY),
        )
        parent.layout(0, 0, width, height)
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        parent.draw(Canvas(bitmap))
        return ByteArrayOutputStream().use {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
            bitmap.recycle()
            it.toByteArray()
        }
    }

    /**
     * Un `TextClock` ne s'écrit qu'une fois attaché à une fenêtre ; hors écran
     * il reste vide. On lui donne son texte, à son fuseau, avec son motif.
     */
    private fun fillClocks(view: View) {
        if (view is TextClock) {
            val pattern = if (DateFormat.is24HourFormat(view.context)) view.format24Hour else view.format12Hour
            val zone = view.timeZone?.let { TimeZone.getTimeZone(it) } ?: TimeZone.getDefault()
            if (pattern != null) view.text = DateFormat.format(pattern, Calendar.getInstance(zone))
        }
        if (view is ViewGroup) for (i in 0 until view.childCount) fillClocks(view.getChildAt(i))
    }
}
