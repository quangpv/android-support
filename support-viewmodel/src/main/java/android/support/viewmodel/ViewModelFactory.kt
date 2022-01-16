package android.support.viewmodel

import android.support.di.dependenceContext
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class ViewModelFactory(private val owner: LifecycleOwner) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return dependenceContext.get(modelClass, owner)
    }
}