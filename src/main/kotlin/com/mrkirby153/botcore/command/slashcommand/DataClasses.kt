package com.mrkirby153.botcore.command.slashcommand

import net.dv8tion.jda.api.interactions.commands.build.OptionData
import java.lang.reflect.Method

/**
 * A node for a slash command in the slash command tree
 */
class SlashCommandNode(
    /**
     * The name of the slash command
     */
    val name: String,
    /**
     * The description of the slash command
     */
    var description: String = "No description provided",
    /**
     * The path in the tree of this slash command
     */
    val path: String
) {

    /**
     * A list of [SlashCommandNode] children
     */
    val children = mutableListOf<SlashCommandNode>()

    /**
     * The clearance required to execute this slash command
     */
    var clearance = 0

    /**
     * The class instance holding method of this slash command
     */
    var classInstance: Any? = null

    /**
     * The method that this slash command correlates to
     */
    var method: Method? = null

    /**
     * A list of options for this slash command
     */
    var options: List<OptionData> = mutableListOf()

    /**
     * Where this slash command is available: Guilds, DMs, or Both
     */
    var availability: Array<SlashCommandAvailability> = emptyArray()


    /**
     * Gets the child node by the provided [name]. Returns the node or null if no child exists with
     * that name
     */
    fun getChildByName(name: String): SlashCommandNode? {
        children.forEach { child ->
            if (child.name == name) {
                return child
            }
        }
        return null
    }
}

/**
 * Data class for context (right click menu) commands
 */
data class ContextCommand(
    /**
     * The name of the context command
     */
    val name: String,
    /**
     * The method invoked when this context command is ran
     */
    val method: Method,
    /**
     * The instance to invoke the method on
     */
    val instance: Any,
    /**
     * The clearance needed to invoke this command
     */
    val clearance: Int = 0
)
