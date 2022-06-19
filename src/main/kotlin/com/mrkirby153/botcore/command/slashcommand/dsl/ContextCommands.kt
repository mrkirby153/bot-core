package com.mrkirby153.botcore.command.slashcommand.dsl

import net.dv8tion.jda.api.interactions.commands.context.ContextInteraction

open class ContextCommand<Event : ContextInteraction<*>> {
    lateinit var name: String
    private lateinit var commandAction: (Event) -> Unit

    fun action(action: (Event) -> Unit) {
        this.commandAction = action
    }

    fun execute(event: Event) {
        try {
            commandAction(event)
        } catch (e: Exception) {
            event.reply(":no_entry: ${e.message ?: "An unknown error occurred!"}")
                .setEphemeral(true).queue()
        }
    }
}

class UserContextCommand : ContextCommand<UserContext>() {
}

class MessageContextCommand : ContextCommand<MessageContext>() {
}