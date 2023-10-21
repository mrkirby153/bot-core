package com.mrkirby153.botcore.command.slashcommand.dsl

/**
 * Interface for arguments that require special processing when invoked
 */
interface WrappedData<T> {
    /**
     * Retrieves the data
     */
    fun get(): T
}
