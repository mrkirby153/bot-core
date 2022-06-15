package com.mrkirby153.botcore.command.slashcommand.dsl

/**
 * A converter that converts a string from the slash command event into the argument type
 */
interface ArgumentConverter<T> {

    /**
     * Converts an argument from a string into the provided type
     */
    fun convert(input: String): T
}