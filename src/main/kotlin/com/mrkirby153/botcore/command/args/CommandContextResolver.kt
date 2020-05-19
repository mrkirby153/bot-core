package com.mrkirby153.botcore.command.args

import java.util.LinkedList

/**
 * Interface for resolving command contexts
 */
interface CommandContextResolver {


    /**
     * Resolves an object from a linked list of parameters
     *
     * @param params The raw command parameters
     *
     * @return The resolved object
     */
    fun resolve(params: LinkedList<String>): Any?
}