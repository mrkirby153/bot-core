package com.mrkirby153.botcore.command.slashcommand.dsl

import com.mrkirby153.botcore.command.slashcommand.dsl.types.IArgBuilder
import com.mrkirby153.botcore.command.slashcommand.dsl.types.enum
import com.mrkirby153.botcore.command.slashcommand.dsl.types.int
import com.mrkirby153.botcore.command.slashcommand.dsl.types.optionalString
import com.mrkirby153.botcore.command.slashcommand.dsl.types.string

/**
 * A top level class for slash command arguments.
 *
 * To use arguments in slash commands, create a class that extends [Arguments]. Arguments can then
 * be declared using the various argument delegators ([string], [int], [enum], etc.). Arguments that
 * are nullable (optional) follow the convention `optionalXXX` (i.e. [optionalString])
 */
open class Arguments {

    private val arguments = mutableListOf<IArgument<*, out IArgBuilder<*>>>()

    /**
     * Adds the provided [arg] to the list of arguments
     */
    internal fun addArgument(arg: IArgument<*, out IArgBuilder<*>>) = arguments.add(arg)

    /**
     * Returns the list of arguments
     */
    fun get() = arguments.toList().sortedBy { a -> if (a is NullableArgument) 1 else -1 }

    /**
     * Gets a specific argument by its [key] ([IArgument.displayName])
     */
    fun get(key: String) = arguments.firstOrNull { it.displayName == key }
}