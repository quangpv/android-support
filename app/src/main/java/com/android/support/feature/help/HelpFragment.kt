package com.android.support.feature.help

import android.os.Bundle
import android.support.core.view.viewBinding
import android.view.View
import com.android.support.R
import com.android.support.app.AppFragment
import com.android.support.databinding.FragmentHelpBinding
import com.android.support.widget.TextCenterTopBarState
import com.android.support.widget.TopBarOwner

class HelpFragment : AppFragment(R.layout.fragment_help), TopBarOwner {
    private val binding by viewBinding(FragmentHelpBinding::bind)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        topBar.setState(TextCenterTopBarState(R.string.title_help))

        binding.txtHelp.setOnClickListener {
        }
    }
}