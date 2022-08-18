package com.mrkirby153.botcore.command.slashcommand.dsl

import com.mrkirby153.botcore.command.slashcommand.dsl.types.enum
import com.mrkirby153.botcore.command.slashcommand.dsl.types.int
import com.mrkirby153.botcore.command.slashcommand.dsl.types.string

/**
 * A top level class for slash command arguments.
 *
 * To use arguments in slash commands, create a class that extends [Arguments]. Arguments can then
 * be declared using the various argument delegators ([string], [int], [enum], etc.)
 */
open class Arguments {

    private val arguments = mutableListOf<ArgumentContainer<*, *>>()

    internal fun addArgument(container: ArgumentContainer<*, *>) = arguments.add(container)

    internal fun getArguments() = arguments.toList().sortedBy { arg -> !arg.required }

    internal fun getArgument(name: String) = arguments.firstOrNull { it.builder.name == name }
}