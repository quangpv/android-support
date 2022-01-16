package android.support.caching.sqlite

import android.database.sqlite.SQLiteDatabase
import android.support.caching.DiskWriteable
import com.google.gson.Gson
import java.util.*

class SqliteDiskWritable<T>(
    private val tableName: String,
    private val converter: Gson,
    private val keyOf: (T) -> String,
    private val groupOf: (T) -> String,
    private val databaseRef: () -> SQLiteDatabase,
) : DiskWriteable<T> {
    private val database get() = databaseRef()

    private fun insertOrReplace(data: DataTable) {
        database.insertWithOnConflict(
            tableName, null,
            data.buildParams(),
            SQLiteDatabase.CONFLICT_REPLACE
        )
    }

    override suspend fun save(item: T) {
        val content = converter.toJson(item)
        val now = Date().time
        insertOrReplace(
            DataTable(keyOf(item), content, groupOf(item), now)
        )
    }

    override suspend fun saveAll(items: Collection<T>?) {
        items ?: return
        val contents = items.map {
            DataTable(keyOf(it), converter.toJson(it), groupOf(it), Date().time)
        }
        contents.forEach { data ->
            insertOrReplace(data)
        }
    }

    override suspend fun remove(id: String?) {
        id ?: return
        database.delete(tableName, "id=?", arrayOf(id))
    }

    override suspend fun removeAll() {
        database.delete(tableName, "", emptyArray())
    }
}
