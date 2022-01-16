package android.support.viewmodel

import android.view.View
import androidx.annotation.CallSuper
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel

interface ViewModelRegistrable {
    @CallSuper
    fun registry(viewModel: ViewModel) {
        val tagId = R.id.tag_registry_view_model

        val callback: (View) -> Unit = { container ->
            var register = container.getTag(tagId) as? ViewModelRegister
            if (register == null) {
                register = ViewModelRegister()
                container.setTag(tagId, register)
            }

            val isViewModelRegistered = register.isRegistered(viewModel.javaClass.name)
            if (!isViewModelRegistered) {
                onRegistryViewModel(viewModel)
                onReRegistryViewModel(viewModel)
                register.save(viewModel.javaClass.name)
            } else {
                onReRegistryViewModel(viewModel)
            }
        }
        when (this) {
            is FragmentActivity -> ViewModelRegister.ofActivity(this, callback)
            is Fragment -> ViewModelRegister.ofFragment(this, callback)
            else -> error("${this.javaClass.name} should be Activity or Fragment")
        }
    }

    fun onRegistryViewModel(viewModel: ViewModel)
    fun onReRegistryViewModel(viewModel: ViewModel) {}

    private class ViewModelRegister {
        private val mCached = hashMapOf<String, Boolean>()

        fun isRegistered(name: String): Boolean {
            return mCached.containsKey(name)
        }

        fun save(name: String) {
            mCached[name] = true
        }

        companion object {
            fun ofActivity(
                activity: FragmentActivity,
                callback: (View) -> Unit
            ) {
                return callback(activity.findViewById(android.R.id.content))
            }

            fun ofFragment(fragment: Fragment, callback: (View) -> Unit) {
                val observer = Observer<LifecycleOwner> { t ->
                    t?.lifecycle?.addObserver(object : DefaultLifecycleObserver {
                        override fun onCreate(owner: LifecycleOwner) {
                            owner.lifecycle.removeObserver(this)
                            callback(fragment.requireView())
                        }

                        override fun onDestroy(owner: LifecycleOwner) {
                            owner.lifecycle.removeObserver(this)
                        }
                    })
                }
                fragment.viewLifecycleOwnerLiveData.observe(fragment, observer)
            }
        }
    }
}