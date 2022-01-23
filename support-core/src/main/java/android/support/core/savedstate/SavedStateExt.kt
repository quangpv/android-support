//Do not change this package
//androidx.lifecycle
package androidx.lifecycle

import android.os.Bundle
import androidx.savedstate.SavedStateRegistry

internal val SavedStateHandle.saveStateProvider get() = savedStateProvider()

internal fun createSaveStateHandle(
    registry: SavedStateRegistry, lifecycle: Lifecycle, key: String
): SavedStateHandle {
    return SavedStateHandleController.create(registry, lifecycle, key, Bundle()).handle
}