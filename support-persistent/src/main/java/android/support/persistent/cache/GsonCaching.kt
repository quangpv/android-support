package android.support.persistent.cache

import android.content.Context
import android.support.persistent.Parser
import com.google.gson.Gson
import java.lang.reflect.Type

class GsonCaching(
    context: Context,
) : Caching(context, GsonParser(), DefaultSharePreferencesFactory())

class GsonParser : Parser {
    private val gson = Gson()
    override fun <T> fromJson(string: String?, type: Class<T>): T? {
        return gson.fromJson<T>(string, type)
    }

    override fun <T> fromJson(string: String?, type: Type): T? {
        return gson.fromJson<T>(string, type)
    }

    override fun <T> toJson(value: T?): String {
        return gson.toJson(value)
    }
}