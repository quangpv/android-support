package android.support.persistent.disk

import android.support.persistent.Parser
import androidx.lifecycle.LiveData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext


class SqliteDiskStorage<T>(
    private val tableName: String,
    private val sqlite: TrackingSqliteOpenHelper,
    private val container: LiveDataTrackingContainer,
    private val clazz: Class<T>,
    private val converter: Parser,
    private val keyOf: (T) -> String,
    private val groupOf: (T) -> String,
    private val searchStrategy: SearchStrategy
) : DiskStorage<T> {
    private val readable = SqliteDiskReadable(tableName, clazz, converter, searchStrategy) {
        sqlite.readableDatabase
    }

    private val writable = SqliteDiskWritable(
        tableName, converter,
        keyOf, groupOf, searchStrategy
    ) {
        sqlite.writableDatabase
    }

    init {
        runBlocking {
            sqlite.withWriteable {
                execSQL(DataTable.queryCreate(tableName))
                searchStrategy.onOpenTable(tableName, this)
            }
        }
    }

    override suspend fun findAll(folder: String?): List<T> {
        return readTransaction { findAll(folder) }
    }

    override suspend fun findAllWith(options: QueryOptions): PagingList<T> {
        return readTransaction { findAllWith(options) }
    }

    override suspend fun findAllByIds(ids: List<String>): List<T> {
        return readTransaction { findAllByIds(ids) }
    }

    override suspend fun findById(id: String): T? {
        return readTransaction { findById(id) }
    }

    override suspend fun count(folder: String?): Int {
        return readTransaction { count(folder) }
    }

    override suspend fun save(item: T, folder: String?) {
        return writeTransaction { save(item, folder) }
    }

    override suspend fun saveAll(items: Collection<T>?, folder: String?) {
        return writeTransaction { saveAll(items, folder) }
    }

    override suspend fun remove(id: String?) {
        return writeTransaction { remove(id) }
    }

    override suspend fun removeAll(options: RemoveOptions?) {
        return writeTransaction { removeAll(options) }
    }

    override fun getAllWith(options: QueryOptions): LiveData<PagingList<T>> {
        return container.create(tableName) { findAllWith(options) }
    }

    override fun getAll(folder: String?): LiveData<List<T>> {
        return container.create(tableName) { findAll(folder) }
    }

    override fun getById(id: String): LiveData<T> {
        return container.create(tableName) { findById(id) }
    }

    override suspend fun transaction(transaction: suspend DiskWriteable<T>.() -> Unit) {
        writeTransaction(transaction)
    }

    private suspend fun <A> readTransaction(transaction: suspend DiskReadable<T>.() -> A): A {
        return withContext(Dispatchers.IO) {
            sqlite.withReadable { transaction(readable) }
        }
    }

    private suspend fun writeTransaction(transaction: suspend DiskWriteable<T>.() -> Unit) {
        withContext(Dispatchers.IO) {
            sqlite.withWriteable(onSuccess = { container.onCommitSucceed(tableName) }) {
                transaction(writable)
            }
        }
    }
}