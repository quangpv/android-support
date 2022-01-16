// Do not rename this package
// This using for access module member protected
// androidx.lifecycle
package androidx.lifecycle


private const val DEFAULT_KEY = "androidx.lifecycle.ViewModelProvider.DefaultKey"

fun ViewModel.isShared(owner: ViewModelStoreOwner): Boolean {
    val modelClass = javaClass
    return owner.viewModelStore.get(DEFAULT_KEY + ":" + modelClass.canonicalName) != null
}

