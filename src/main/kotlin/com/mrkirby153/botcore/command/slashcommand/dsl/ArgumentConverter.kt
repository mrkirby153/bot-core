package com.mrkirby153.botcore.command.slashcommand.dsl

import net.dv8tion.jda.api.interactions.commands.OptionMapping

/**
 * A converter that converts a string from the slash command event into the argument type
 */
interface ArgumentConverter<T> {

    /**
     * Converts an argument from a string into the provided type
     */
    fun convert(input: OptionMapping): T
}