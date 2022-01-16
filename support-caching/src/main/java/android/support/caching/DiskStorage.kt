package android.support.caching

import androidx.lifecycle.LiveData

interface DiskStorage<T> : DiskReadable<T>, DiskWriteable<T> {
    fun getAllWith(options: QueryOptions): LiveData<PagingList<T>>
    fun getAll(): LiveData<List<T>>
    fun getById(id: String): LiveData<T>

    suspend fun transaction(transaction: suspend DiskWriteable<T>.() -> Unit)
}

interface DiskReadable<T> {
    suspend fun findAll(): List<T>
    suspend fun findAllWith(options: QueryOptions): PagingList<T>
    suspend fun findAllByIds(ids: List<String>): List<T>
    suspend fun findById(id: String): T?

    suspend fun count(): Int
}

interface DiskWriteable<T> {
    suspend fun save(item: T)
    suspend fun saveAll(items: Collection<T>?)
    suspend fun remove(id: String?)
    suspend fun removeAll()
}

class QueryOptions(
    val page: Int = -1,
    val size: Int = -1,
    val groupBy: String? = null,
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

data class PagingList<T>(
    val data: List<T>,
    val page: Int,
    val size: Int,
    val total: Int
)