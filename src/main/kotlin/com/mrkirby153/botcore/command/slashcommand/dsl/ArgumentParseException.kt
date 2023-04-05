package com.mrkirby153.botcore.command.slashcommand.dsl

/**
 * An exception that's thrown if an error occurs when parsing an argument
 *
 * @param msg The error message
 */
class ArgumentParseException(msg: String) : RuntimeException(msg)

/**
 * An exception thrown when multiple errors occur when parsing arguments
 *
 * @param exceptions The exceptions that occurred
 */
class BatchArgumentParseException(val exceptions: Map<String, ArgumentParseException>) :
    RuntimeException(
        buildString {
            appendLine("The following errors have occurred:")
            exceptions.forEach { (k, v) ->
                appendLine("- `$k`: $v")
            }
        })