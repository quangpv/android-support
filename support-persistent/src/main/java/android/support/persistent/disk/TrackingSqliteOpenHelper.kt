package android.support.persistent.disk

import android.annotation.SuppressLint
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import androidx.core.database.sqlite.transaction

class TrackingSqliteOpenHelper(
    context: Context,
    dbName: String,
    version: Int,
    private val debugging: Boolean = false
) : SQLiteOpenHelper(context, dbName, null, version) {

    private var mReadableDatabase: SQLiteDatabase? = null
    private var mWriteableDatabase: SQLiteDatabase? = null

    private val autoCloser = TrackingDatabaseAutoCloser(5000, debugging) {
        synchronized(this) {
            val db = mWriteableDatabase ?: mReadableDatabase
            if (db != null && db.isOpen) {
                db.close()
                mWriteableDatabase = null
                mReadableDatabase = null
            }
        }
    }

    private val isOpen: Boolean
        get() {
            if (mWriteableDatabase?.isOpen == true) return true
            if (mReadableDatabase?.isOpen == true) return true
            return false
        }

    override fun getWritableDatabase(): SQLiteDatabase {
        if (mWriteableDatabase == null) error("Writeable database is not initialized yet!")
        return mWriteableDatabase!!
    }

    override fun getReadableDatabase(): SQLiteDatabase {
        if (mReadableDatabase == null) error("Readable database is not initialized yet!")
        return mReadableDatabase!!
    }

    private fun checkout() {
        autoCloser.decreaseCount { isOpen }
    }

    override fun onOpen(db: SQLiteDatabase) {
    }

    @SuppressLint("Recycle")
    private fun SQLiteDatabase.getAllTables(): List<String> {
        return rawQuery(
            "SELECT name FROM sqlite_master WHERE type ='table' AND name NOT LIKE 'sqlite_%' AND name != 'android_metadata'",
            arrayOf()
        ).readList { it.getString(0) }
    }

    override fun onCreate(db: SQLiteDatabase?) {}

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        cleanUpDatabase(db)
    }

    override fun onDowngrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db ?: return
        cleanUpDatabase(db)
    }

    private fun cleanUpDatabase(db: SQLiteDatabase) {
        db.transaction {
            getAllTables().forEach { db.execSQL("DROP TABLE IF EXISTS $it") }
        }
    }

    suspend fun withWriteable(
        onSuccess: (() -> Unit)? = null,
        function: suspend SQLiteDatabase.() -> Unit
    ) {
        autoCloser.increaseCount()
        val writable = super.getWritableDatabase()
        if (writable.inTransaction()) {
            error("The new transaction should not in another transaction")
        }
        synchronized(this) { mWriteableDatabase = writable }
        with(writable) {
            var success = false
            beginTransaction()
            try {
                function()
                setTransactionSuccessful()
                success = true
            } finally {
                endTransaction()
                if (success) onSuccess?.invoke()
                checkout()
            }
        }
    }

    suspend fun <T> withReadable(function: suspend SQLiteDatabase.() -> T): T {
        autoCloser.increaseCount()
        val readable = super.getReadableDatabase()
        synchronized(this) { mReadableDatabase = readable }
        with(readable) {
            try {
                return function(this)
            } catch (e: Throwable) {
                if (debugging) {
                    e.printStackTrace()
                }
                throw e
            } finally {
                checkout()
            }
        }
    }
}