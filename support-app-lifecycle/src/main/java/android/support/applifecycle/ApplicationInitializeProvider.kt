package android.support.applifecycle

import android.content.ContentProvider
import android.content.ContentValues
import android.database.Cursor
import android.net.Uri

class ApplicationInitializeProvider : ContentProvider() {
    override fun onCreate(): Boolean {
        val context = context ?: error("Context can not be null")
        ApplicationLifecycleOwner.init(context)
        return true
    }

    override fun query(
        uri: Uri,
        projection: Array<out String>?,
        selection: String?,
        selectionArgs: Array<out String>?,
        sortOrder: String?
    ): Cursor? {
        error("Not allowed!")
    }

    override fun getType(uri: Uri): String? {
        error("Not allowed!")
    }

    override fun insert(uri: Uri, values: ContentValues?): Uri? {
        error("Not allowed!")
    }

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?): Int {
        error("Not allowed!")
    }

    override fun update(
        uri: Uri,
        values: ContentValues?,
        selection: String?,
        selectionArgs: Array<out String>?
    ): Int {
        error("Not allowed!")
    }
}