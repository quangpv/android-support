package android.support.persistent.disk

import android.database.Cursor

fun <T> Cursor.readList(mapping: (Cursor) -> T?): List<T> {
    use {
        val data = ArrayList<T>()
        if (!moveToFirst()) {
            return data
        }
        while (true) {
            val result = mapping(this)
            if (result != null) data.add(result)
            if (!moveToNext()) break
        }
        return data
    }
}

fun <T> Cursor.readOne(mapping: (Cursor) -> T?): T? {
    use {
        if (moveToFirst()) {
            return mapping(this)
        }

        return null
    }
}