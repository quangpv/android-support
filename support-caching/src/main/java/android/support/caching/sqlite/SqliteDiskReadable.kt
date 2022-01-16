package android.support.caching.sqlite

import android.annotation.SuppressLint
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.support.caching.DiskReadable
import android.support.caching.PagingList
import android.support.caching.QueryOptions
import androidx.annotation.WorkerThread
import com.google.gson.Gson

@SuppressLint("Recycle")
class SqliteDiskReadable<T>(
    private val tableName: String,
    private val clazz: Class<T>,
    private val converter: Gson,
    private val databaseRef: () -> SQLiteDatabase,
) : DiskReadable<T> {
    private val database get() = databaseRef()

    private fun <T> Cursor.readListOrEmpty(mapping: (Cursor) -> T?): List<T> {
        return try {
            readList(mapping)
        } catch (e: Throwable) {
            return emptyList()
        }
    }

    private fun <T> Cursor.readOneOrNull(mapping: (Cursor) -> T?): T? {
        return try {
            readOne(mapping)
        } catch (e: Throwable) {
            return null
        }
    }

    @WorkerThread
    private fun <T> select(
        query: String = "",
        vararg args: String,
        mapFunc: (DataTable) -> T
    ): List<T> {
        return database.getCursor(query, args).readListOrEmpty { mapFunc(DataTable.read(it)) }
    }

    @WorkerThread
    private fun <T> selectOne(
        query: String = "",
        vararg args: String,
        mapFunc: (DataTable) -> T
    ): T? {
        return database.getCursor("$query limit 1", args)
            .readOneOrNull { mapFunc(DataTable.read(it)) }
    }

    private fun SQLiteDatabase.getCursor(query: String, args: Array<out String>): Cursor {
        return rawQuery("select * from $tableName $query", args)
    }

    override suspend fun findAll(): List<T> {
        return select { deserialize(it) }
    }

    override suspend fun findAllWith(options: QueryOptions): PagingList<T> {
        val queryBuilder = StringBuilder()
        if (options.groupBy != null) {
            queryBuilder.append("where groupBy='${options.groupBy}'")
        }
        if (options.sortBy != QueryOptions.Sort.None) {
            queryBuilder.append(" order by updateAt ${options.sortBy.name.lowercase()}")
        }
        if (options.size > 0) {
            val pageLimit = options.size
            val pageOffset = if (options.page <= 1) 0 else (options.page - 1) * pageLimit
            queryBuilder.append(" limit $pageLimit offset $pageOffset")
        }
        val query = queryBuilder.toString()
        var total = 0

        val data = database.rawQuery(
            "select *, COUNT() OVER() AS totalCount from $tableName $query",
            emptyArray()
        ).readListOrEmpty {
            val totalIndex = it.getColumnIndex("totalCount")
            val data = DataTable.read(it)
            total = it.getInt(totalIndex)
            deserialize(data)
        }
        return PagingList(data, options.page, options.size, total)
    }

    private fun deserialize(data: DataTable): T {
        return converter.fromJson(data.jsonContent, clazz)
    }

    override suspend fun findAllByIds(ids: List<String>): List<T> {
        return select("where id in (${ids.joinToString()})") {
            deserialize(it)
        }
    }

    override suspend fun findById(id: String): T? {
        return selectOne("where id=?", id) {
            deserialize(it)
        }
    }

    override suspend fun count(): Int {
        return database.rawQuery("select count(*) from $tableName", emptyArray())
            .readOneOrNull { it.getInt(0) } ?: 0
    }
}