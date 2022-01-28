package com.android.support.repo.random

import android.support.core.livedata.DistributionLiveData
import android.support.core.livedata.distributeBy
import android.support.core.livedata.mapNotNull
import android.support.di.Inject
import com.android.support.datasource.local.RandomLocalSource
import com.android.support.factory.RandomFactory
import com.android.support.model.ui.IRandom

@Inject
class FetchRandomByIdRepo(
    private val randomLocalSource: RandomLocalSource,
    private val randomFactory: RandomFactory,
) {
    val result = DistributionLiveData<IRandom>()

    operator fun invoke(id: String) {
        randomLocalSource.getById(id)
            .mapNotNull { randomFactory.create(it) }
            .distributeBy(result)
    }
}