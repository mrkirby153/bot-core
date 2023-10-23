package com.mrkirby153.botcore.event

import com.mrkirby153.botcore.utils.SLF4J
import net.dv8tion.jda.api.events.Event
import net.dv8tion.jda.api.events.GenericEvent
import net.dv8tion.jda.api.hooks.EventListener
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException
import java.util.function.Consumer
import java.util.function.Predicate

/**
 * Handles waiting for an [Event] from discord
 *
 * @param pool The thread pool to use when waiting for events
 */
class EventWaiter(
    private val pool: ScheduledExecutorService = Executors.newSingleThreadScheduledExecutor()
) : EventListener {

    private val log by SLF4J

    private val waitingEvents = mutableMapOf<Class<*>, MutableSet<WaitingEvent<*>>>()

    override fun onEvent(event: GenericEvent) {
        var c: Class<in GenericEvent>? = event.javaClass
        while (c != null) {
            val events = waitingEvents[c] ?: return
            events.removeIf { it.attempt(event) }
            c = c.superclass
        }
    }

    @JvmOverloads
    @Deprecated("Use the CompletableFuture version instead")
    fun <T : Event> waitFor(
        event: Class<T>, predicate: Predicate<T>, action: Consumer<T>,
        timeout: Long = 30, units: TimeUnit = TimeUnit.SECONDS,
        onTimeout: Runnable? = null
    ) {
        waitFor(event, predicate, timeout, units).exceptionally {
            if (it is TimeoutException) {
                log.debug("Waiting for {} timed out", event)
                onTimeout?.run()
            } else {
                log.debug("Waiting for {} completed exceptionally", event, it)
                throw it
            }
            null
        }.thenAccept(action)
    }

    /**
     * Waits for an [Event] satisfying the given [Predicate] or until a timeout has occurred
     *
     * @param event The event to wait for
     * @param predicate The predicate the event must satisfy
     * @param timeout The time before the timer times out and the returned future is completed with [TimeoutException]
     * @param units The units for the timeout
     */
    @JvmOverloads
    fun <T : Event> waitFor(
        event: Class<T>,
        predicate: Predicate<T>,
        timeout: Long = 30,
        units: TimeUnit = TimeUnit.SECONDS
    ): CompletableFuture<T> {
        check(!pool.isShutdown) { "Attempting to wait for an event when the listener is shut down" }
        val future = CompletableFuture<T>()
        val waitingEvent = WaitingEvent(predicate, future, null)
        val set = waitingEvents.computeIfAbsent(event) { mutableSetOf() }
        set.add(waitingEvent)
        log.debug("Waiting {} {} for {}", timeout, units, event)
        if (timeout > 0) {
            val sf = pool.schedule({
                if (set.remove(waitingEvent)) {
                    log.debug("Timed out waiting for {}", event)
                    future.completeExceptionally(TimeoutException("Timed out"))
                }
            }, timeout, units)
            waitingEvent.timeoutFuture = sf
        }
        return future
    }

    /**
     * Waits for the given event [T] that matches the provided [predicate]. Specify [timeout] and
     * [units] to specify how long to wait, or provide `0` for no timeout.
     */
    inline fun <reified T : Event> waitFor(
        crossinline predicate: (T) -> Boolean,
        timeout: Long = 30,
        units: TimeUnit = TimeUnit.SECONDS
    ) = waitFor(T::class.java, { predicate(it) }, timeout, units)

    /**
     * Shuts down the [ScheduledExecutorService] in charge of unscheduling events
     */
    fun shutdown() {
        pool.shutdown()
    }

    private class WaitingEvent<T : GenericEvent>(
        val predicate: Predicate<T>,
        val action: CompletableFuture<T>,
        var timeoutFuture: ScheduledFuture<*>?
    ) {

        @Suppress("UNCHECKED_CAST")
        fun attempt(event: GenericEvent): Boolean {
            return if (predicate.test(event as T)) {
                timeoutFuture?.cancel(true)
                action.complete(event)
                true
            } else {
                false
            }
        }
    }
}