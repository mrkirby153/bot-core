package com.mrkirby153.botcore.command.slashcommand

import net.dv8tion.jda.api.events.interaction.SlashCommandEvent

/**
 * Annotation indicating that this method is a slash command. Slash commands must have the first
 * parameter of the method be a [SlashCommandEvent], with parameters following, annotated with
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