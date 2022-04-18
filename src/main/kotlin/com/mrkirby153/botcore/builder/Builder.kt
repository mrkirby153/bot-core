package com.mrkirby153.botcore.builder

/**
 * Generic interface for all builders
 */
interface Builder<T> {

    /**
     * Builds an instance of the builder
     */
    fun build(): T
}