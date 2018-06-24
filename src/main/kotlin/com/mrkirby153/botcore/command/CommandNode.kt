package com.mrkirby153.botcore.command

import java.lang.reflect.Method

/**
 * A skeleton command (one without a method executor attached). Used for keeping track of empty
 * branches in the command tree
 */
class SkeletonCommandNode(private val name: String) : CommandNode {

    private val children = mutableListOf<CommandNode>()

    override fun addChild(node: CommandNode) {
        if (node.getName() in children.map { it.getName() })
            throw IllegalArgumentException(
                    "The child '${node.getName()}' already exists on this node")
        children.add(node)
    }

    override fun getChild(name: String): CommandNode? {
        return children.firstOrNull { it.getName().equals(name, true) }
    }

    override fun removeChild(name: String) {
        children.removeIf { it.getName().equals(name, true) }
    }

    override fun getName(): String {
        return name
    }

    override fun toString(): String {
        return buildString {
            append("Skeleton[${this@SkeletonCommandNode.getName()}]{${children.joinToString(
                    ", ")}}")
        }
    }

    override fun getChildren(): List<CommandNode> {
        return this.children
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as SkeletonCommandNode

        if (name != other.name) return false

        return true
    }

    override fun hashCode(): Int {
        return name.hashCode()
    }

}

/**
 * A node with a method, metadata, and instance attached.
 */
class ResolvedCommandNode(val metadata: CommandExecutor.CommandMetadata, val method: Method,
                          val instance: Any) : CommandNode {

    private val children = mutableListOf<CommandNode>()

    override fun addChild(node: CommandNode) {
        if (node.getName() in children.map { it.getName() })
            throw IllegalArgumentException(
                    "The child '${node.getName()}' already exists on this node")
        children.add(node)
    }

    override fun getChild(name: String): CommandNode? {
        return children.firstOrNull { it.getName().equals(name, true) }
    }

    override fun getName(): String {
        return metadata.name
    }

    override fun removeChild(name: String) {
        children.removeIf { it.getName().equals(name, true) }
    }

    override fun getChildren(): List<CommandNode> {
        return this.children
    }

    override fun toString(): String {
        return buildString {
            append("Resolved[${this@ResolvedCommandNode.getName()}]{${children.joinToString(
                    ", ")}}")
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as SkeletonCommandNode

        if (getName() != other.getName()) return false

        return true
    }

    override fun hashCode(): Int {
        return getName().hashCode()
    }

}

/**
 *  A node in the Command tree
 */
interface CommandNode {
    /**
     * Adds a child to the tree
     */
    fun addChild(node: CommandNode)

    /**
     * Gets an immediate child of this node
     *
     * @return The [CommandNode] or null if it wasn't found
     */
    fun getChild(name: String): CommandNode?

    /**
     * Removes an immediate child of this node
     */
    fun removeChild(name: String)

    /**
     * Gets the name of this node
     *
     * @return The name of the node
     */
    fun getName(): String

    /**
     * Gets all the children of this node
     *
     * @return A list of all the children
     */
    fun getChildren(): List<CommandNode>
}
