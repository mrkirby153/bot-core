package com.mrkirby153.botcore

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.concurrent.ConcurrentHashMap

/**
 * A cache of all constructed loggers
 */
internal val logCache = ConcurrentHashMap<Class<*>, Logger>()

/**
 * Gets a logger for the current class
 */
internal val Any.log: Logger
    get() = logCache.computeIfAbsent(this::class.java) { LoggerFactory.getLogger(it) }