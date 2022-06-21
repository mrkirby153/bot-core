package com.mrkirby153.botcore.command.slashcommand.dsl

import net.dv8tion.jda.api.interactions.commands.context.ContextInteraction

/**
 * Top level class for context menu interactions
 *
 * @param Event The [ContextInteraction] that is provided to [execute] when the interaction is ran
 */
open class ContextCommand<Event : ContextInteraction<*>> {

    /**
     * The name of the command
     */
    lateinit var name: String
    private lateinit var commandAction: (Event) -> Unit

    /**
     * If this command should be enabled by default
     */
    var enabledByDefault = true

    /**
     * The function ran when this interaction is invoked
     */
    fun action(action: (Event) -> Unit) {
        this.commandAction = action
    }

    /**
     * Executes this command with the provided [event]
     */
    fun execute(event: Event) {
        try {
            commandAction(event)
        } catch (e: Exception) {
            event.reply(":no_entry: ${e.message ?: "An unknown error occurred!"}")
                .setEphemeral(true).queue()
        }
    }
}

/**
 * A user context menu interaction
 */
class UserContextCommand : ContextCommand<UserContext>() {
}

/**
 * A message context menu interaction
 */
class MessageContextCommand : ContextCommand<MessageContext>() {
}