package com.mrkirby153.botcore.command.slashcommand

import net.dv8tion.jda.api.interactions.commands.build.OptionData
import java.lang.reflect.Method

class SlashCommandNode(val name: String, var description: String = "No description provided", val path: String) {

    val children = mutableListOf<SlashCommandNode>()
    var clearance = 0
    var classInstance: Any? = null
    var method: Method? = null
    var options: List<OptionData> = mutableListOf()
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