package com.android.support.feature.help

import android.os.Bundle
import android.support.core.view.viewBinding
import android.support.viewmodel.viewModel
import android.view.View
import android.widget.Toast
import androidx.lifecycle.ViewModel
import com.android.support.R
import com.android.support.app.AppFragment
import com.android.support.databinding.FragmentSupportHelpBinding

class SupportHelpFragment : AppFragment(R.layout.fragment_support_help) {
    private val binding by viewBinding(FragmentSupportHelpBinding::bind)
    private val viewModel by viewModel<VM>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.btnSupportViaCall.setOnClickListener {
            Toast.makeText(it.context, "Not support yet", Toast.LENGTH_SHORT).show()
        }
        viewModel.user.bind {
            binding.txtUser.text = it.name
        }
    }

    class VM(private val fetchUserRepo: FetchUserRepo) : ViewModel() {

        val user = fetchUserRepo.result
    }
}