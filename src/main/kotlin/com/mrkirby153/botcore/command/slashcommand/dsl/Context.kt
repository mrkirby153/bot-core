package com.mrkirby153.botcore.command.slashcommand.dsl

import com.mrkirby153.botcore.builder.MessageBuilder
import com.mrkirby153.botcore.utils.SLF4J
import kotlinx.coroutines.CoroutineScope
import net.dv8tion.jda.api.events.interaction.command.MessageContextInteractionEvent
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.events.interaction.command.UserContextInteractionEvent
import net.dv8tion.jda.api.interactions.InteractionHook
import net.dv8tion.jda.api.interactions.commands.SlashCommandInteraction
import net.dv8tion.jda.api.interactions.commands.context.MessageContextInteraction
import net.dv8tion.jda.api.interactions.commands.context.UserContextInteraction
import net.dv8tion.jda.api.requests.restaction.interactions.ReplyCallbackAction
import org.slf4j.Logger

/**
 * A [SlashCommandInteraction] with additional fields to interact with the Slash DSL.
 *
 * @param command The slash command that is being invoked
 * @param event The event that invoked this slash command
 * @param A The [Arguments] for this slash command
 */
@SlashDsl
class SlashContext(
    val command: AbstractSlashCommand,
    private val event: SlashCommandInteractionEvent,
    private val coroutineScope: CoroutineScope
) : SlashCommandInteraction by event, CoroutineScope by coroutineScope {

    private val log: Logger by SLF4J

    /**
     * Loads the context
     */
    fun load() {
        log.trace("Loading context")
        loadArguments()
    }

    /**
     * Replies to the interaction using a [MessageBuilder]
     */
    fun reply(ephemeral: Boolean = false, body: MessageBuilder.() -> Unit): ReplyCallbackAction {
        val mb = MessageBuilder()
        body(mb)
        return event.reply(mb.create()).setEphemeral(ephemeral)
    }

    private fun loadArguments() {
//        log.trace("Loading arguments")
//        val args = command.args() ?: return
//        this.args = args
//
//
//        val argParseErrors = mutableMapOf<String, String>()
//        event.options.forEach { opt ->
//            val name = opt.name
//            val arg = args.getArgument(name) ?: return@forEach
//            val raw = event.getOption(name)
//            log.trace("Mapping {} -> {}. Required? {}", opt.name, raw, arg.required)
//            if (raw == null) {
//                if (arg.required) {
//                    log.error("Required argument $name was somehow not provided")
//                    argParseErrors[name] = "Required argument was not provided"
//                }
//                return@forEach
//            }
//            log.trace("Attempting to parse argument {} with {}", name, raw)
//            try {
//                args.addMappedValue(name, arg.converter.convert(opt))
//            } catch (e: ArgumentParseException) {
//                log.trace("Parse of {} failed", name, e)
//                argParseErrors[name] = e.message ?: "An unknown error occurred"
//            }
//        }
//        if (argParseErrors.isNotEmpty()) {
//            log.trace("Parse completed with {} errors", argParseErrors.size)
//            throw BatchArgumentParseException(argParseErrors.map { (k, v) ->
//                k to ArgumentParseException(v)
//            }.toMap())
//        }
    }
}

/**
 * Context for user context menu interactions
 *
 * @param event The [UserContextInteractionEvent] that this context wraps
 */
class UserContext(event: UserContextInteractionEvent) : UserContextInteraction by event {

}

/**
 * Context for message context interactions
 *
 * @param event The [MessageContextInteractionEvent] that this context wraps
 */
class MessageContext(private val event: MessageContextInteractionEvent) :
    MessageContextInteraction by event {
    /**
     * The message that the interaction is being ran from
     */
    val message
        get() = event.target
}