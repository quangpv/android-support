package com.android.support.datasource.local

import android.support.di.Inject
import android.support.di.ShareScope
import android.support.persistent.disk.ExpireTime
import android.support.persistent.disk.QueryOptions
import android.support.persistent.disk.RemoveOptions
import androidx.lifecycle.LiveData
import com.android.support.model.entity.RandomEntity
import kotlinx.coroutines.flow.MutableSharedFlow

@Inject(ShareScope.Singleton)
class RandomLocalSource(
    private val datasourceProvider: DatasourceProvider,
) {
    private val dao = datasourceProvider.testDao
    val invalidate = MutableSharedFlow<Any>(0, 1)

    suspend fun findAllBy(key: String, folderId: String): List<RandomEntity> {
        return dao.findAllWith(
            QueryOptions(
                search = "%${key}%",
                folder = folderId
            )
        ).data
    }

    suspend fun removeAll(folderId: String, expireTime: ExpireTime) {
        dao.removeAll(RemoveOptions(
            folder = folderId,
            expireTime = expireTime
        ))
    }

    suspend fun invalidate() {
        this.invalidate.emit(Any())
    }

    fun getById(id: String): LiveData<RandomEntity> {
        return dao.getById(id)
    }

    suspend fun remove(id: String) {
        dao.remove(id)
    }

    suspend fun saveAll(data: List<RandomEntity>, folderId: String) {
        dao.saveAll(data, folderId)
    }
}