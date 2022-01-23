package android.support.persistent.disk

import java.text.Normalizer

interface SearchableSerializer {
    fun serialize(value: Any?): String
}

class SearchableSerializerImpl(private val adapter: Adapter = EmptyAdapter) : SearchableSerializer {
    private val accentRegex = Regex("[^\\p{ASCII}]")

    override fun serialize(value: Any?): String {
        value ?: return ""
        return StringBuilder().appendSearchable(value).toString()
    }

    private fun StringBuilder.appendSearchable(obj: Any): StringBuilder {
        when (obj) {
            is Number -> append(obj.toString()).append(" ")

            is Char, is CharSequence -> {
                append(trimAccent(obj)).append(" ")
            }

            is Collection<*> -> obj.forEach {
                if (it != null) appendSearchable(it)
            }
            is Array<*> -> obj.forEach {
                if (it != null) appendSearchable(it)
            }
            is Map<*, *> -> {
                obj.values.forEach {
                    if (it != null) appendSearchable(it)
                }
            }

            else -> {
                val clazz = obj.javaClass
                if (clazz.isPrimitive || clazz.isEnum) {
                    append(obj.toString()).append(" ")
                    return this
                }
                val adapterValue = adapter.serialize(obj)

                if (adapterValue != null) {
                    append(trimAccent(adapterValue)).append(" ")
                    return this
                }

                val fields = clazz.declaredFields
                if (fields.isEmpty()) {
                    append(obj.toString()).append(" ")
                    return this
                }
                fields.forEach {
                    it.isAccessible = true
                    val value = it.get(obj)
                    if (value != null) {
                        appendSearchable(value)
                    }
                    it.isAccessible = false
                }
            }
        }
        return this
    }

    private fun trimAccent(obj: Any): String {
        return Normalizer
            .normalize(obj.toString(), Normalizer.Form.NFD)
            .replace(accentRegex, "")
    }

    interface Adapter {
        fun serialize(obj: Any): String?
    }

    object EmptyAdapter : Adapter {
        override fun serialize(obj: Any): String? {
            return null
        }
    }
}
