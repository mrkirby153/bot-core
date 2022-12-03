package com.mrkirby153.botcore.coroutine

import net.dv8tion.jda.api.events.GenericEvent

/**
 * A JDA event listener that executes events in a coroutine
 */
fun interface CoroutineEventListener {

    /**
     * The duration for how long callbacks can run. Defaults to [EventTimeout.Inherit], which will
     * use the timeout from the event manager
     */
    fun timeout(): EventTimeout = EventTimeout.Inherit

    suspend fun onEvent(event: GenericEvent)

    /**
     * Cancels this event listener (i.e. unregisters it)
     */
    fun cancel() {}
}