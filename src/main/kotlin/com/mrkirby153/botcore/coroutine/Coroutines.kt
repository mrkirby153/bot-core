package com.mrkirby153.botcore.coroutine

import kotlinx.coroutines.suspendCancellableCoroutine
import net.dv8tion.jda.api.requests.RestAction
import java.util.concurrent.CompletableFuture
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

private suspend fun <T> CompletableFuture<T>.await() = suspendCancellableCoroutine<T> {
    it.invokeOnCancellation { cancel(true) }
    whenComplete { r, e ->
        when {
            e != null -> it.resumeWithException(e)
            else -> it.resume(r)
        }
    }
}

/**
 * Await the results of the [RestAction]
 */
suspend fun <T> RestAction<T>.await(): T = submit().await()