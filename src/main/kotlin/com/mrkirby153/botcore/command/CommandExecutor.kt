package com.mrkirby153.botcore.command

import java.util.LinkedList

/**
 * An executor for executing commands
 */
class CommandExecutor {

    val parentNode = SkeletonCommandNode("$\$ROOT$$")

    /**
     * Registers all the methods in the provided class annotated with @[Command] annotation
     *
     * @param instance The class to register
     */
    fun register(instance: Any) {
        val potentialMethods = instance.javaClass.declaredMethods.filter {
            it.getAnnotation(Command::class.java) != null
        }

        potentialMethods.forEach { method ->
            val annotation = method.getAnnotation(Command::class.java)
            val cmdName = annotation.name.split(" ").last()
            val clearance = annotation.clearance
            val arguments = annotation.arguments
            val parent = annotation.parent

            val metadata = CommandMetadata(cmdName, clearance, arguments.toList())

            var parentNode = (if (parent.isNotBlank()) this.parentNode.getChild(
                    parent) else this.parentNode)

            if (parentNode == null) {
                val sk = SkeletonCommandNode(parent)
                this.parentNode.addChild(sk)
                parentNode = sk
            }

            var p = parentNode
            annotation.name.split(" ").forEach { cmd ->
                val child = p!!.getChild(cmd)
                if (child != null) {
                    if (cmd == cmdName) {
                        if (child is SkeletonCommandNode) {
                            // Upgrade to an actual command node
                            p!!.removeChild(child.getName())
                            val node = ResolvedCommandNode(metadata, method, instance)
                            child.getChildren().forEach { c ->
                                node.addChild(c)
                            }
                            p!!.addChild(node)
                        } else {
                            throw IllegalArgumentException(
                                    "Attempted to register a child that already exists??")
                        }
                    }
                    p = child
                } else {
                    val node: CommandNode = if (cmd == cmdName) {
                        ResolvedCommandNode(metadata, method, instance)
                    } else {
                        SkeletonCommandNode(cmd.toLowerCase())
                    }

                    p!!.addChild(node)
                    p = node
                }
            }
        }
    }

    /**
     * Takes a list of arguments (i.e `command1 subcommand1`) and resolves it out of the command tree
     *
     * @param arguments A [LinkedList] of arguments to resolve
     * @param parent The parent node to use
     *
     * @return The command node of the command or null if the command was not found
     */
    @JvmOverloads
    tailrec fun resolve(arguments: LinkedList<String>,
                        parent: CommandNode = parentNode): CommandNode? {
        if (arguments.peek() == null) {
            if (parent == parentNode)
                return null
            return parent
        }
        val childName = arguments.pop()
        val childNode = parent.getChild(childName)
        if (childNode == null) {
            if (parentNode == parent)
                return null
            return parent
        }
        if (arguments.peek() != null && childNode.getChild(arguments.peek()) == null)
            return childNode
        return resolve(arguments, childNode)
    }

    /**
     * Takes a list of arguments and resolves a [CommandNode] out of the command tree
     *
     * @param string The list of arguments
     *
     * @return The [CommandNode] or null if it doesn't exist
     */
    fun resolve(string: String): CommandNode? {
        val l = LinkedList<String>()
        l.addAll(string.split(" "))
        return resolve(l)
    }

    /**
     * Registers a class' methods annotated with @[Command] into the command tree. **Note:** This
     * creates a new instance of the class via [Class.newInstance]
     *
     * @param cmd The class of the command to register
     */
    fun register(cmd: Class<*>) {
        this.register(cmd.newInstance())
    }


    /**
     * Metadata about commands
     */
    data class CommandMetadata(val name: String, val clearance: Int, val arguments: List<String>)
}