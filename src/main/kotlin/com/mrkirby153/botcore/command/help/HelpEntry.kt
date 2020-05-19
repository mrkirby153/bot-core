package com.mrkirby153.botcore.command.help

/**
 * A help data class for command help
 */
class HelpEntry(val command: String, val rawArgs: List<String>, val argString: String,
                val description: String) {
    /**
     * The full command consisting of the command and its arguments
     */
    val fullCommand = "$command $argString"
}