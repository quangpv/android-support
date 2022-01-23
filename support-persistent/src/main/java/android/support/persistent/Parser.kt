package android.support.persistent

import java.lang.reflect.Type

interface Parser {
    fun <T> fromJson(string: String?, type: Class<T>): T?
    fun <T> fromJson(string: String?, type: Type): T?
    fun <T> toJson(value: T?): String
}