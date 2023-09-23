package com.mrkirby153.botcore.command.slashcommand.dsl

import com.mrkirby153.botcore.command.slashcommand.dsl.types.enum
import com.mrkirby153.botcore.command.slashcommand.dsl.types.string

/**
 * A top level class for slash command arguments.
 *
 * To use arguments in slash commands, create a class that extends [Arguments]. Arguments can then
 * be declared using the various argument delegators ([string], [int], [enum], etc.)
 */
class Arguments(
    private val command: AbstractSlashCommand
) {
    private val mapped = HashMap<String, Any?>()

    private fun getArguments() =
        command.arguments.values.toList().sortedBy { arg -> !arg.required }

    internal fun getArgument(name: String): ArgumentContainer<*, *>? {
        return getArguments().firstOrNull { it.builder.name == name }
    }

    internal fun addMappedValue(name: String, value: Any?) = mapped.put(name, value)

    @Suppress("UNCHECKED_CAST")
    internal fun <T> getValue(argumentContainer: ArgumentContainer<*, *>): T {
        val data = mapped[argumentContainer.builder.name]
        return when (mapped[argumentContainer.builder.name]) {
            null -> {
                if (argumentContainer.default != null) {
                    argumentContainer.default
                } else {
                    if (argumentContainer.required) {
                        error("${argumentContainer.builder.name} was required but is null")
                    }
                    null
                }
            }

            else -> data
        } as T
    }

    override fun toString(): String {
        return buildString {
            append("Arguments(")
            append(getArguments().joinToString(",") {
                "${it.builder.name}=${getValue<Any?>(it)}"
            })
            append(")")
        }
    }
}