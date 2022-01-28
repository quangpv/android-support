package com.android.support.repo.random

import android.support.di.Inject
import android.support.di.ShareScope
import android.support.persistent.disk.ExpireTime
import com.android.support.datasource.local.RandomLocalSource
import java.util.concurrent.TimeUnit

@Inject(ShareScope.Fragment)
class RemoveAllRandomByFolderRepo(
    private val randomLocalSource: RandomLocalSource,
) {

    suspend operator fun invoke(folderId: Int) {
        randomLocalSource.removeAll(
            folderId.toString(),
            ExpireTime(10, TimeUnit.SECONDS)
        )
        randomLocalSource.invalidate()
    }
}

@Inject(ShareScope.Fragment)
class RemoveRandomByIdRepo(
    private val randomLocalSource: RandomLocalSource,
) {
    suspend operator fun invoke(id: String) {
        randomLocalSource.remove(id)
        randomLocalSource.invalidate()
    }
}
