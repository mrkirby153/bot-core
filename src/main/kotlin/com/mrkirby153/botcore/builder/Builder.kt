package com.mrkirby153.botcore.builder

/**
 * Generic interface for all builders
 *
 * @param T The JVM type that this builder builds
 */
interface Builder<T> {

    /**
     * Builds an instance of the builder
     */
    fun build(): T
}