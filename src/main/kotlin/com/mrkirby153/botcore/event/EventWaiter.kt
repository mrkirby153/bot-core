package com.mrkirby153.botcore.event

import net.dv8tion.jda.api.events.Event
import net.dv8tion.jda.api.events.GenericEvent
import net.dv8tion.jda.api.hooks.EventListener
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit
import java.util.function.Consumer
import java.util.function.Predicate

/**
 * Handles waiting for an [Event] from discord
 *
 * @param pool The thread pool to use when waiting for events
 */
class EventWaiter(
        private val pool: ScheduledExecutorService = Executors.newSingleThreadScheduledExecutor()) :
        EventListener {

    private val waitingEvents = mutableMapOf<Class<*>, MutableSet<WaitingEvent<*>>>()

    override fun onEvent(event: GenericEvent) {
        var c: Class<in GenericEvent>? = event.javaClass
        while (c != null) {
            val events = waitingEvents[c] ?: return
            events.removeIf { it.attempt(event) }
            c = c.superclass
        }
    }

    /**
     * Waits for an [Event] satisfying the given [Predicate] or until a timeout has occurred
     *
     * @param event The event to wait for
     * @param predicate The predicate the event must satisfy
     * @param action The action to perform
     * @param timeout The time before the timer times out and the `onTimeout` [Runnable] is ran. Set to 0 for no timeout
     * @param units The units for the timeout
     * @param onTimeout An action to run when the event times out
     */
    @JvmOverloads
    fun <T : Event> waitFor(event: Class<T>, predicate: Predicate<T>, action: Consumer<T>,
                            timeout: Long = 30, units: TimeUnit = TimeUnit.SECONDS,
                            onTimeout: Runnable? = null) {
        if (pool.isShutdown)
            throw IllegalStateException(
                    "Attempting to wait for an event when the listener has been shut down")
        val we = WaitingEvent(predicate, action)
        val set = waitingEvents.computeIfAbsent(event) { mutableSetOf() }
        set.add(we)

        if (timeout > 0) {
            pool.schedule({
                if (set.remove(we) && onTimeout != null) {
                    onTimeout.run()
                }
            }, timeout, units)
        }
    }

    /**
     * Shuts down the [ScheduledExecutorService] in charge of unscheduling events
     */
    fun shutdown() {
        pool.shutdown()
    }

    private class WaitingEvent<T : GenericEvent>(val predicate: Predicate<T>,
                                          val action: Consumer<T>) {

        @Suppress("UNCHECKED_CAST")
        fun attempt(event: GenericEvent): Boolean {
            return if (predicate.test(event as T)) {
                action.accept(event)
                true
            } else {
                false
            }
        }
    }
}