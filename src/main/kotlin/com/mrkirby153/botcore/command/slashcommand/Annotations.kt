package com.mrkirby153.botcore.command.slashcommand

import net.dv8tion.jda.api.events.interaction.command.MessageContextInteractionEvent
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.events.interaction.command.UserContextInteractionEvent

/**
 * Annotation indicating that this method is a slash command. Slash commands must have the first
 * parameter of the method be a [SlashCommandInteractionEvent], with parameters following, annotated with
 * [SlashCommandParameter]
 */
@Target(AnnotationTarget.FUNCTION)
annotation class SlashCommand(
    /**
     * The name of the slash command
     */
    val name: String,
    /**
     * The slash command's description (Shown in the client)
     */
    val description: String,
    /**
     * The clearance required to run this slash command
     */
    val clearance: Int = 0,

    /**
     * Where this command can be used
     */
    val availability: Array<SlashCommandAvailability> = []
)

/**
 * Annotation indicating that this method is a user context command. User commands must have
 * the first parameter of the method be a [UserContextInteractionEvent]
 */
annotation class UserCommand(
    val name: String,
    val clearance: Int = 0
)

/**
 * Annotation indicating that this method is a message context command. Message commands must
 * have the first parameter of the method be a [MessageContextInteractionEvent]
 */
annotation class MessageCommand(
    val name: String,
    val clearance: Int = 0
)

enum class SlashCommandAvailability {
    GUILD,
    DM
}

@Target(AnnotationTarget.VALUE_PARAMETER)
annotation class SlashCommandParameter(
    /**
     * The name of the parameter
     */
    val name: String,
    /**
     * The parameter's description (Shown in the client)
     */
    val description: String
)