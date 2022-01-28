package com.android.support.feature.home

import android.os.Bundle
import android.support.core.event.LiveDataStatusOwner
import android.support.core.event.WindowStatusOwner
import android.support.core.view.RecyclerAdapter
import android.support.core.view.RecyclerHolder
import android.support.core.view.bindingOf
import android.support.core.view.viewBinding
import android.support.viewmodel.launch
import android.support.viewmodel.viewModel
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModel
import androidx.recyclerview.widget.RecyclerView
import com.android.support.R
import com.android.support.app.AppFragment
import com.android.support.databinding.FragmentHomeBinding
import com.android.support.databinding.ItemRandomBinding
import com.android.support.model.entity.RandomEntity
import com.android.support.navigation.Router
import com.android.support.navigation.Routing
import com.android.support.repo.random.GenerateRandomRepo
import com.android.support.repo.random.RemoveAllRandomByFolderRepo
import com.android.support.repo.random.RemoveRandomByIdRepo
import com.android.support.repo.random.SearchRandomRepo
import com.android.support.widget.SearchTopBarState
import com.android.support.widget.TopBarOwner

class HomeFragment : AppFragment(R.layout.fragment_home), TopBarOwner {
    private val viewModel by viewModel<HomeViewModel>()
    private val binding by viewBinding(FragmentHomeBinding::bind)
    private val folderId get() = binding.edtGroup.text.toString().toIntOrNull() ?: 0

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        topBar.setState(SearchTopBarState(R.string.title_home, viewModel.searchText) {
            viewModel.search(it)
        })

        binding.btnClick.setOnClickListener {
            viewModel.generate(folderId)
        }

        binding.btnOpenFolder.setOnClickListener {
            viewModel.openFolder(folderId)
        }
        binding.btnRemove.setOnClickListener {
            viewModel.removeAll(folderId)
        }

        viewModel.result.bind(Adapter()::submit)
    }


    private inner class Adapter : RecyclerAdapter<RandomEntity>() {
        init {
            binding.rvList.adapter = this
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            val binding = parent.bindingOf(ItemRandomBinding::inflate)
            return object : RecyclerHolder<RandomEntity>(binding) {
                override fun bind(item: RandomEntity) {
                    super.bind(item)
                    binding.txtName.text = item.name
                    binding.txtId.text = item.id
                    binding.txtStatus.text = item.status
                    binding.btnRemove.setOnClickListener {
                        viewModel.remove(item.id)
                    }
                    itemView.setOnClickListener {
                        Router.open(self, Routing.RandomDetail(item.id, folderId))
                    }
                }
            }
        }

    }

    class HomeViewModel(
        private val generateRandomRepo: GenerateRandomRepo,
        private val searchRandomRepo: SearchRandomRepo,
        private val removeAllRandomByFolderRepo: RemoveAllRandomByFolderRepo,
        private val removeRandomByIdRepo: RemoveRandomByIdRepo,
    ) : ViewModel(), WindowStatusOwner by LiveDataStatusOwner() {
        var searchFolder: Int = 0
            private set
        var searchText: String = ""
            private set
        val result = searchRandomRepo.result

        init {
            search(searchText)
        }

        fun generate(folderId: Int) = launch {
            generateRandomRepo(folderId)
        }

        fun search(text: String) = launch(error = error) {
            searchText = text
            searchRandomRepo(text, searchFolder)
        }

        fun removeAll(folder: Int) = launch(error = error) {
            searchFolder = folder
            removeAllRandomByFolderRepo(folder)
        }

        fun remove(itemID: String) = launch {
            removeRandomByIdRepo(itemID)
        }

        fun openFolder(folderId: Int) = launch {
            searchFolder = folderId
            searchRandomRepo(searchText, searchFolder)
        }
    }
}