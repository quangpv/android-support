package com.android.support.feature.help

import android.annotation.SuppressLint
import android.os.Bundle
import android.support.core.view.viewBinding
import android.support.viewmodel.viewModel
import android.view.View
import androidx.lifecycle.ViewModel
import com.android.support.R
import com.android.support.app.AppFragment
import com.android.support.databinding.FragmentContactHelpBinding

class ContactHelpFragment : AppFragment(R.layout.fragment_contact_help) {
    private val binding by viewBinding(FragmentContactHelpBinding::bind)
    private val viewModel by viewModel<VM>()

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.user.bind {
            binding.txtUser.text = "Hello user ${it.name}"
        }
    }

    class VM(private val fetchUserRepo: FetchUserRepo) : ViewModel() {
        val user = fetchUserRepo.result
    }
}