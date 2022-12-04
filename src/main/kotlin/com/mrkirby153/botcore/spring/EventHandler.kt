package com.mrkirby153.botcore.spring

import com.mrkirby153.botcore.utils.SLF4J
import net.dv8tion.jda.api.events.GenericEvent
import net.dv8tion.jda.api.hooks.EventListener
import org.slf4j.LoggerFactory
import org.springframework.context.ApplicationEventPublisher

/**
 * Event handler to dispatch JDA events into the Spring event publisher
 */
class EventHandler
/**
 * Constructs a new event handler
 *
 * @param eventPublisher The publisher to publish events on
 */(private val eventPublisher: ApplicationEventPublisher) : EventListener {

    private val log by SLF4J
    override fun onEvent(event: GenericEvent) {
        log.trace("Dispatching event {} to spring", event.javaClass)
        eventPublisher.publishEvent(event)
    }
}