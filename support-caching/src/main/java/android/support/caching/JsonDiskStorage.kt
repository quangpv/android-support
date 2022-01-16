package android.support.caching

import android.content.Context
import android.support.caching.sqlite.LiveDataTrackingContainer
import androidx.core.content.edit
import androidx.lifecycle.LiveData
import java.util.concurrent.locks.ReentrantLock
import kotlin.reflect.KClass

class JsonDiskStorage<T : Any>(
    private val name: String,
    private val context: Context,
    private val trackingContainer: LiveDataTrackingContainer,
    private val parser: Parser,
    private val type: KClass<T>,
    private val keyOf: (T) -> String
) : DiskStorage<T> {
    private val mShared = context.getSharedPreferences(name, Context.MODE_PRIVATE)
    private val mTransactionLock = ReentrantLock()

    private fun notifyDatasetChange() {
        if (!mTransactionLock.isLocked) trackingContainer.onCommitSucceed(name)
    }

    override suspend fun findAll(): List<T> {
        return withIO {
            mShared.all.values.map { parse(it) }
        }
    }

    private fun parse(it: Any?): T {
        return parser.fromJson(it.toString(), type.java)!!
    }

    private fun parseNullable(it: Any?): T? {
        it ?: return null
        val json = it.toString()
        if (json.isBlank()) return null
        return parser.fromJson(json, type.java)
    }

    override suspend fun findAllWith(options: QueryOptions): PagingList<T> {
        error("Not support ${javaClass.name} yet!")
    }

    override suspend fun findAllByIds(ids: List<String>): List<T> {
        return withIO {
            ids.mapNotNull { parseNullable(mShared.getString(it, "")) }
        }
    }

    override suspend fun findById(id: String): T? {
        return withIO { parseNullable(mShared.getString(id, "")) }
    }

    override fun getAll(): LiveData<List<T>> {
        return trackingContainer.create(name) { findAll() }
    }

    override fun getById(id: String): LiveData<T> {
        return trackingContainer.create(name) {
            parseNullable(mShared.getString(id, ""))
        }
    }

    override suspend fun saveAll(items: Collection<T>?) {
        items ?: return
        withIO {
            mShared.edit {
                items.forEach {
                    putString(keyOf(it), parser.toJson(it))
                }
            }
            notifyDatasetChange()
        }
    }

    override suspend fun remove(id: String?) {
        id ?: return
        withIO {
            mShared.edit { remove(id) }
            notifyDatasetChange()
        }
    }

    override suspend fun save(item: T) {
        val id = keyOf(item)
        withIO {
            mShared.edit { putString(id, parser.toJson(item)) }
            notifyDatasetChange()
        }
    }

    override suspend fun removeAll() {
        withIO {
            mShared.edit { this.clear() }
            notifyDatasetChange()
        }
    }

    override suspend fun count(): Int {
        return withIO { mShared.all.size }
    }

    override suspend fun transaction(transaction: suspend DiskWriteable<T>.() -> Unit) {
        mTransactionLock.lock()

        val result = kotlin.runCatching {
            transaction()
        }
        mTransactionLock.unlock()

        if (!result.isFailure) {
            notifyDatasetChange()
        } else {
            throw result.exceptionOrNull()!!
        }
    }

    override fun getAllWith(options: QueryOptions): LiveData<PagingList<T>> {
        error("Not support ${javaClass.name} yet!")
    }

}