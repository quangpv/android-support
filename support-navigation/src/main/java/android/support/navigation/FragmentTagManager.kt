package android.support.navigation

import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable
import androidx.fragment.app.Fragment
import kotlin.reflect.KClass

class FragmentTagManager {
    companion object {
        private const val KEY_TAG = "fragment:manager:tag"
    }

    private val mTags = arrayListOf<FragmentTag>()

    private val String.tagId: Long?
        get() = split(":").lastOrNull()?.toLong()

    private val String.isSingle: Boolean
        get() = split(":").let { it[it.size - 2] == "single" }

    private fun findTagId(kClass: KClass<out Fragment>): Long? {
        return mTags.findLast { (it.fragmentClass == kClass) && it.tag.isSingle }?.tag?.tagId
    }

    fun getTagId(fragment: Fragment?): Long {
        return getTagId(fragment?.javaClass?.kotlin)
    }

    fun getTagId(fragmentClazz: KClass<out Fragment>?): Long {
        return fragmentClazz?.let { findTagId(it) } ?: System.currentTimeMillis()
    }

    fun save(tag: String, kClass: KClass<out Fragment>) {
        if (mTags.find { it.tag == tag } != null) return
        mTags.add(FragmentTag(tag, kClass))
//        Log.e("TAG", mTags.joinToString("\n") { it.tag })
    }

    fun saveState(state: Bundle) {
        state.putParcelableArrayList(KEY_TAG, mTags)
    }

    fun restoreState(saved: Bundle) {
        val tags = saved.getParcelableArrayList<FragmentTag>(KEY_TAG).orEmpty()
        mTags.addAll(tags)
    }

    fun remove(clazz: KClass<out Fragment>) {
        mTags.find { it.fragmentClass == clazz }
            ?.also { mTags.remove(it) }
    }

    class FragmentTag(
        val tag: String,
        val fragmentClass: KClass<out Fragment>
    ) : Parcelable {
        constructor(parcel: Parcel) : this(
            parcel.readString() ?: "",
            FragmentTag::class.java.classLoader!!.loadClass(parcel.readString())
                .asSubclass(Fragment::class.java).kotlin
        )

        override fun writeToParcel(parcel: Parcel, flags: Int) {
            parcel.writeString(tag)
            parcel.writeString(fragmentClass.java.name)
        }

        override fun describeContents(): Int {
            return 0
        }

        companion object CREATOR : Parcelable.Creator<FragmentTag> {
            override fun createFromParcel(parcel: Parcel): FragmentTag {
                return FragmentTag(parcel)
            }

            override fun newArray(size: Int): Array<FragmentTag?> {
                return arrayOfNulls(size)
            }
        }
    }
}