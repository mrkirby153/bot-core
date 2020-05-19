package com.mrkirby153.botcore.spring;

import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.hooks.EventListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;

import javax.annotation.Nonnull;

class EventHandler implements EventListener {

    private static final Logger log = LoggerFactory.getLogger(EventHandler.class);

    private final ApplicationEventPublisher eventPublisher;

    public EventHandler(ApplicationEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    @Override
    public void onEvent(@Nonnull GenericEvent event) {
        log.trace("Dispatching event {} to spring", event.getClass());
        eventPublisher.publishEvent(event);
    }
}
