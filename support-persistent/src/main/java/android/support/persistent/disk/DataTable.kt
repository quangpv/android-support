package android.support.persistent.disk

import android.content.ContentValues

data class DataTable(
    val id: String,
    val jsonContent: String,
    val searchContent: String,
    val groupBy: String,
    val folder: String,
    val updateAt: Long = 0
) {
    fun buildParams(): ContentValues {
        return ContentValues().also {
            it.put("id", id)
            it.put("jsonContent", jsonContent)
            it.put("searchContent", searchContent)
            it.put("folder", folder)
            it.put("groupBy", groupBy)
        }
    }

    companion object {
        fun queryCreate(tableName: String) = """
                CREATE TABLE IF NOT EXISTS $tableName (
                    id TEXT PRIMARY KEY,
                    jsonContent TEXT NOT NULL,
                    searchContent TEXT DEFAULT '',
                    groupBy TEXT NOT NULL,
                    folder TEXT DEFAULT '',
                    updateAt TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
                )
            """.trimIndent()

        fun queryCreateFullTextSearch(tableName: String): Array<String> {
            val indexTable = "${tableName}_index"
            val insertTrigger = "after_${tableName}_insert"
            val updateTrigger = "after_${tableName}_update"
            val deleteTrigger = "after_${tableName}_delete"
            return arrayOf(
                // Table indexing
                """
                CREATE VIRTUAL TABLE IF NOT EXISTS $indexTable USING fts4(
                    jsonContent UNINDEXED,
                    searchContent,
                    groupBy UNINDEXED,
                    folder UNINDEXED,
                    tokenize=simple);
                """.trimIndent(),

                // Trigger insert
                """
                CREATE TRIGGER IF NOT EXISTS $insertTrigger AFTER INSERT ON $tableName BEGIN
                  INSERT INTO $indexTable (rowid,jsonContent,searchContent,groupBy,folder)
                  VALUES(new.id,new.jsonContent,new.searchContent,new.groupBy,new.folder);
                END;
                """.trimIndent(),

                // Trigger update
                """
                CREATE TRIGGER IF NOT EXISTS $updateTrigger UPDATE OF jsonContent ON $tableName BEGIN
                  UPDATE $indexTable 
                  SET jsonContent = new.jsonContent 
                    AND searchContent = new.searchContent
                    AND groupBy = new.groupBy
                    AND folder = new.folder
                  WHERE rowid = old.id;
                END;
                """.trimIndent(),

                // Trigger delete
                """
                CREATE TRIGGER IF NOT EXISTS $deleteTrigger AFTER DELETE ON $tableName BEGIN
                    DELETE FROM $indexTable WHERE rowid = old.id;
                END;
                """.trimIndent()
            )
        }
    }
}