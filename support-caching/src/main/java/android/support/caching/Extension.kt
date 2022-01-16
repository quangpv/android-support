package android.support.caching

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

internal suspend fun <T> withIO(function: suspend CoroutineScope. () -> T): T {
    return withContext(Dispatchers.IO) { function() }
}