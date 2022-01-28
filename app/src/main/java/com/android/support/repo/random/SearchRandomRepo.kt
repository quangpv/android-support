package com.android.support.repo.random

import android.support.core.livedata.post
import android.support.di.Inject
import android.support.di.ShareScope
import androidx.lifecycle.MutableLiveData
import com.android.support.datasource.local.RandomLocalSource
import com.android.support.helper.ShareIOScope
import com.android.support.model.entity.RandomEntity
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach


@Inject(ShareScope.Fragment)
class SearchRandomRepo(
    private val randomLocalSource: RandomLocalSource,
    private val shareIOScope: ShareIOScope,
) {
    private var mSearchOptions: SearchOptions? = null

    val result = MutableLiveData<List<RandomEntity>>()

    init {
        randomLocalSource.invalidate.onEach {
            doSearch()
        }.launchIn(shareIOScope)
    }

    suspend operator fun invoke(key: String, folderId: Int) {
        val options = SearchOptions(key, folderId.toString())
        if (options.isDiff(mSearchOptions)) {
            mSearchOptions = options
            doSearch()
        }
    }

    private suspend fun doSearch() {
        val options = mSearchOptions ?: return
        val data = randomLocalSource.findAllBy(options.key, options.folderId)
        result.post(data)
    }

    class SearchOptions(val key: String, val folderId: String) {
        fun isDiff(options: SearchOptions?): Boolean {
            options ?: return true
            return key != options.key || folderId != options.key
        }
    }
}