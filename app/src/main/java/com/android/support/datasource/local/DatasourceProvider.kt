package com.android.support.datasource.local

import android.content.Context
import android.support.di.Inject
import android.support.di.ShareScope
import android.support.persistent.disk.DiskStorageFactory
import android.support.persistent.disk.SearchStrategy
import android.support.persistent.disk.StorageOptions
import com.android.support.model.entity.RandomEntity

@Inject(ShareScope.Singleton)
class DatasourceProvider(private val context: Context) {
    private val factory = DiskStorageFactory(context, version = 2, debug = true)

    val testDao by lazy {
        factory.create(
            StorageOptions(
                "test_name", RandomEntity::class,
                keyOf = { id },
                groupBy = { status },
                searchStrategy = SearchStrategy.Text()
            )
        )
    }
}