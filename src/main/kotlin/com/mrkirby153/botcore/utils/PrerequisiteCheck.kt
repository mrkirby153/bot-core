package com.mrkirby153.botcore.utils

/**
 * A generic prerequisite check
 *
 * @param instance An instance providing context to checks
 */
open class PrerequisiteCheck<T>(
    val instance: T
) {
    /**
     * If the check has failed
     */
    var failed = false
        private set

    /**
     * The failure message this check failed with
     */
    var failureMessage: String? = null
        private set

    /**
     * Fails the check with the optionally provided [message]
     */
    fun fail(message: String? = null) {
        this.failed = true
        this.failureMessage = message
    }

    fun failWhen(condition: Boolean, message: String? = null) {
        if (condition) {
            this.failed = true
            this.failureMessage = message;
        }
    }

    fun failWhen(message: String? = null, check: () -> Boolean) {
        if (!check()) {
            this.failed = true
            this.failureMessage = message
        }
    }
}