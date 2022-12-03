package com.mrkirby153.botcore.coroutine

import com.mrkirby153.botcore.utils.SLF4J
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.events.GenericEvent
import net.dv8tion.jda.api.hooks.EventListener
import net.dv8tion.jda.api.hooks.IEventManager
import net.dv8tion.jda.api.sharding.DefaultShardManagerBuilder
import net.dv8tion.jda.api.sharding.ShardManager
import java.time.Duration
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.Executor
import java.util.concurrent.TimeUnit
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

private val log by SLF4J("CoroutineEventManager")

fun getDefaultScope(
    pool: Executor? = null,
    job: Job? = null,
    errorHandler: CoroutineExceptionHandler? = null,
    context: CoroutineContext = EmptyCoroutineContext
): CoroutineScope {
    val dispatcher = pool?.asCoroutineDispatcher() ?: Dispatchers.Default
    val parent = job ?: SupervisorJob()
    val handler = errorHandler ?: CoroutineExceptionHandler { _, throwable ->
        log.error("Uncaught exception from coroutine", throwable)
        if (throwable is Error) {
            parent.cancel()
            throw throwable
        }
    }
    return CoroutineScope(dispatcher + parent + handler + context)
}

/**
 * An [IEventManager] that executes all events in a coroutine context
 */
open class CoroutineEventManager(
    scope: CoroutineScope = getDefaultScope(),
    private val timeout: EventTimeout = EventTimeout.Infinite
) : IEventManager, CoroutineScope by scope {
    protected val listeners = CopyOnWriteArrayList<Any>()

    override fun register(listener: Any) {
        check(listener is EventListener || listener is CoroutineEventListener) { "Listener must implement CoroutineEventListener or EventListener" }
        listeners.add(listener)
    }

    override fun unregister(listener: Any) {
        listeners.remove(listener)
    }

    private fun getTimeout(listener: CoroutineEventListener): Long? {
        val listenerTimeout = listener.timeout()
        println(listenerTimeout.timeout)
        if (listenerTimeout == EventTimeout.Inherit) {
            return if (timeout == EventTimeout.Infinite) return null else timeout.timeout
        } else {
            return if (listenerTimeout == EventTimeout.Infinite) return null else listenerTimeout.timeout
        }
    }

    override fun handle(event: GenericEvent) {
        launch {
            listeners.forEach { listener ->
                try {
                    val timeout = (listener as? CoroutineEventListener)?.run { getTimeout(this) }
                    if (timeout != null) {
                        val result = withTimeoutOrNull(timeout) {
                            runListener(listener, event)
                        }
                        if (result == null) {
                            log.debug("Event of type {} timed out", event.javaClass.simpleName)
                        }
                    } else {
                        runListener(listener, event)
                    }
                } catch (e: Exception) {
                    log.error("Uncaught exception in event listener", e)
                }
            }
        }
    }

    protected open suspend fun runListener(listener: Any, event: GenericEvent) = when (listener) {
        is CoroutineEventListener -> listener.onEvent(event)
        is EventListener -> listener.onEvent(event)
        else -> Unit
    }

    override fun getRegisteredListeners(): MutableList<Any> = mutableListOf(listeners)

}

/**
 * Sets the event manager to a [CoroutineEventManager] dispatching events in coroutines
 */
fun JDABuilder.enableCoroutines() = setEventManager(CoroutineEventManager())

/**
 * Sets the event manager to a [CoroutineEventManager] dispatching events in coroutines
 */
fun DefaultShardManagerBuilder.enableCoroutines() =
    setEventManagerProvider { CoroutineEventManager() }

/**
 * Listens for the given event [T]. Specify [timeout] to have these coroutines time out after a duration
 */
inline fun <reified T : GenericEvent> JDA.on(
    timeout: Duration? = null,
    crossinline consumer: suspend CoroutineEventListener.(T) -> Unit
): CoroutineEventListener {
    return object : CoroutineEventListener {
        override fun timeout(): EventTimeout {
            return timeout?.run { EventTimeout(timeout.toMillis(), TimeUnit.MILLISECONDS) }
                ?: EventTimeout.Infinite
        }

        override suspend fun onEvent(event: GenericEvent) {
            if (event is T)
                consumer(event)
        }

        override fun cancel() {
            removeEventListener(this)
        }
    }.also { addEventListener(it) }
}

/**
 * Listens for the given event [T]. Specify [timeout] to have these coroutines time out after a duration
 */
inline fun <reified T : GenericEvent> ShardManager.on(
    timeout: Duration? = null,
    crossinline consumer: suspend CoroutineEventListener.(T) -> Unit
): CoroutineEventListener {
    return object : CoroutineEventListener {
        override fun timeout(): EventTimeout {
            return timeout?.run { EventTimeout(timeout.toMillis(), TimeUnit.MILLISECONDS) }
                ?: EventTimeout.Infinite
        }

        override suspend fun onEvent(event: GenericEvent) {
            if (event is T)
                consumer(event)
        }

        override fun cancel() {
            removeEventListener(this)
        }
    }.also { addEventListener(it) }
}