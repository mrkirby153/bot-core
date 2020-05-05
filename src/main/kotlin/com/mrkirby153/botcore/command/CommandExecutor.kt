package com.mrkirby153.botcore.command

import com.mrkirby153.botcore.command.args.ArgumentParseException
import com.mrkirby153.botcore.command.args.ArgumentParser
import com.mrkirby153.botcore.command.args.CommandContext
import com.mrkirby153.botcore.shard.ShardManager
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.ChannelType
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.Message
import java.lang.reflect.InvocationTargetException
import java.util.LinkedList
import java.util.regex.Pattern
import kotlin.math.roundToInt

/**
 * An executor for executing commands
 */

open class CommandExecutor(private val prefix: String,
                           private val mentionMode: MentionMode = MentionMode.OPTIONAL,
                           private val jda: JDA? = null,
                           private val shardManager: ShardManager? = null) {

    private val parentNode = SkeletonCommandNode("$\$ROOT$$")

    lateinit var clearanceResolver: (Member) -> Int

    var alertUnknownCommand = true
    var alertNoClearance = true

    private val resolvers = mutableMapOf<String, (LinkedList<String>) -> Any?>()

    init {
        this.registerDefaultResolvers()
    }

    /**
     * Registers all the methods in the provided class annotated with @[Command] annotation
     *
     * If `declaredOnly` is set to true, then only methods returned by [Class.getDeclaredMethods]
     * will be scanned. Otherwise, methods returned by [Class.getMethods] will be scanned
     *
     * @param instance The class to register
     * @param clazz The class of the instance. If null, the instance's [Object.getClass] method
     * will be used
     * @param declaredOnly If only methods declared in the class should be scanned
     */
    @JvmOverloads
    fun register(instance: Any, clazz: Class<*>? = null, declaredOnly: Boolean = true) {
        val effectiveClass = clazz ?: instance.javaClass
        val methods = if (declaredOnly) effectiveClass.declaredMethods else effectiveClass.methods;
        val potentialMethods = methods.filter {
            it.isAnnotationPresent(Command::class.java)
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

    fun execute(message: Message) {
        var raw = message.contentRaw
        if (raw.isEmpty())
            return

        val botId = message.guild.selfMember.user.id
        val isMention = raw.matches(Regex("^<@!?$botId>.*"))

        when (mentionMode) {
            MentionMode.REQUIRED -> {
                if (!isMention)
                    return
            }
            MentionMode.OPTIONAL -> {
                if (!isMention) {
                    if (!raw.startsWith(prefix))
                        return
                }
            }
            MentionMode.DISABLED -> {
                if (!raw.startsWith(prefix))
                    return
            }
        }

        raw = if (isMention) raw.replace(Regex("^<@!?$botId>\\s?"), "") else raw.substring(
                prefix.length)
        val parts = raw.split(" ")
        if (parts.isEmpty())
            return

        val cmdArray = LinkedList(raw.split(" "))

        val resolved = resolve(cmdArray)

        if (resolved == null || resolved !is ResolvedCommandNode) {
            if (alertUnknownCommand)
                message.channel.sendMessage(":no_entry: That command does not exist").queue()
            return
        }

        val userClearance = if (message.channelType == ChannelType.PRIVATE) 0 else this.clearanceResolver.invoke(
                message.member!!)
        val metadata = resolved.metadata

        if (userClearance < metadata.clearance) {
            if (alertNoClearance)
                message.channel.sendMessage(
                        ":lock: You do not have permission to perform this command").queue()
            return
        }

        val arguments = metadata.arguments

        val parser = ArgumentParser(cmdArray.toTypedArray(), arguments.toTypedArray(), this)
        val cmdContext: CommandContext = try {
            parser.parse()
        } catch (e: ArgumentParseException) {
            message.channel.sendMessage(
                    ":no_entry: ${e.message ?: "An unknown error occurred"}").queue()
            return
        }

        val method = resolved.method
        val context = Context(message)
        context.clearance = userClearance
        context.commandName = metadata.name
        context.commandPrefix = if (isMention) "<@$botId>" else prefix

        try {
            method.invoke(resolved.instance, context, cmdContext)
        } catch (e: InvocationTargetException) {
            if (e.targetException is CommandException) {
                message.channel.sendMessage(":no_entry: ${e.targetException.message}").queue()
            } else {
                e.printStackTrace()
                message.channel.sendMessage(":no_entry: An unknown error occurred").queue()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            message.channel.sendMessage(":no_entry: An unknown error occurred").queue()
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
        this.register(cmd.getConstructor().newInstance())
    }

    /**
     * Adds a context resolver for resolving arguments
     *
     * @param name The name of the resolver
     * @param resolver The argument resolver
     */
    fun addContextResolver(name: String, resolver: (LinkedList<String>) -> Any?) {
        if (this.resolvers.containsKey(name.toLowerCase()))
            throw IllegalArgumentException("The argument resolver '$name' is already registered")
        this.resolvers[name] = resolver
    }

    /**
     * Gets a context resolver by its name
     *
     * @param name The name of the resolver
     */
    fun getContextResolver(name: String) = this.resolvers[name]


    private fun registerDefaultResolvers() {
        // String resolver -- Matches strings surrounded in quotes
        addContextResolver("string") { args ->
            if (args.peek().matches(Regex("^(?<!\\\\)\".*"))) {
                val string = buildString {
                    while (true) {
                        if (args.peek() == null) {
                            throw ArgumentParseException("Unmatched quotes")
                        }
                        val next = args.pop()
                        append("$next ")
                        if (next.matches(Regex(".*(?<!\\\\)\"\$"))) {
                            break
                        }
                    }
                }
                string.trim().substring(1..(string.length - 3)).replace("\\\"", "\"")
            } else {
                args.pop()
            }
        }
        addContextResolver("string...") { args ->
            buildString {
                while (args.peek() != null)
                    append("${args.pop()} ")
            }.trim().replace(Regex("^(?<!\\\\)\\\""), "").replace(Regex("(?<!\\\\)\\\"\$"), "")
        }
        addContextResolver("snowflake") { args ->
            val first = args.pop()
            if (first.matches(Regex("<@!?\\d{17,18}>"))) {
                val pattern = Pattern.compile("\\d{17,18}")
                val matcher = pattern.matcher(first)
                if (matcher.find()) {
                    try {
                        return@addContextResolver matcher.group()
                    } catch (e: IllegalStateException) {
                        throw ArgumentParseException("Could not convert `$first` to `snowflake`")
                    }
                }
            }
            if (!first.matches(Regex("\\d{17,18}")))
                throw ArgumentParseException("Could not convert `$first` to `snowflake`")
            return@addContextResolver first
        }
        addContextResolver("user") { args ->
            val m = args.peek()
            val id = getContextResolver("snowflake")?.invoke(args) as? String
                    ?: throw ArgumentParseException("Could not convert `$m` to `user`")

            when {
                shardManager != null -> shardManager.getUserById(id)
                jda != null -> jda.getUserById(id)
                else -> null
            }
        }
        addContextResolver("number") { args ->
            val num = args.pop()
            num.toDoubleOrNull() ?: throw ArgumentParseException(
                    "Could not convert `$num` to `number`")
        }
        addContextResolver("int") { args ->
            val m = args.peek()
            (getContextResolver("number")?.invoke(args) as? Double)?.roundToInt()
                    ?: throw ArgumentParseException("Could not convert `$m` to `int`")
        }
    }

    /**
     * Metadata about commands
     */
    data class CommandMetadata(val name: String, val clearance: Int, val arguments: List<String>)

    enum class MentionMode {
        DISABLED,
        OPTIONAL,
        REQUIRED
    }
}