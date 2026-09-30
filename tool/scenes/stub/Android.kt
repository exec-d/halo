// Le peu d'Android hors android.graphics dont le fond a besoin pour compiler.
@file:Suppress("unused", "UNUSED_PARAMETER")

package android.content

open class Context {
    fun getColor(id: Int): Int = 0
}
