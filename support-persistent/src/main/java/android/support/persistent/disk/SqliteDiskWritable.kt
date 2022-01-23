package android.support.persistent.disk

import android.database.sqlite.SQLiteDatabase
import android.support.persistent.Parser

class SqliteDiskWritable<T>(
    private val tableName: String,
    private val converter: Parser,
    private val keyOf: (T) -> String,
    private val groupOf: (T) -> String,
    private val searchStrategy: SearchStrategy,
    private val databaseRef: () -> SQLiteDatabase
) : DiskWriteable<T> {
    private val database get() = databaseRef()

    private fun createDataTable(item: T, folder: String?): DataTable {
        val content = converter.toJson(item)
        return DataTable(
            id = keyOf(item),
            jsonContent = content,
            searchContent = searchStrategy.getSearchableContent(item),
            groupBy = groupOf(item),
            folder = folder.orEmpty()
        )
    }

    override suspend fun save(item: T, folder: String?) {
        val data = createDataTable(item, folder)
        database.insertWithOnConflict(
            tableName, null,
            data.buildParams(),
            SQLiteDatabase.CONFLICT_REPLACE
        )
    }

    override suspend fun saveAll(items: Collection<T>?, folder: String?) {
        items?.forEach { data -> save(data, folder) }
    }

    override suspend fun remove(id: String?) {
        id ?: return
        database.delete(tableName, "id=?", arrayOf(id))
    }

    override suspend fun removeAll(options: RemoveOptions?) {
        val clause = StringBuilder()
        val params = arrayListOf<String>()

        if (options != null) {
            var shouldAndClause = false
            if (options.folder != null) {
                clause.append(" folder=?")
                params.add(options.folder)
                shouldAndClause = true
            }
            if (options.expireTime != null) {
                val expireInMillis = options.expireTime.unit.toMillis(options.expireTime.time)
                val validDate = (System.currentTimeMillis() - expireInMillis) / 1000
                if (shouldAndClause) clause.append(" and")
                clause.append(" updateAt < datetime(?, 'unixepoch')")
                params.add(validDate.toString())
            }
        }
        database.delete(tableName, clause.toString(), params.toTypedArray())
    }
}
