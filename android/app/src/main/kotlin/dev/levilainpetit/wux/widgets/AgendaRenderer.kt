package dev.levilainpetit.wux.widgets

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.CalendarContract
import android.util.SizeF
import android.view.View
import android.widget.RemoteViews
import dev.levilainpetit.wux.MainActivity
import dev.levilainpetit.wux.R
import dev.levilainpetit.wux.calendar.AgendaDay
import dev.levilainpetit.wux.calendar.AgendaLine
import java.time.ZoneId

/**
 * Dessine un agenda en une ou deux colonnes, sans défilement, comme la maquette.
 *
 * Une `RemoteViews` ne se mesure pas avant d'être affichée : on estime la
 * hauteur de chaque élément (en dp, grossissement du texte compris) et l'on
 * remplit les colonnes jusqu'à ce que le suivant ne tienne plus, auquel cas
 * « ••• » signale la suite. Sur Android 12+, une mise en page est produite
 * pour chaque taille que le lanceur annonce (portrait, paysage).
 */
object AgendaRenderer {

    /** En dessous de cette largeur (dp), deux colonnes ne tiennent pas : une seule. */
    private const val TWO_COLUMNS_MIN_WIDTH = 220f

    // Hauteurs estimées (dp à 100 % de texte) ; suivre les mises en page agenda_*.
    private const val HEADING = 24f
    private const val ROW = 35f
    private const val NOTE = 17f

    private sealed interface Item {
        val height: Float

        data class Heading(val day: AgendaDay) : Item {
            override val height = HEADING
        }

        data class Event(val line: AgendaLine) : Item {
            override val height = ROW
        }

        data class Note(val text: String, val opensApp: Boolean = false) : Item {
            override val height = NOTE
        }
    }

    /**
     * [agenda] à `null` : l'accès au calendrier n'est pas accordé.
     * [options] : celles du widget, pour sa taille.
     */
    fun views(
        context: Context,
        agenda: List<AgendaDay>?,
        options: Bundle,
        columns: Int,
    ): RemoteViews {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            @Suppress("DEPRECATION")
            val sizes = options.getParcelableArrayList<SizeF>(AppWidgetManager.OPTION_APPWIDGET_SIZES)
            if (!sizes.isNullOrEmpty()) {
                return RemoteViews(sizes.associateWith { render(context, agenda, it, columns) })
            }
        }
        // Avant Android 12 : la taille portrait (largeur mini, hauteur maxi).
        val width = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH, 250)
        val height = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT, 180)
        return render(context, agenda, SizeF(width.toFloat(), height.toFloat()), columns)
    }

    fun render(
        context: Context,
        agenda: List<AgendaDay>?,
        size: SizeF,
        columns: Int,
    ): RemoteViews {
        val views = RemoteViews(context.packageName, R.layout.widget_agenda)

        val twoColumns = columns == 2 && size.width >= TWO_COLUMNS_MIN_WIDTH
        views.setViewVisibility(R.id.agenda_separator, if (twoColumns) View.VISIBLE else View.GONE)
        views.setViewVisibility(R.id.agenda_col2, if (twoColumns) View.VISIBLE else View.GONE)
        views.removeAllViews(R.id.agenda_col1)
        views.removeAllViews(R.id.agenda_col2)

        val scale = context.resources.configuration.fontScale
        val padding = context.resources.getDimension(R.dimen.agenda_padding) /
            context.resources.displayMetrics.density
        val usable = size.height - 2 * padding
        val capacities = List(if (twoColumns) 2 else 1) { usable }

        val items = items(context, agenda)
        val columns = List(capacities.size) { mutableListOf<Item>() }
        var column = 0
        var used = 0f
        var overflow = false
        for ((index, item) in items.withIndex()) {
            val height = item.height * scale
            // Un titre de jour ne reste pas seul en bas d'une colonne.
            val next = items.getOrNull(index + 1)?.height?.times(scale) ?: 0f
            val needed = if (item is Item.Heading) height + next else height
            if (used + needed > capacities[column]) {
                if (column + 1 < columns.size) {
                    column++
                    used = 0f
                }
                if (used + needed > capacities[column]) {
                    overflow = true
                    break
                }
            }
            columns[column] += item
            used += height
        }
        if (overflow) {
            val last = columns[column]
            // Place pour « ••• » : on retire au besoin le dernier élément.
            while (last.isNotEmpty() && used + NOTE * scale > capacities[column]) {
                used -= last.removeAt(last.lastIndex).height * scale
            }
            last += Item.Note(context.getString(R.string.agenda_more))
        }

        columns.forEachIndexed { i, content ->
            val id = if (i == 0) R.id.agenda_col1 else R.id.agenda_col2
            content.forEach { views.addView(id, itemView(context, it)) }
        }
        return views
    }

    private fun items(context: Context, agenda: List<AgendaDay>?): List<Item> {
        if (agenda == null) {
            return listOf(Item.Note(context.getString(R.string.agenda_permission_missing), opensApp = true))
        }
        return agenda.flatMap { day ->
            listOf(Item.Heading(day)) +
                if (day.lines.isEmpty()) {
                    listOf(Item.Note(context.getString(R.string.agenda_empty)))
                } else {
                    day.lines.map { Item.Event(it) }
                }
        }
    }

    private fun itemView(context: Context, item: Item): RemoteViews = when (item) {
        is Item.Heading -> RemoteViews(context.packageName, R.layout.agenda_heading).apply {
            setTextViewText(R.id.agenda_heading, item.day.label)
            val millis = item.day.date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
            val open = Intent(
                Intent.ACTION_VIEW,
                CalendarContract.CONTENT_URI.buildUpon().appendPath("time").also {
                    ContentUris.appendId(it, millis)
                }.build(),
            )
            setOnClickPendingIntent(R.id.agenda_heading, activity(context, open, millis.hashCode()))
        }
        is Item.Event -> RemoteViews(
            context.packageName,
            when {
                item.line.ongoing -> R.layout.agenda_row_now
                item.line.later -> R.layout.agenda_row_later
                else -> R.layout.agenda_row
            },
        ).apply {
            val line = item.line
            setTextViewText(R.id.agenda_row_title, line.title)
            setTextViewText(R.id.agenda_row_detail, line.detail)
            // Teinte le trait et son halo à la couleur de l'agenda.
            setInt(R.id.agenda_row_bar, "setColorFilter", line.color)
            val open = Intent(
                Intent.ACTION_VIEW,
                ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI, line.eventId),
            )
                .putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, line.begin)
                .putExtra(CalendarContract.EXTRA_EVENT_END_TIME, line.end)
            setOnClickPendingIntent(R.id.agenda_row, activity(context, open, (line.eventId * 31 + line.begin).hashCode()))
        }
        is Item.Note -> RemoteViews(context.packageName, R.layout.agenda_note).apply {
            setTextViewText(R.id.agenda_note, item.text)
            if (item.opensApp) {
                setOnClickPendingIntent(
                    R.id.agenda_note,
                    activity(context, Intent(context, MainActivity::class.java), 1),
                )
            }
        }
    }

    private fun activity(context: Context, intent: Intent, requestCode: Int): PendingIntent =
        PendingIntent.getActivity(
            context,
            requestCode,
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )
}
