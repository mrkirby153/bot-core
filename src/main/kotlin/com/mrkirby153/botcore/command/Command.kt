package com.mrkirby153.botcore.command

import net.dv8tion.jda.api.Permission

/**
 * Declares a method as a chat command
 */
@Target(AnnotationTarget.FUNCTION)
annotation class Command(
    /**
     * The name of the command
     */
    val name: String,

    /**
     * The clearance needed to execute this command
     */
    val clearance: Int = 0,
    /**
     * The arguments of this command
     */
    val arguments: Array<String> = [],
    /**
     * The parent of this command
     */
    val parent: String = "",
    /**
     * A list of permissions the bot needs for this command to be invoked
     */
    val permissions: Array<Permission> = []
)
