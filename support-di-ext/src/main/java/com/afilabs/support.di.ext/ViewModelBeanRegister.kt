package com.afilabs.support.di.ext

import android.support.di.BeanNotFoundCallback
import android.support.di.BeanRegister
import android.support.di.ShareScope
import androidx.lifecycle.ViewModel

class ViewModelBeanRegister : BeanRegister {
    override fun <T> registry(clazz: Class<T>, notFoundCallback: BeanNotFoundCallback): Boolean {
        if (ViewModel::class.java.isAssignableFrom(clazz)) {
            notFoundCallback.onDefinitionNotFounded(
                clazz,
                clazz,
                ShareScope.None
            )
            return true
        }
        return false
    }
}