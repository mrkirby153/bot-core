package com.mrkirby153.botcore.coroutine

import java.util.concurrent.TimeUnit

private const val INFINITE = -1L
private const val INHERIT = -2L

/**
 * Class for managing timeouts of events
 */
class EventTimeout private constructor(val timeout: Long) {

    constructor(duration: Long, timeUnit: TimeUnit) : this(
        TimeUnit.MILLISECONDS.convert(
            duration,
            timeUnit
        )
    )

    private constructor() : this(INFINITE)

    override fun equals(other: Any?): Boolean {
        return other is EventTimeout && other.timeout == timeout
    }

    override fun hashCode(): Int {
        return timeout.hashCode()
    }

    companion object {
        val Inherit = EventTimeout(INHERIT)
        val Infinite = EventTimeout(INFINITE)
    }
}