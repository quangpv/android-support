package android.support.caching.sqlite

import android.database.sqlite.SQLiteDatabase
import android.support.caching.*
import androidx.lifecycle.LiveData
import com.google.gson.Gson
import kotlinx.coroutines.runBlocking


class SqliteDiskStorage<T>(
    private val tableName: String,
    private val sqlite: TrackingSqliteOpenHelper,
    private val container: LiveDataTrackingContainer,
    private val clazz: Class<T>,
    private val converter: Gson,
    private val keyOf: (T) -> String,
    private val groupOf: (T) -> String,
) : DiskStorage<T> {
    private val readable = SqliteDiskReadable(tableName, clazz, converter) {
        sqlite.readableDatabase
    }

    private val writable = SqliteDiskWritable(tableName, converter, keyOf, groupOf) {
        sqlite.writableDatabase
    }

    init {
        runBlocking {
            sqlite.withWriteable {
                execSQL(DataTable.queryCreate(tableName))
                insertWithOnConflict(
                    TrackingTable.NAME, null,
                    TrackingTable.paramsOf(tableName),
                    SQLiteDatabase.CONFLICT_IGNORE
                )
            }
        }
    }

    override suspend fun findAll(): List<T> {
        return readTransaction { findAll() }
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

    override suspend fun count(): Int {
        return readTransaction { count() }
    }

    override suspend fun save(item: T) {
        return writeTransaction { save(item) }
    }

    override suspend fun saveAll(items: Collection<T>?) {
        return writeTransaction { saveAll(items) }
    }

    override suspend fun remove(id: String?) {
        return writeTransaction { remove(id) }
    }

    override suspend fun removeAll() {
        return writeTransaction { removeAll() }
    }

    override fun getAllWith(options: QueryOptions): LiveData<PagingList<T>> {
        return container.create(tableName) { findAllWith(options) }
    }

    override fun getAll(): LiveData<List<T>> {
        return container.create(tableName) { findAll() }
    }

    override fun getById(id: String): LiveData<T> {
        return container.create(tableName) { findById(id) }
    }

    override suspend fun transaction(transaction: suspend DiskWriteable<T>.() -> Unit) {
        writeTransaction(transaction)
    }

    private suspend fun <A> readTransaction(transaction: suspend DiskReadable<T>.() -> A): A {
        return withIO {
            sqlite.withReadable { transaction(readable) }
        }
    }

    private suspend fun writeTransaction(transaction: suspend DiskWriteable<T>.() -> Unit) {
        withIO {
            sqlite.withWriteable(onSuccess = { container.onCommitSucceed(tableName) }) {
                transaction(writable)
            }
        }
    }
}