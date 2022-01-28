package com.android.support.feature.random

import android.os.Bundle
import android.support.core.event.LiveDataStatusOwner
import android.support.core.event.WindowStatusOwner
import android.support.core.livedata.SingleLiveEvent
import android.support.core.livedata.call
import android.support.core.route.lazyArgument
import android.support.core.view.viewBinding
import android.support.viewmodel.launch
import android.support.viewmodel.viewModel
import android.view.View
import androidx.lifecycle.ViewModel
import com.android.support.R
import com.android.support.app.AppFragment
import com.android.support.databinding.FragmentRandomDetailBinding
import com.android.support.model.request.EditRandomRequest
import com.android.support.navigation.Routing
import com.android.support.repo.random.EditRandomRepo
import com.android.support.repo.random.FetchRandomByIdRepo

class RandomDetailFragment : AppFragment(R.layout.fragment_random_detail) {
    private val binding by viewBinding(FragmentRandomDetailBinding::bind)
    private val viewModel by viewModel<VM>()

    private val args by lazyArgument<Routing.RandomDetail>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.random.bind {
            with(binding) {
                txtId.text = it.id
                edtName.setText(it.name)
                edtStatus.setText(it.status)
                edtFolderId.setText(args.folderId.toString())
            }
        }
        binding.btnSave.setOnClickListener {
            viewModel.save(EditRandomRequest(
                binding.txtId.text.toString(),
                binding.edtName.text.toString(),
                binding.edtStatus.text.toString(),
                binding.edtFolderId.text.toString()
            ))
        }
        binding.btnBack.setOnClickListener {
            requireActivity().onBackPressed()
        }
        viewModel.success.bind {
            requireActivity().onBackPressed()
        }
        viewModel.setId(args.id)
    }

    class VM(
        private val fetchRandomByIdRepo: FetchRandomByIdRepo,
        private val editRandomRepo: EditRandomRepo,
    ) : ViewModel(), WindowStatusOwner by LiveDataStatusOwner() {
        val success = SingleLiveEvent<Any>()
        val random = fetchRandomByIdRepo.result

        fun setId(id: String) = launch {
            fetchRandomByIdRepo(id)
        }

        fun save(request: EditRandomRequest) = launch(error = error) {
            editRandomRepo(request)
            success.call()
        }
    }
}