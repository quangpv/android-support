package android.support.viewmodel

import android.os.Bundle
import android.os.Parcelable
import androidx.annotation.CallSuper

interface SavedStateCreatable {
    fun onCreate(savedState: Bundle?) {}
    fun onSavedState(): Bundle
}

interface ViewModelStateSaveAble : SavedStateCreatable {
    companion object {
        private const val KEY_SAVED = "saved:viewModel"
    }

    @CallSuper
    override fun onCreate(savedState: Bundle?) {
        savedState?.getParcelable<Parcelable>(KEY_SAVED)?.also {
            restoreState(it)
        }
    }

    @CallSuper
    override fun onSavedState(): Bundle {
        return Bundle().also { it.putParcelable(KEY_SAVED, saveState()) }
    }

    fun saveState(): Parcelable

    fun restoreState(savedState: Parcelable)
}
