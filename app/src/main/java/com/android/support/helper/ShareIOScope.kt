package com.android.support.helper

import android.support.di.Inject
import android.support.di.ShareScope
import android.util.Log
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

@Inject(ShareScope.FragmentOrActivity)
class ShareIOScope : CoroutineScope, AutoCloseable {
    override val coroutineContext: CoroutineContext =
        SupervisorJob() + Dispatchers.Main.immediate + CoroutineExceptionHandler { _, _ -> }

    override fun close() {
        cancel()
        Log.e("Close", "Scope")
    }
}