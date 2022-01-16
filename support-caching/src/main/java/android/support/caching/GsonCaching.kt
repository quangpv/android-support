package android.support.caching

import android.content.Context
import com.google.gson.Gson

class GsonCaching(
    context: Context,
) : Caching(context, object : Parser {
    private val gson = Gson()
    override fun <T> fromJson(string: String?, type: Class<T>): T? {
        return gson.fromJson<T>(string, type)
    }

    override fun <T> toJson(value: T?): String {
        return gson.toJson(value)
    }

}, DefaultSharePreferencesFactory())