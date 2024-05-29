package com.mrkirby153.botcore.command.slashcommand.dsl

import com.mrkirby153.botcore.builder.MessageBuilder
import com.mrkirby153.botcore.coroutine.await
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

    val arguments: Arguments = loadArguments()

    /**
     * Replies to the interaction using a [MessageBuilder]
     */
    fun reply(ephemeral: Boolean = false, body: MessageBuilder.() -> Unit): ReplyCallbackAction {
        val mb = MessageBuilder()
        body(mb)
        return event.reply(mb.create()).setEphemeral(ephemeral)
    }

    operator fun <T> Option<T>.invoke(): T {
        log.trace("Retrieving argument with name {}", this.name)
        val container =
            arguments.getArgument(this.name) ?: error("Argument with name ${this.name} not found")
        return arguments.getValue(container)
    }

    private fun loadArguments(): Arguments {
        log.trace("Loading arguments")
        val parseErrors = mutableMapOf<String, String>()
        val args = Arguments(command)
        event.options.forEach { option ->
            val name = option.name
            val argument = command.arguments[name] ?: return@forEach
            val raw = event.getOption(name)
            log.trace("Mapping {} -> {}. Required? {}", raw, option.name, argument.required)
            if (raw == null) {
                if (argument.required) {
                    log.error("Required argument $name was somehow not provided")
                    parseErrors[name] = "Required argument was somehow not provided. This is a bug"
                }
                return@forEach
            }
            log.trace("Attempting to parse argument {} from {}", name, raw)
            try {
                args.addMappedValue(name, argument.converter.convert(option))
            } catch (e: ArgumentParseException) {
                log.trace("Parse of {} failed", name, e)
                parseErrors[name] = e.message ?: "An unknown error occurred"
            }
        }
        if (parseErrors.isNotEmpty()) {
            log.trace("Parse completed with {} errors", parseErrors.size)
            throw BatchArgumentParseException(parseErrors.map { (k, v) ->
                k to ArgumentParseException(v)
            }.toMap())
        } else {
            log.trace("Parse completed")
        }
        return args
    }

    /**
     * Defers the reply and then executes [handler]. Specify [ephemeral] to defer ephemerally
     */
    suspend fun defer(ephemeral: Boolean = false, handler: suspend (InteractionHook) -> Unit) {
        val hook = deferReply(ephemeral).await()
        handler(hook)
    }
}

/**
 * Context for user context menu interactions
 *
 * @param event The [UserContextInteractionEvent] that this context wraps
 */
class UserContext(event: UserContextInteractionEvent, scope: CoroutineScope) :
    UserContextInteraction by event, CoroutineScope by scope {

}

/**
 * Context for message context interactions
 *
 * @param event The [MessageContextInteractionEvent] that this context wraps
 */
class MessageContext(private val event: MessageContextInteractionEvent, scope: CoroutineScope) :
    MessageContextInteraction by event, CoroutineScope by scope {
    /**
     * The message that the interaction is being run from
     */
    val message
        get() = event.target
}