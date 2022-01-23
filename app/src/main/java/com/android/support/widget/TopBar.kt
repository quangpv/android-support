package com.android.support.widget

import android.view.View
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.core.widget.doOnTextChanged
import com.android.support.R
import com.android.support.databinding.TopBarSearchBinding
import com.android.support.databinding.TopBarSimpleBinding
import com.android.support.databinding.TopBarTextCenterBinding


class SimpleTopBarState(
    @StringRes
    private val title: Int,
    @DrawableRes
    private val iconBack: Int = R.drawable.ic_baseline_arrow_back_24,
    private val hasDivider: Boolean = false,
    private val onBackClick: () -> Unit = {}
) : TopBarState() {
    override val stateBinding by bindingOf(TopBarSimpleBinding::inflate)

    override fun doApply() {
        with(stateBinding) {
            txtABTitle.setText(title)
            btnBack.setImageResource(iconBack)
            btnBack.setOnClickListener { onBackClick() }
            divider.visibility = if (hasDivider) View.VISIBLE else View.GONE
        }
    }
}

class TextCenterTopBarState(
    @StringRes
    private val title: Int,
    private val hasDivider: Boolean = false,
) : TopBarState() {
    override val stateBinding by bindingOf(TopBarTextCenterBinding::inflate)

    override fun doApply() {
        with(stateBinding) {
            txtABTitle.setText(title)
            divider.visibility = if (hasDivider) View.VISIBLE else View.GONE
        }
    }
}

class SearchTopBarState(
    private val hint: Int = R.string.hint_search,
    private val initial: String = "",
    private val onTextChange: (String) -> Unit = {}
) : TopBarState() {
    override val stateBinding by bindingOf(TopBarSearchBinding::inflate)

    override fun doApply() {
        with(stateBinding) {
            edtABSearch.setHint(hint)
            edtABSearch.doOnTextChanged { text, _, _, _ ->
                onTextChange(text?.toString().orEmpty())
            }
            edtABSearch.setText(initial)
        }
    }

    fun setText(it: String) {
        stateBinding.edtABSearch.setText(it)
    }
}