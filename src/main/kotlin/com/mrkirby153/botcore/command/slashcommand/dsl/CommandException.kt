package com.mrkirby153.botcore.command.slashcommand.dsl

/**
 * A generic exception that can be thrown by commands if exceptions occur during execution.
 *
 * @param msg The exception message
 */
class CommandException(
    val msg: String
) : RuntimeException(msg)