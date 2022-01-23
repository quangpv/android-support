package com.android.support.datasource

import android.content.Context
import android.support.di.Inject
import android.support.di.ShareScope
import android.support.persistent.disk.DiskStorageFactory
import android.support.persistent.disk.SearchStrategy
import android.support.persistent.disk.StorageOptions
import com.android.support.model.TestEntity

@Inject(ShareScope.Singleton)
class DatasourceProvider(private val context: Context) {
    private val factory = DiskStorageFactory(context, version = 2, debug = true)

    val testDao by lazy {
        factory.create(
            StorageOptions(
                "test_name", TestEntity::class,
                keyOf = { id },
                groupBy = { status },
                searchStrategy = SearchStrategy.Text()
            )
        )
    }
}