package com.android.support.repo.random

import android.support.di.Inject
import android.support.di.ShareScope
import com.android.support.datasource.local.RandomLocalSource
import com.android.support.model.entity.RandomEntity
import kotlin.random.Random

@Inject(ShareScope.Fragment)
class GenerateRandomRepo(
    private val randomLocalSource: RandomLocalSource,
) {

    suspend operator fun invoke(folderId: Int) {
        val size = 20
        val offset = folderId * size
        val data = (offset until offset + size).map {
            RandomEntity(
                it.toString(),
                "Tên $it - ${Random.nextDouble()}",
                "Trạng thái - ${(it % 3)}",
            )
        }
        randomLocalSource.saveAll(data, folderId.toString())
        randomLocalSource.invalidate()
    }
}