package com.mrkirby153.botcore

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.concurrent.ConcurrentHashMap

internal val logCache = ConcurrentHashMap<Class<*>, Logger>()

internal val Any.log: Logger
    get() = logCache.computeIfAbsent(this::class.java) { LoggerFactory.getLogger(it) }