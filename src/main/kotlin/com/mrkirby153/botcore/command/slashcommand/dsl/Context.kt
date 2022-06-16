package com.mrkirby153.botcore.command.slashcommand.dsl

import com.mrkirby153.botcore.builder.MessageBuilder
import com.mrkirby153.botcore.command.args.ArgumentParseException
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.requests.restaction.interactions.ReplyCallbackAction

class Context<A : Arguments>(
    val command: AbstractSlashCommand<A>,
    private val event: SlashCommandInteractionEvent
) {

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

        // Parse all required arguments
        args.get().forEach { arg ->
            val name = arg.displayName
            val raw =
                event.getOption(name) ?: throw ArgumentParseException("Missing argument $name")
            arg.parse(raw)
        }
        // Parse all optional arguments
        args.getNullable().forEach { arg ->
            val name = arg.displayName
            val raw = event.getOption(name) ?: return@forEach
            arg.parse(raw)
        }
    }
}