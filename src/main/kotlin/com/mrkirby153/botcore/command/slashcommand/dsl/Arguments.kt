package com.mrkirby153.botcore.command.slashcommand.dsl

import com.mrkirby153.botcore.command.slashcommand.dsl.types.enum
import com.mrkirby153.botcore.command.slashcommand.dsl.types.int
import com.mrkirby153.botcore.command.slashcommand.dsl.types.string
import kotlin.reflect.KProperty

/**
 * A top level class for slash command arguments.
 *
 * To use arguments in slash commands, create a class that extends [Arguments]. Arguments can then
 * be declared using the various argument delegators ([string], [int], [enum], etc.)
 */
open class Arguments {
    private val arguments = mutableListOf<ArgumentContainer<*, *>>()

    private val mapped = HashMap<String, Any?>()

    internal fun addArgument(container: ArgumentContainer<*, *>) = arguments.add(container)
    internal fun getArguments() = arguments.toList().sortedBy { arg -> !arg.required }

    internal fun getArgument(name: String) = arguments.firstOrNull { it.builder.name == name }

    internal fun addMappedValue(name: String, value: Any?) = mapped.put(name, value)

    @Suppress("UNCHECKED_CAST")
    operator fun <T : Any?, U : T> ArgumentContainer<T, U>.getValue(
        o: Arguments,
        desc: KProperty<*>
    ): T {
        val data = mapped[builder.name]
        return when (mapped[builder.name]) {
            null -> {
                if (required) {
                    error("${builder.name} was required but is null")
                }
                null
            }

            else -> data
        } as T
    }
}