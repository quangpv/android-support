package com.android.support.repo.random

import android.support.di.Inject
import android.support.di.ShareScope
import com.android.support.datasource.local.RandomLocalSource
import com.android.support.datasource.remote.RandomApi

@Inject(ShareScope.Fragment)
class GenerateRandomRepo(
    private val randomLocalSource: RandomLocalSource,
    private val randomApi: RandomApi,
) {

    suspend operator fun invoke(folderId: Int) {
        val size = 20
        val data = randomApi.generate(folderId, size).await()
        randomLocalSource.saveAll(data, folderId.toString())
        randomLocalSource.invalidate()
    }
}