package com.mrkirby153.botcore.command

import net.dv8tion.jda.api.entities.Message


/**
 * A [Message] that provides additional information about the invoked command
 *
 * @param message The message of the invoking command
 */
class Context(
    message: Message
) : Message by message {
    /**
     * The clearance of the user invoking the command
     */
    var clearance: Int = 0

    /**
     * The name of the command that was invoked
     */
    var commandName: String = ""

    /**
     * The prefix that was used to invoke the command
     */
    var commandPrefix: String = ""
}