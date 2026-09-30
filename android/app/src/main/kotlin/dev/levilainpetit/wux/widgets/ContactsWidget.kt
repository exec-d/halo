package dev.levilainpetit.wux.widgets

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.BitmapShader
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Shader
import android.net.Uri
import android.util.SizeF
import android.view.View
import android.widget.RemoteViews
import dev.levilainpetit.wux.MainActivity
import dev.levilainpetit.wux.R
import java.io.File
import java.util.Locale
import kotlin.math.roundToInt

/**
 * Contacts favoris : jusqu'à six pastilles néon (la photo du contact, ou son
 * initiale). Un toucher appelle, ou ouvre la conversation SMS, selon le
 * réglage.
 */
class ContactsWidget : NeonWidget() {

    override fun build(context: Context, size: SizeF, sample: Boolean): RemoteViews {
        val views = RemoteViews(context.packageName, R.layout.widget_contacts)
        views.setOnClickPendingIntent(R.id.contacts_root, activity(context, Intent(context, MainActivity::class.java), 699))
        val contacts = if (sample) SAMPLE else FavoriteContacts.list(context)
        val sms = FavoriteContacts.sms(context)
        val density = context.resources.displayMetrics.density
        // Autant de pastilles que la largeur en laisse tenir (58 dp chacune).
        val fit = ((size.width - 48) / 58f).toInt().coerceIn(1, FavoriteContacts.MAX)
        val avatar = (44 * density).roundToInt()
        SLOTS.forEachIndexed { i, (slot, photo, name) ->
            val contact = contacts.getOrNull(i)?.takeIf { i < fit }
            views.setViewVisibility(slot, if (contact == null) View.GONE else View.VISIBLE)
            if (contact == null) return@forEachIndexed
            views.setImageViewBitmap(photo, avatar(context, contact, avatar, density))
            views.setTextViewText(name, contact.name.substringBefore(' '))
            views.setContentDescription(slot, contact.name)
            views.setOnClickPendingIntent(slot, activity(context, action(context, contact, sms), 700 + i))
        }
        views.setViewVisibility(R.id.contacts_empty, if (contacts.isEmpty()) View.VISIBLE else View.GONE)
        views.setTextViewText(R.id.contacts_title, context.getString(if (sms) R.string.contacts_title_sms else R.string.contacts_title))
        return views
    }

    /** Appeler (directement si Halo en a le droit, sinon par le clavier), ou écrire. */
    private fun action(context: Context, contact: FavoriteContact, sms: Boolean): Intent {
        val number = Uri.encode(contact.number)
        return when {
            sms -> Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:$number"))
            context.checkSelfPermission(Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED ->
                Intent(Intent.ACTION_CALL, Uri.parse("tel:$number"))
            else -> Intent(Intent.ACTION_DIAL, Uri.parse("tel:$number"))
        }
    }

    /** Une pastille : la photo en rond, ou l'initiale, dans un anneau néon. */
    private fun avatar(context: Context, contact: FavoriteContact, size: Int, density: Float): Bitmap {
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val line = context.getColor(R.color.clock_line)
        val core = context.getColor(R.color.clock_core)
        val c = size / 2f
        val ring = 1.6f * density
        val inner = c - 3.5f * density
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        val photo = contact.photo?.let { path -> runCatching { BitmapFactory.decodeFile(File(path).path) }.getOrNull() }
        if (photo != null) {
            val shader = BitmapShader(photo, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP)
            val scale = 2 * inner / minOf(photo.width, photo.height)
            shader.setLocalMatrix(Matrix().apply {
                setScale(scale, scale)
                postTranslate(c - photo.width * scale / 2, c - photo.height * scale / 2)
            })
            paint.shader = shader
            canvas.drawCircle(c, c, inner, paint)
            paint.shader = null
        } else {
            paint.color = line
            paint.alpha = 40
            canvas.drawCircle(c, c, inner, paint)
            paint.color = core
            paint.textSize = size * 0.4f
            paint.textAlign = Paint.Align.CENTER
            paint.isFakeBoldText = true
            val initial = contact.name.trim().firstOrNull()?.uppercase(Locale.getDefault()) ?: "?"
            canvas.drawText(initial, c, c - (paint.descent() + paint.ascent()) / 2, paint)
        }
        // L'anneau et son halo.
        paint.style = Paint.Style.STROKE
        paint.color = line
        paint.alpha = 70
        paint.strokeWidth = ring * 3.5f
        canvas.drawCircle(c, c, c - ring * 2f, paint)
        paint.alpha = 255
        paint.strokeWidth = ring
        canvas.drawCircle(c, c, c - ring * 2f, paint)
        return bitmap
    }

    private companion object {
        val SLOTS = listOf(
            Triple(R.id.contacts_slot_0, R.id.contacts_photo_0, R.id.contacts_name_0),
            Triple(R.id.contacts_slot_1, R.id.contacts_photo_1, R.id.contacts_name_1),
            Triple(R.id.contacts_slot_2, R.id.contacts_photo_2, R.id.contacts_name_2),
            Triple(R.id.contacts_slot_3, R.id.contacts_photo_3, R.id.contacts_name_3),
            Triple(R.id.contacts_slot_4, R.id.contacts_photo_4, R.id.contacts_name_4),
            Triple(R.id.contacts_slot_5, R.id.contacts_photo_5, R.id.contacts_name_5),
        )

        val SAMPLE = listOf("Maman", "Léa", "Tom", "Paul", "Bureau").map { FavoriteContact(it, "0600000000", null) }
    }
}
