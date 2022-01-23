package com.android.support.feature.home

import android.os.Bundle
import android.support.core.event.LiveDataStatusOwner
import android.support.core.event.WindowStatusOwner
import android.support.core.livedata.DistributionLiveData
import android.support.core.livedata.post
import android.support.core.route.open
import android.support.core.view.RecyclerAdapter
import android.support.core.view.RecyclerHolder
import android.support.core.view.bindingOf
import android.support.core.view.viewBinding
import android.support.di.Inject
import android.support.di.ShareScope
import android.support.persistent.disk.ExpireTime
import android.support.persistent.disk.QueryOptions
import android.support.persistent.disk.RemoveOptions
import android.support.viewmodel.launch
import android.support.viewmodel.viewModel
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModel
import androidx.recyclerview.widget.RecyclerView
import com.android.support.R
import com.android.support.app.AppFragment
import com.android.support.databinding.FragmentHomeBinding
import com.android.support.databinding.ItemTestBinding
import com.android.support.datasource.DatasourceProvider
import com.android.support.feature.MainNavigationActivity
import com.android.support.model.TestEntity
import com.android.support.navigation.Routing
import com.android.support.widget.SearchTopBarState
import com.android.support.widget.TopBarOwner
import java.util.concurrent.TimeUnit
import kotlin.random.Random

class HomeFragment : AppFragment(R.layout.fragment_home), TopBarOwner {
    private val viewModel by viewModel<HomeViewModel>()
    private val binding by viewBinding(FragmentHomeBinding::bind)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        topBar.setState(SearchTopBarState(R.string.title_home, viewModel.searchText) {
            viewModel.search(it)
        })

        binding.btnClick.setOnClickListener {
            viewModel.generate(binding.edtGroup.text.toString().toIntOrNull() ?: 0)
        }

        binding.btnSearch.setOnClickListener {
            viewModel.search(binding.edtGroup.text.toString().toIntOrNull() ?: 0)
        }
        binding.btnRemove.setOnClickListener {
            viewModel.remove(binding.edtGroup.text.toString().toIntOrNull() ?: 0)
        }

        viewModel.result.bind(Adapter()::submit)
    }

    private inner class Adapter : RecyclerAdapter<TestEntity>() {
        init {
            binding.rvList.adapter = this
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            val binding = parent.bindingOf(ItemTestBinding::inflate)
            return object : RecyclerHolder<TestEntity>(binding) {
                override fun bind(item: TestEntity) {
                    super.bind(item)
                    binding.txtName.text = item.name
                    binding.txtId.text = item.id
                    binding.txtStatus.text = item.status
                    itemView.setOnClickListener {
                        open<MainNavigationActivity>(Routing.TestDetail(item.id))
                    }
                }
            }
        }

    }

    class HomeViewModel(
        private val generateTestRepo: GenerateTestRepo,
        private val searchTestRepo: SearchTestRepo,
        private val removeTestRepo: RemoveTestRepo
    ) : ViewModel(), WindowStatusOwner by LiveDataStatusOwner() {
        var searchFolder: Int = 0
            private set
        var searchText: String = ""
            private set
        val result = searchTestRepo.result

        init {
            search(searchText)
        }

        fun generate(folderId: Int) = launch {
            generateTestRepo(folderId)
            searchTestRepo(searchText, folderId)
        }

        fun search(text: String) = launch(error = error) {
            searchText = text
            searchTestRepo(text, searchFolder)
        }

        fun search(folder: Int) = launch(error = error) {
            searchFolder = folder
            searchTestRepo(searchText, searchFolder)
        }

        fun remove(folder: Int) = launch(error = error) {
            searchFolder = folder
            removeTestRepo(folder)
            searchTestRepo(searchText, searchFolder)
        }
    }
}

@Inject(ShareScope.Fragment)
class SearchTestRepo(
    private val datasourceProvider: DatasourceProvider
) {
    private val testDatasource = datasourceProvider.testDao
    val result = DistributionLiveData<List<TestEntity>>()

    suspend operator fun invoke(key: String, folderId: Int) {
        val data = testDatasource.findAllWith(
            QueryOptions(
                search = "%$key%",
                folder = folderId.toString()
            )
        ).data
        result.post(data)
    }
}

@Inject(ShareScope.Fragment)
class RemoveTestRepo(
    private val datasourceProvider: DatasourceProvider
) {
    private val testDatasource = datasourceProvider.testDao

    suspend operator fun invoke(folderId: Int) {
        testDatasource.removeAll(
            RemoveOptions(
                folder = folderId.toString(),
                expireTime = ExpireTime(10, TimeUnit.SECONDS)
            )
        )
    }
}

@Inject(ShareScope.Fragment)
class GenerateTestRepo(
    private val datasourceProvider: DatasourceProvider
) {
    private val testDatasource = datasourceProvider.testDao

    suspend operator fun invoke(folderId: Int) {
        val size = 20
        val offset = folderId * size
        testDatasource.saveAll((offset until offset + size).map {
            TestEntity(
                it.toString(),
                "Tên $it - ${Random.nextDouble()}",
                "Trạng thái - ${(it % 3)}",
            )
        }, folderId.toString())
    }
}