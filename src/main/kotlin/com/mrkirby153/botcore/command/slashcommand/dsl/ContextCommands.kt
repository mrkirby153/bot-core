package com.mrkirby153.botcore.command.slashcommand.dsl

import com.mrkirby153.botcore.utils.PrerequisiteCheck
import net.dv8tion.jda.api.interactions.commands.context.ContextInteraction

/**
 * Top level class for context menu interactions
 *
 * @param Event The [ContextInteraction] that is provided to [execute] when the interaction is ran
 */
open class ContextCommand<Event : ContextInteraction<*>>(
    val name: String
) {

    /**
     * The name of the command
     */

    private lateinit var commandAction: (Event) -> Unit

    private val checks = mutableListOf<PrerequisiteCheck<Event>.() -> Unit>()

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
     * Adds a new prerequisite check to this context command
     */
    fun check(builder: PrerequisiteCheck<Event>.() -> Unit) {
        this.checks.add(builder)
    }

    /**
     * Executes this command with the provided [event]
     */
    fun execute(event: Event) {
        val checkCtx = PrerequisiteCheck(event)
        checks.forEach {
            it(checkCtx)
            if (checkCtx.failed) {
                event.reply(":no_entry: ${checkCtx.failureMessage ?: "A required prerequisite was not fulfilled"}")
                    .setEphemeral(true).queue()
                return
            }
        }
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
class UserContextCommand(name: String) : ContextCommand<UserContext>(name)

/**
 * A message context menu interaction
 */
class MessageContextCommand(name: String) : ContextCommand<MessageContext>(name)