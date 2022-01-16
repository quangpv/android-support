// Do not rename this package
// This using for access module member protected
// androidx.lifecycle
package androidx.lifecycle

import android.support.di.dependenceContext
import android.support.viewmodel.SavedStateCreatable
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryOwner

class SavableViewModelFactory(private val owner: SavedStateRegistryOwner) :
    ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val viewModel = dependenceContext.get(modelClass, owner as LifecycleOwner)
        when (viewModel) {
            is SavedStateCreatable -> onCreateSavedState(viewModel, owner)
        }
        return viewModel
    }

    class SavedStateRecreation : SavedStateRegistry.AutoRecreated {
        override fun onRecreated(owner: SavedStateRegistryOwner) {
            val store = (owner as ViewModelStoreOwner).viewModelStore
            if (store.keys().isEmpty()) return

            for (key in store.keys()) {
                val viewModel = store.get(key)
                if (viewModel is SavedStateCreatable) {
                    registrySavedState(viewModel, owner)
                }
            }
        }
    }

    private fun onCreateSavedState(viewModel: ViewModel, owner: SavedStateRegistryOwner) {
        viewModel as SavedStateCreatable
        val registry = owner.savedStateRegistry

        val savedState =
            if (registry.isRestored) registry.consumeRestoredStateForKey(getSavedStateKey(viewModel)) else null

        registrySavedState(viewModel, owner)

        viewModel.onCreate(savedState)
    }

    companion object {

        private fun getSavedStateKey(viewModel: Any): String {
            return "factory:saved:${viewModel.javaClass.name}"
        }

        private fun registrySavedState(viewModel: ViewModel, owner: SavedStateRegistryOwner) {
            viewModel as SavedStateCreatable

            val key = getSavedStateKey(viewModel)
            val registry = owner.savedStateRegistry

            registry.unregisterSavedStateProvider(key)
            registry.registerSavedStateProvider(key) {
                registry.unregisterSavedStateProvider(key)
                viewModel.onSavedState()
            }
            tryToAddReCreator(registry, owner.lifecycle)
        }

        private fun tryToAddReCreator(registry: SavedStateRegistry, lifecycle: Lifecycle) {
            val currentState = lifecycle.currentState
            if (currentState == Lifecycle.State.INITIALIZED || currentState.isAtLeast(Lifecycle.State.STARTED)) {
                registry.runOnNextRecreation(SavedStateRecreation::class.java)
            } else {
                lifecycle.addObserver(object : LifecycleEventObserver {
                    override fun onStateChanged(
                        source: LifecycleOwner,
                        event: Lifecycle.Event
                    ) {
                        if (event == Lifecycle.Event.ON_START) {
                            lifecycle.removeObserver(this)
                            registry.runOnNextRecreation(SavedStateRecreation::class.java)
                        }
                    }
                })
            }
        }
    }

}