package com.mrkirby153.botcore.command.slashcommand.dsl

import com.mrkirby153.botcore.builder.MessageBuilder
import com.mrkirby153.botcore.command.args.ArgumentParseException
import com.mrkirby153.botcore.command.args.BatchArgumentParseException
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.SlashCommandInteraction
import net.dv8tion.jda.api.requests.restaction.interactions.ReplyCallbackAction

class Context<A : Arguments>(
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

        // Parse all required arguments
        val argParseErrors = mutableMapOf<String, String>()
        args.get().forEach { arg ->
            val name = arg.displayName
            val raw =
                event.getOption(name) ?: throw ArgumentParseException("Missing argument $name")
            try {
                arg.parse(raw)
            } catch (e: ArgumentParseException) {
                argParseErrors[name] = e.message ?: "An unknown parse error occurred"
            }
        }
        // Parse all optional arguments
        args.getNullable().forEach { arg ->
            val name = arg.displayName
            val raw = event.getOption(name) ?: return@forEach
            try {
                arg.parse(raw)
            } catch (e: ArgumentParseException) {
                argParseErrors[name] = e.message ?: "An unknown parse error occurred"
            }
        }
        if (argParseErrors.isNotEmpty()) {
            throw BatchArgumentParseException(argParseErrors.map { (k, v) -> k to ArgumentParseException(v) }.toMap())
        }
    }
}