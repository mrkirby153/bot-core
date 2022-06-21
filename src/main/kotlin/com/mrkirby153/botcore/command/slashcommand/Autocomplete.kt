package com.mrkirby153.botcore.command.slashcommand

import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent
import net.dv8tion.jda.api.interactions.commands.Command

/**
 * An abstract autocomplete handler class
 *
 * @param clazz The class that the autocomplete is. This is null on invocations
 * @param T The JVM type of this class
 */
abstract class AutocompleteHandler<T>(
    val clazz: Class<T>
) {

    /**
     * The raw value of this handler
     */
    var rawValue: Any? = null

    /**
     * Gets the value typed
     */
    fun getValue(): T {
        return rawValue as T
    }

    /**
     * Gets a list of choices for this autocomplete query. If there are more than 25 options provided,
     * the first 25 will be chosen
     */
    abstract fun choices(
        event: CommandAutoCompleteInteractionEvent
    ): List<Command.Choice>
}