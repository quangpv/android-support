package com.android.support.feature.auth

import android.app.ActionBar
import android.app.Dialog
import android.content.Context
import android.support.core.view.RecyclerAdapter
import android.support.core.view.RecyclerHolder
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.updateMargins
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.support.R

class ChangeLanguageDialog(context: Context) : Dialog(context) {
    private var mCallback: (Language) -> Unit = {}
    private val titleView = TextView(context)

    init {
        val view = RecyclerView(context)
        setContentView(LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            val size10 = context.resources.getDimensionPixelSize(R.dimen.size_10)
            addView(titleView, createLayoutParams())
            addView(view, createLayoutParams().apply {
                updateMargins(top = size10)
            })

            setPadding(size10, size10, size10, size10)
        })
        view.adapter = Adapter()
        view.layoutManager = LinearLayoutManager(context)
    }

    private fun createLayoutParams(): LinearLayout.LayoutParams {
        return LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
        )
    }

    fun show(title: String, function: (Language) -> Unit) {
        titleView.text = title
        mCallback = {
            function(it)
            dismiss()
        }
        super.show()
    }

    private inner class Adapter : RecyclerAdapter<Language>() {
        init {
            submit(Language.values().toList())
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

            return object : RecyclerHolder<Language>(TextView(parent.context)) {
                init {
                    val padding = itemView.resources.getDimensionPixelSize(R.dimen.size_10)
                    itemView.setPadding(padding, padding, padding, padding)
                }

                override fun bind(item: Language) {
                    super.bind(item)
                    itemView as TextView
                    itemView.text = item.name
                    itemView.setOnClickListener { mCallback(item) }
                }
            }
        }

    }

    enum class Language(val code: String) {
        VietNam("VI"),
        UnitedState("US"),
    }
}