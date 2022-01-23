package android.support.persistent.disk

import android.annotation.SuppressLint
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.support.persistent.Parser
import androidx.annotation.WorkerThread

@SuppressLint("Recycle")
class SqliteDiskReadable<T>(
    private val tableName: String,
    private val clazz: Class<T>,
    private val converter: Parser,
    private val searchStrategy: SearchStrategy,
    private val databaseRef: () -> SQLiteDatabase,
) : DiskReadable<T> {
    private val database get() = databaseRef()
    private val queryBuilder = SearchQueryBuilder()

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
        mapFunc: (String) -> T
    ): List<T> {
        return database.getCursor(query, args).readListOrEmpty { mapFunc(it.getString(0)) }
    }

    private fun SQLiteDatabase.getCursor(query: String, args: Array<out String>): Cursor {
        return rawQuery("select jsonContent from $tableName $query", args)
    }

    override suspend fun findAll(folder: String?): List<T> {
        val query = if (!folder.isNullOrBlank()) "where folder='$folder'" else ""
        return select(query) { deserialize(it) }
    }

    override suspend fun findAllWith(options: QueryOptions): PagingList<T> {
        queryBuilder.use(searchStrategy)

        fun executeQuery(query: String): PagingList<T> {
            var total = 0
            val data = database.rawQuery(query, emptyArray()).readListOrEmpty {
                val data = it.getString(0)
                total = it.getInt(1)
                deserialize(data)
            }
            return PagingList(data, options.page, options.size, total)
        }

        val result = executeQuery(queryBuilder.build(tableName, options))

        if (!searchStrategy.accept(result)) {
            queryBuilder.use(searchStrategy.alternativeStrategy)
            return executeQuery(queryBuilder.build(tableName, options))
        }
        return result
    }

    private fun deserialize(data: String): T {
        return converter.fromJson(data, clazz)!!
    }

    override suspend fun findAllByIds(ids: List<String>): List<T> {
        return select("where id in (${ids.joinToString()})") {
            deserialize(it)
        }
    }

    override suspend fun findById(id: String): T? {
        return database.getCursor("where id=? limit 1", arrayOf(id))
            .readOneOrNull { deserialize(it.getString(0)) }
    }

    override suspend fun count(folder: String?): Int {
        val query = StringBuilder("select count(*) from $tableName")
        if (!folder.isNullOrBlank()) query.append(" folder='$folder'")
        return database.rawQuery(query.toString(), emptyArray())
            .readOneOrNull { it.getInt(0) } ?: 0
    }

    class SearchQueryBuilder {
        private var mStrategy: SearchStrategy = SearchStrategy.Default

        fun use(strategy: SearchStrategy) {
            mStrategy = strategy
        }

        fun build(tableName: String, options: QueryOptions): String {
            val queryBuilder = StringBuilder()
            var whereClauseExists = false
            var tableSearch = tableName

            if (!options.groupIn.isNullOrEmpty()) {
                queryBuilder.append("where groupBy in (${options.groupIn.joinToString { "'$it'" }})")
                whereClauseExists = true
            }

            if (!options.search.isNullOrBlank() && options.search.replace("%", "").isNotBlank()) {
                if (whereClauseExists) queryBuilder.append(" and")
                else queryBuilder.append(" where")
                tableSearch = mStrategy.getTableToSearch(tableName)
                queryBuilder.append(mStrategy.buildSearchQuery(tableSearch, options.search))
                whereClauseExists = true
            }

            if (!options.folder.isNullOrBlank()) {
                if (whereClauseExists) queryBuilder.append(" and")
                else queryBuilder.append(" where")
                queryBuilder.append(" folder='${options.folder}'")
            }

            if (options.sortBy != QueryOptions.Sort.None) {
                queryBuilder.append(" order by updateAt ${options.sortBy.name.lowercase()}")
            }

            if (options.size > 0) {
                val pageLimit = options.size
                val pageOffset = if (options.page <= 1) 0 else (options.page - 1) * pageLimit
                queryBuilder.append(" limit $pageLimit offset $pageOffset")
            }

            return "select jsonContent, COUNT() OVER() AS totalCount from $tableSearch $queryBuilder"
        }
    }
}