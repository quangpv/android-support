package com.android.support.exception

class ViewError(val id: Int, message: String) : RuntimeException(message)

fun viewError(id: Int, message: String): Nothing = throw ViewError(id, message)