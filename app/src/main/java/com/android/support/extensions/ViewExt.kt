package com.android.support.extensions

import android.widget.CheckBox
import android.widget.EditText
import androidx.core.widget.doOnTextChanged


fun EditText.bind(any: (String) -> Unit) {
    doOnTextChanged { text, _, _, _ -> any(text?.toString().orEmpty()) }
}

fun CheckBox.bind(any: (Boolean) -> Unit) {
    setOnCheckedChangeListener { _, b -> any(b) }
}