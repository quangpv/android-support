package android.support.persistent.cache

import android.content.Context
import android.content.SharedPreferences
import android.support.persistent.Parser
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

abstract class Caching(
    context: Context,
    val parser: Parser,
    factory: SharePreferencesFactory = DefaultSharePreferencesFactory()
) {
    private val mShared = factory.create(context)
    val shared: SharedPreferences get() = mShared

    open fun clear() {}

    inline fun <reified T> reference(key: String) = object : CacheProperty<T?> {

        private var mValue: T? = null

        override fun getValue(thisRef: Any, property: KProperty<*>): T? {
            if (mValue == null) mValue =
                parser.fromJson(shared.getString(key, ""), T::class.java)
            return mValue
        }

        override fun setValue(thisRef: Any, property: KProperty<*>, value: T?) {
            mValue = value
            shared.edit().putString(key, parser.toJson(value)).apply()
        }
    }

    fun string(key: String, def: String = ""): CacheProperty<String> =
        Primitive(get = { getString(key, def) ?: def }, set = { putString(key, it) })

    fun long(key: String, def: Long = 0L): CacheProperty<Long> =
        Primitive(get = { getLong(key, def) }, set = { putLong(key, it) })

    fun int(key: String, def: Int = 0): CacheProperty<Int> =
        Primitive(get = { getInt(key, def) }, set = { putInt(key, it) })

    fun float(key: String, def: Float = 0f): CacheProperty<Float> =
        Primitive(get = { getFloat(key, def) }, set = { putFloat(key, it) })

    fun boolean(key: String, def: Boolean = false): CacheProperty<Boolean> =
        Primitive(get = { getBoolean(key, def) }, set = { putBoolean(key, it) })

    fun double(key: String, def: Double = 0.0) = float(key, def.toFloat())

    interface CacheProperty<T> : ReadWriteProperty<Any, T>

    private inner class Primitive<T>(
        private val get: SharedPreferences.() -> T,
        private val set: SharedPreferences.Editor.(T) -> Unit
    ) : CacheProperty<T> {
        private var mValue: T? = null

        override fun getValue(thisRef: Any, property: KProperty<*>): T {
            if (mValue == null) mValue = get(mShared)
            return mValue!!
        }

        override fun setValue(thisRef: Any, property: KProperty<*>, value: T) {
            mValue = value
            mShared.edit().apply { set(value) }.apply()
        }
    }

}