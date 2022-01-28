package com.android.support.factory

import android.support.di.Inject
import com.android.support.model.ui.IRandom
import com.android.support.model.entity.RandomEntity

@Inject
class RandomFactory {
    fun create(it: RandomEntity): IRandom {
        return object : IRandom {
            override val status: String get() = it.status
            override val name: String get() = it.name
            override val id: String get() = it.id
        }
    }
}