// Do not change this package
// androidx.lifecycle
package androidx.lifecycle

@Suppress("unchecked_cast")
internal fun <T : ViewModel> ViewModelStore.getOrPut(key: String, def: () -> T): T {
    var item = this.get(key) as? T
    if (item == null) {
        synchronized(ViewModelStore::class) {
            if (item == null) {
                item = def()
                put(key, item)
            }
        }
    }
    return item!!
}