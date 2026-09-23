package dev.levilainpetit.wux.widgets

import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.provider.CalendarContract
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.glance.ColorFilter
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalContext
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.lazy.LazyColumn
import androidx.glance.appwidget.lazy.items
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.color.ColorProvider as DayNightColorProvider
import androidx.glance.currentState
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import dev.levilainpetit.wux.MainActivity
import dev.levilainpetit.wux.R
import dev.levilainpetit.wux.calendar.AgendaBuilder
import dev.levilainpetit.wux.calendar.AgendaDay
import dev.levilainpetit.wux.calendar.AgendaLine
import dev.levilainpetit.wux.calendar.CalendarRepository
import es.antonborri.home_widget.HomeWidgetPlugin

/**
 * Widget agenda : les événements groupés par jour, sur [days] jours à partir
 * d'aujourd'hui.
 *
 * [widgetId] est l'identifiant partagé avec Flutter (`lib/src/widgets/catalog.dart`).
 */
abstract class AgendaWidget(val widgetId: String, val days: Int) : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            // Changée par AgendaRefresh à chaque rafraîchissement : elle force
            // la relecture même quand la session Glance est encore ouverte.
            val refresh = currentState(REFRESH_KEY) ?: 0L
            val state = remember(refresh) { load(context) }
            AgendaContent(state)
        }
    }

    private fun load(context: Context): AgendaState {
        val settings = AgendaSettings.read(HomeWidgetPlugin.getData(context), widgetId)
        val repository = CalendarRepository(context)
        if (!repository.hasPermission()) return AgendaState(null, settings.background)
        val agenda = AgendaBuilder.build(context, repository, days, settings.calendarIds)
        return AgendaState(agenda, settings.background)
    }

    companion object {
        val REFRESH_KEY = longPreferencesKey("wux.refresh")
    }
}

class TodayAgendaWidget : AgendaWidget(widgetId = "agenda_today", days = 1)

class TwoDayAgendaWidget : AgendaWidget(widgetId = "agenda_two_days", days = 2)

class TodayAgendaWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = TodayAgendaWidget()

    override fun onEnabled(context: Context) {
        super.onEnabled(context)
        AgendaRefresh.schedule(context)
    }
}

class TwoDayAgendaWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = TwoDayAgendaWidget()

    override fun onEnabled(context: Context) {
        super.onEnabled(context)
        AgendaRefresh.schedule(context)
    }
}

/** `agenda` à `null` : l'accès au calendrier n'est pas accordé. */
private data class AgendaState(val agenda: List<AgendaDay>?, val background: AgendaBackground)

/** Une ligne de la liste : un titre de jour, un événement ou une absence d'événement. */
private sealed interface ListRow {
    val key: Long

    data class Heading(val day: AgendaDay, override val key: Long) : ListRow

    data class Event(val line: AgendaLine, override val key: Long) : ListRow

    data class Empty(override val key: Long) : ListRow
}

/** Couleur identique en mode clair et sombre. */
private fun fixed(color: Color): ColorProvider = DayNightColorProvider(day = color, night = color)

private class Palette(val primary: ColorProvider, val text: ColorProvider, val secondary: ColorProvider)

@Composable
private fun AgendaContent(state: AgendaState) {
    val context = LocalContext.current
    val palette = when (state.background) {
        AgendaBackground.TRANSPARENT -> Palette(
            primary = fixed(Color.White),
            text = fixed(Color.White),
            secondary = fixed(Color(0xE6FFFFFF)),
        )
        AgendaBackground.SURFACE -> Palette(
            primary = ColorProvider(R.color.agenda_primary),
            text = ColorProvider(R.color.agenda_on_surface),
            secondary = ColorProvider(R.color.agenda_on_surface_variant),
        )
    }
    var modifier = GlanceModifier.fillMaxSize().padding(horizontal = 16.dp, vertical = 12.dp)
    if (state.background == AgendaBackground.SURFACE) {
        modifier = GlanceModifier.fillMaxSize()
            .background(ColorProvider(R.color.agenda_surface))
            .cornerRadius(24.dp)
            .padding(horizontal = 16.dp, vertical = 12.dp)
    }

    Box(modifier = modifier) {
        val agenda = state.agenda
        if (agenda == null) {
            Text(
                text = context.getString(R.string.agenda_permission_missing),
                style = TextStyle(color = palette.text, fontSize = 14.sp),
                modifier = GlanceModifier.fillMaxWidth()
                    .clickable(actionStartActivity(Intent(context, MainActivity::class.java))),
            )
        } else {
            LazyColumn {
                items(agenda.toRows(), itemId = { it.key }) { row ->
                    when (row) {
                        is ListRow.Heading -> DayHeading(context, row.day, palette)
                        is ListRow.Event -> EventRow(context, row.line, palette)
                        is ListRow.Empty -> Text(
                            text = context.getString(R.string.agenda_empty),
                            style = TextStyle(color = palette.secondary, fontSize = 14.sp),
                            modifier = GlanceModifier.padding(bottom = 8.dp),
                        )
                    }
                }
            }
        }
    }
}

private fun List<AgendaDay>.toRows(): List<ListRow> {
    val rows = mutableListOf<ListRow>()
    var key = 0L
    forEach { day ->
        rows += ListRow.Heading(day, key++)
        if (day.lines.isEmpty()) rows += ListRow.Empty(key++)
        day.lines.forEach { rows += ListRow.Event(it, key++) }
    }
    return rows
}

@Composable
private fun DayHeading(context: Context, day: AgendaDay, palette: Palette) {
    val millis = day.date.atStartOfDay(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()
    val open = Intent(
        Intent.ACTION_VIEW,
        CalendarContract.CONTENT_URI.buildUpon().appendPath("time").also {
            ContentUris.appendId(it, millis)
        }.build(),
    )
    Text(
        text = day.label,
        style = TextStyle(color = palette.primary, fontSize = 26.sp, fontWeight = FontWeight.Bold),
        modifier = GlanceModifier.padding(top = 8.dp, bottom = 8.dp)
            .clickable(actionStartActivity(open)),
    )
}

@Composable
private fun EventRow(context: Context, line: AgendaLine, palette: Palette) {
    val open = Intent(
        Intent.ACTION_VIEW,
        ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI, line.eventId),
    )
        .putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, line.begin)
        .putExtra(CalendarContract.EXTRA_EVENT_END_TIME, line.end)
    Row(
        modifier = GlanceModifier.fillMaxWidth().padding(bottom = 12.dp)
            .clickable(actionStartActivity(open)),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = GlanceModifier.defaultWeight()) {
            Text(line.time, style = TextStyle(color = palette.secondary, fontSize = 13.sp))
            Text(
                line.title,
                maxLines = 2,
                style = TextStyle(color = palette.text, fontSize = 15.sp, fontWeight = FontWeight.Medium),
            )
            if (line.location.isNotBlank()) {
                Text(
                    line.location,
                    maxLines = 1,
                    style = TextStyle(color = palette.secondary, fontSize = 12.sp),
                )
            }
        }
        Spacer(GlanceModifier.width(12.dp))
        Image(
            provider = ImageProvider(R.drawable.agenda_dot),
            contentDescription = null,
            colorFilter = ColorFilter.tint(fixed(Color(line.color))),
            modifier = GlanceModifier.size(12.dp),
        )
    }
}

