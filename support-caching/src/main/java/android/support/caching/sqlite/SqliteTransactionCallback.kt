package android.support.caching.sqlite

fun interface SqliteTransactionCallback {
    fun onCommitSucceed(tableName: String)
}