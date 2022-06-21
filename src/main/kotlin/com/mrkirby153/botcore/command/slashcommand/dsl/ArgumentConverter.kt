package com.mrkirby153.botcore.command.slashcommand.dsl

import net.dv8tion.jda.api.interactions.commands.OptionMapping

/**
 * A converter that converts a string from the slash command event into the argument type
 *
 * @param T The JVM type that this converter produces
 */
interface ArgumentConverter<T> {

    /**
     * Converts an argument from the [input] into the provided JVM type
     */
    fun convert(input: OptionMapping): T
}