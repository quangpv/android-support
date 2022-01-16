package android.support.caching

import com.google.gson.Gson

interface Parser {
    fun <T> fromJson(string: String?, type: Class<T>): T?
    fun <T> toJson(value: T?): String
}


class GsonParser(private val parser: Gson = Gson()) : Parser {
    override fun <T> fromJson(string: String?, type: Class<T>): T? {
        return parser.fromJson(string, type)
    }

    override fun <T> toJson(value: T?): String {
        return parser.toJson(value)
    }

}