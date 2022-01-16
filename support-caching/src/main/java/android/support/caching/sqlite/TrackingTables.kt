package android.support.caching.sqlite

import android.content.ContentValues
import android.database.Cursor

data class DataTable(
    val id: String,
    val jsonContent: String,
    val groupBy: String,
    val updateAt: Long
) {
    fun buildParams(): ContentValues {
        return ContentValues().also {
            it.put("id", id)
            it.put("jsonContent", jsonContent)
            it.put("groupBy", groupBy)
            it.put("updateAt", updateAt)
        }
    }

    companion object {
        fun queryCreate(tableName: String) = """
                CREATE TABLE IF NOT EXISTS $tableName (
                    id TEXT PRIMARY KEY,
                    jsonContent TEXT NOT NULL,
                    groupBy TEXT NOT NULL,
                    updateAt INTEGER
                )
            """.trimIndent()

        fun read(query: Cursor): DataTable {
            return DataTable(
                query.getString(0),
                query.getString(1),
                query.getString(2),
                query.getLong(3),
            )
        }
    }
}

data class TrackingTable(
    val tableName: String
) {
    companion object {
        const val NAME = "TrackingTable"

        fun queryCreate(): String {
            return """
                CREATE TABLE IF NOT EXISTS $NAME (
                    name TEXT PRIMARY KEY
                )
            """.trimIndent()
        }

        fun paramsOf(tableName: String): ContentValues {
            return ContentValues().also {
                it.put("name", tableName)
            }
        }
    }
}