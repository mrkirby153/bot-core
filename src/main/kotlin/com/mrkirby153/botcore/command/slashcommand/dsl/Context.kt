package com.mrkirby153.botcore.command.slashcommand.dsl

import com.mrkirby153.botcore.builder.MessageBuilder
import com.mrkirby153.botcore.command.args.ArgumentParseException
import com.mrkirby153.botcore.command.args.BatchArgumentParseException
import net.dv8tion.jda.api.events.interaction.command.GenericContextInteractionEvent
import net.dv8tion.jda.api.events.interaction.command.MessageContextInteractionEvent
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.events.interaction.command.UserContextInteractionEvent
import net.dv8tion.jda.api.interactions.commands.SlashCommandInteraction
import net.dv8tion.jda.api.interactions.commands.context.MessageContextInteraction
import net.dv8tion.jda.api.interactions.commands.context.UserContextInteraction
import net.dv8tion.jda.api.requests.restaction.interactions.ReplyCallbackAction

class SlashContext<A : Arguments>(
    val command: AbstractSlashCommand<A>,
    private val event: SlashCommandInteractionEvent
) : SlashCommandInteraction by event {

    lateinit var args: A
    fun load() {
        loadArguments()
    }

    fun reply(body: MessageBuilder.() -> Unit): ReplyCallbackAction {
        val mb = MessageBuilder()
        body(mb)
        return event.reply(mb.build())
    }

    private fun loadArguments() {
        val args = command.args() ?: return
        this.args = args


        val argParseErrors = mutableMapOf<String, String>()
        args.get().forEach { arg ->
            val name = arg.displayName
            val raw =
                event.getOption(name)
            if (raw == null) {
                if (arg !is NullableArgument) {
                    argParseErrors[name] = "Required argument was not provided"
                }
            } else {
                try {
                    arg.parse(raw)
                } catch (e: ArgumentParseException) {
                    argParseErrors[name] = e.message ?: "An unknown parse error occurred"
                }
            }
        }
        if (argParseErrors.isNotEmpty()) {
            throw BatchArgumentParseException(argParseErrors.map { (k, v) ->
                k to ArgumentParseException(
                    v
                )
            }.toMap())
        }
    }
}

class UserContext(event: UserContextInteractionEvent) : UserContextInteraction by event {

}

class MessageContext(private val event: MessageContextInteractionEvent) : MessageContextInteraction by event {
    val message
        get() = event.target
}