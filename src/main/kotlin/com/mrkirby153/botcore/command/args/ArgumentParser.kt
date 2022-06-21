package com.mrkirby153.botcore.command.args

import com.mrkirby153.botcore.command.CommandExecutor
import java.util.LinkedList

/**
 * Parse a list of arguments
 *
 * @param arguments The list of parameters to parse
 * @param argumentTypes The types of the arguments
 * @param executor The [CommandExecutor] that's currently executing the command
 */
class ArgumentParser(private val arguments: Array<String>, private val argumentTypes: Array<String>,
                     private val executor: CommandExecutor) {

    /**
     * Parse the arguments and returns a [CommandContext]
     */
    fun parse(): CommandContext {
        val argList = LinkedList<String>()
        arguments.forEach { argList.add(it) }
        val context = CommandContext()

        for (i in argumentTypes.indices) {
            var argument = argumentTypes[i]
            val argType = ArgType.determine(argument)
            argument = argument.replace(Regex("<|>|\\[|]"), "")
            val parts = argument.split(":")
            val name = parts[0]
            val type = parts[1]
            if (argType == ArgType.REQUIRED && argList.peek() == null)
                throw ArgumentParseException("The argument `${argumentTypes[i]}` is required")
            if (argList.peek() != null) {
                val resolver = executor.getContextResolver(type) ?: throw ArgumentParseException(
                        "The resolver `$type` is not found")
                val data = resolver.resolve(argList)
                context.put(name, data)
            }
        }
        return context
    }

    private enum class ArgType {
        UNKNOWN,
        REQUIRED,
        OPTIONAL;

        companion object {
            fun determine(arg: String): ArgType {
                val requiredPattern = Regex("^<.*>$")
                val optionalPattern = Regex("^\\[.*]$")

                return when {
                    optionalPattern.matches(arg) -> OPTIONAL
                    requiredPattern.matches(arg) -> REQUIRED
                    else -> UNKNOWN
                }
            }
        }
    }
}