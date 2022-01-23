package android.support.persistent.disk

import android.content.Context
import android.support.persistent.Parser
import android.support.persistent.cache.GsonParser

class DiskStorageFactory(
    private val context: Context,
    private val fileName: String = "disk_storage",
    version: Int = 1,
    debug: Boolean = false,
    private val converter: Parser = GsonParser()
) {
    private val sqlite = TrackingSqliteOpenHelper(context, fileName, version, debug)
    private val liveDataTrackingContainer = LiveDataTrackingContainer(debug)

    fun <T : Any> create(options: StorageOptions<T>): DiskStorage<T> {
        if (options.name.isBlank()) error("Name should not be blank")
        val name = options.name.replace(Regex("\\s+"), "_")
        return SqliteDiskStorage(
            tableName = name,
            sqlite = sqlite,
            container = liveDataTrackingContainer,
            clazz = options.clazz.java,
            converter = converter,
            keyOf = options.keyOf,
            groupOf = options.groupBy ?: options.keyOf,
            searchStrategy = options.searchStrategy,
        )
    }
}