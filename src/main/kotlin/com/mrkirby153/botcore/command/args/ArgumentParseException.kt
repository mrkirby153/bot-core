package com.mrkirby153.botcore.command.args

/**
 * An exception that's thrown if an error occurs when parsing an argument
 *
 * @param msg The error message
 */
class ArgumentParseException(msg: String) : RuntimeException(msg)

class BatchArgumentParseException(val exceptions: Map<String, ArgumentParseException>) :
    RuntimeException(
        buildString {
            appendLine("The following errors have occurred:")
            exceptions.forEach { (k, v) ->
                appendLine("- `$k`: $v")
            }
        })