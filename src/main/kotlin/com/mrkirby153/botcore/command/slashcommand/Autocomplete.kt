package com.mrkirby153.botcore.command.slashcommand

import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent
import net.dv8tion.jda.api.interactions.commands.Command

abstract class AutocompleteHandler<T>(
    /**
     * The class that this autocomplete is. This is null on invocations
     */
    val clazz: Class<T>
) {

    var rawValue: Any? = null

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