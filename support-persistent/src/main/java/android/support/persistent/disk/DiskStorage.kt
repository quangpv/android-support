package android.support.persistent.disk

import androidx.lifecycle.LiveData
import java.util.concurrent.TimeUnit
import kotlin.reflect.KClass

interface DiskStorage<T> : DiskReadable<T>, DiskWriteable<T> {
    fun getAllWith(options: QueryOptions): LiveData<PagingList<T>>
    fun getAll(folder: String? = null): LiveData<List<T>>
    fun getById(id: String): LiveData<T>

    suspend fun transaction(transaction: suspend DiskWriteable<T>.() -> Unit)
}

interface DiskReadable<T> {
    suspend fun findAll(folder: String? = null): List<T>
    suspend fun findAllWith(options: QueryOptions): PagingList<T>
    suspend fun findAllByIds(ids: List<String>): List<T>
    suspend fun findById(id: String): T?

    suspend fun count(folder: String? = null): Int
}

interface DiskWriteable<T> {
    suspend fun save(item: T, folder: String? = null)
    suspend fun saveAll(items: Collection<T>?, folder: String? = null)
    suspend fun remove(id: String?)
    suspend fun removeAll(options: RemoveOptions? = null)
}

class QueryOptions(
    val page: Int = -1,
    val size: Int = -1,
    val groupIn: Array<String>? = null,
    val folder: String? = null,
    val search: String? = null,
    val sortBy: Sort = Sort.None
) {
    enum class Sort {
        None,

        // Update time asc
        Asc,

        // Update time desc
        Desc
    }
}

class StorageOptions<T : Any>(
    val name: String,
    val clazz: KClass<T>,
    val keyOf: T.() -> String,
    val groupBy: (T.() -> String)? = null,
    val searchStrategy: SearchStrategy = SearchStrategy.Default
)

data class PagingList<T>(
    val data: List<T>,
    val page: Int,
    val size: Int,
    val total: Int
)

class RemoveOptions(
    val folder: String? = null,
    val expireTime: ExpireTime? = null
)

class ExpireTime(
    val time: Long,
    val unit: TimeUnit
)