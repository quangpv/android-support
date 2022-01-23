package android.support.persistent.disk

import android.database.sqlite.SQLiteDatabase

sealed class SearchStrategy {
    internal open val alternativeStrategy: SearchStrategy get() = Default
    internal open fun onOpenTable(tableName: String, db: SQLiteDatabase) {}
    internal abstract fun <T> getSearchableContent(item: T): String

    internal abstract fun getTableToSearch(tableName: String): String

    internal abstract fun buildSearchQuery(tableName: String, search: String): String
    internal open fun accept(result: PagingList<*>): Boolean = true

    open class FullText(private val serializer: SearchableSerializer = SearchableSerializerImpl()) :
        SearchStrategy() {
        override fun onOpenTable(tableName: String, db: SQLiteDatabase) {
            DataTable.queryCreateFullTextSearch(tableName).forEach {
                db.execSQL(it)
            }
        }

        override fun <T> getSearchableContent(item: T): String {
            return serializer.serialize(item)
        }

        override fun getTableToSearch(tableName: String): String {
            return "${tableName}_index"
        }

        override fun buildSearchQuery(tableName: String, search: String): String {
            return " $tableName match '${search}'"
        }
    }

    object Default : SearchStrategy() {
        override fun <T> getSearchableContent(item: T): String {
            return ""
        }

        override fun getTableToSearch(tableName: String): String {
            return tableName
        }

        override fun buildSearchQuery(tableName: String, search: String): String {
            return " jsonContent like '$search'"
        }
    }

    class Text(private val serializer: SearchableSerializer = SearchableSerializerImpl()) :
        SearchStrategy() {
        override fun <T> getSearchableContent(item: T): String {
            return serializer.serialize(item)
        }

        override fun getTableToSearch(tableName: String): String {
            return tableName
        }

        override fun buildSearchQuery(tableName: String, search: String): String {
            return " searchContent like '$search'"
        }
    }

    class Complete(serializer: SearchableSerializer = SearchableSerializerImpl()) :
        FullText(serializer) {
        override val alternativeStrategy = Text(serializer)

        override fun accept(result: PagingList<*>): Boolean {
            return result.data.isNotEmpty()
        }
    }
}