package com.android.support.datasource.remote

import android.support.di.InjectBy
import android.support.di.Injectable
import android.support.di.ShareScope
import com.android.support.helper.network.Async
import com.android.support.model.entity.RandomEntity
import retrofit2.http.GET
import kotlin.random.Random

@InjectBy(MockRandomApi::class, ShareScope.Singleton)
interface RandomApi : Injectable {
    @GET("")
    fun generate(index: Int, size: Int): Async<List<RandomEntity>>
}

class MockRandomApi : RandomApi {
    override fun generate(index: Int, size: Int): Async<List<RandomEntity>> {
        val offset = index * size
        val data = (offset until offset + size).map {
            RandomEntity(
                it.toString(),
                "Tên $it - ${Random.nextDouble()}",
                "Trạng thái - ${(it % 3)}",
            )
        }
        return MockAsync(data)
    }
}