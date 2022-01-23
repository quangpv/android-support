package android.support.persistent.disk

fun interface SqliteTransactionCallback {
    fun onCommitSucceed(tableName: String)
}