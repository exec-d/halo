package dev.levilainpetit.wux.widgets

import android.content.Context
import android.net.Uri
import android.provider.ContactsContract.CommonDataKinds.Phone
import org.json.JSONArray
import java.io.File

/** Un contact favori : son nom, son numéro, et sa photo gardée par Halo. */
data class FavoriteContact(val name: String, val number: String, val photo: String?)

/**
 * Les contacts du widget Contacts favoris. Ils sont choisis avec le
 * sélecteur d'Android, qui ne donne à Halo que le contact touché (pas
 * d'accès au carnet d'adresses) ; Halo garde alors son nom, son numéro et
 * une copie de sa photo. La liste est écrite par Flutter (`contacts.list`).
 */
object FavoriteContacts {

    const val MAX = 6

    fun list(context: Context): List<FavoriteContact> {
        val json = NeonWidget.settings(context).getString("contacts.list", null) ?: return emptyList()
        return runCatching {
            val array = JSONArray(json)
            List(array.length()) { i ->
                val o = array.getJSONObject(i)
                FavoriteContact(o.getString("name"), o.getString("number"), o.optString("photo").takeIf { it.isNotBlank() })
            }
        }.getOrDefault(emptyList()).take(MAX)
    }

    /** « sms » pour écrire au toucher, sinon appeler. */
    fun sms(context: Context) = NeonWidget.settings(context).getString("contacts.action", null) == "sms"

    /** Lit le contact rendu par le sélecteur ; copie sa photo si elle est lisible. */
    fun read(context: Context, uri: Uri): Map<String, String>? {
        val projection = arrayOf(Phone.DISPLAY_NAME, Phone.NUMBER, Phone.PHOTO_THUMBNAIL_URI)
        val row = context.contentResolver.query(uri, projection, null, null, null)?.use { cursor ->
            if (!cursor.moveToFirst()) null else Triple(cursor.getString(0), cursor.getString(1), cursor.getString(2))
        } ?: return null
        val (name, number, thumbnail) = row
        if (number.isNullOrBlank()) return null
        val photo = thumbnail?.let { source ->
            runCatching {
                val dir = File(context.filesDir, "contacts").apply { mkdirs() }
                val file = File(dir, "${System.currentTimeMillis()}.jpg")
                context.contentResolver.openInputStream(Uri.parse(source))?.use { input -> file.outputStream().use { input.copyTo(it) } }
                file.takeIf { it.length() > 0 }?.absolutePath
            }.getOrNull()
        }
        return buildMap {
            put("name", name?.takeIf { it.isNotBlank() } ?: number)
            put("number", number)
            if (photo != null) put("photo", photo)
        }
    }
}
