package com.mrkirby153.botcore.command

import com.mrkirby153.botcore.command.args.ArgumentParseException
import com.mrkirby153.botcore.command.args.ArgumentParser
import com.mrkirby153.botcore.command.args.CommandContext
import com.mrkirby153.botcore.command.args.CommandContextResolver
import com.mrkirby153.botcore.command.help.Description
import com.mrkirby153.botcore.command.help.HelpEntry
import com.mrkirby153.botcore.command.help.Hidden
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.ChannelType
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.Message
import java.lang.reflect.InvocationTargetException
import java.util.LinkedList
import java.util.function.BiFunction
import java.util.function.Function
import java.util.regex.Pattern
import kotlin.math.roundToInt

/**
 * An executor for executing commands
 */

open class CommandExecutor(private val prefix: String,
                           private val mentionMode: MentionMode = MentionMode.OPTIONAL,
                           private val jda: JDA? = null,
                           private val shardManager: net.dv8tion.jda.api.sharding.ShardManager? = null) {

    private val parentNode = SkeletonCommandNode("$\$ROOT$$")

    lateinit var clearanceResolver: ClearanceResolver

    var alertUnknownCommand = true
    var alertNoClearance = true

    private val resolvers = mutableMapOf<String, CommandContextResolver>()

    private var prefixResolver: (Message) -> String = {
        prefix // Default we want to just return the prefix
    }

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

            val desc = method.getAnnotation(
                    Description::class.java)?.value ?: "No description provided"

            val metadata = CommandMetadata(cmdName, clearance, arguments.toList(),
                    method.isAnnotationPresent(
                            Hidden::class.java), desc, annotation.permissions)

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

        val prefix = this.prefixResolver.invoke(message)

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

        val userClearance = if (message.channelType == ChannelType.PRIVATE) 0 else this.clearanceResolver.resolve(
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
            message.channel.sendMessage(buildString {
                appendln(":no_entry: ${e.message ?: "An unknown error occurred"}")
                appendln(
                        "Usage: `${prefix}${resolved.metadata.name} ${resolved.metadata.arguments.joinToString(
                                " ")}`")
            }).queue()
            return
        }

        val method = resolved.method
        val context = Context(message)
        context.clearance = userClearance
        context.commandName = metadata.name
        context.commandPrefix = if (isMention) "<@$botId>" else prefix

        if (resolved.metadata.permissions.isNotEmpty()) {
            // Check permissions
            val missingPerms = mutableListOf<Permission>()
            resolved.metadata.permissions.forEach { permission ->
                if (!message.guild.selfMember.hasPermission(message.textChannel, permission)) {
                    missingPerms.add(permission)
                }
            }
            if (missingPerms.isNotEmpty()) {
                if (message.guild.selfMember.hasPermission(message.textChannel,
                                Permission.MESSAGE_SEND))
                    message.channel.sendMessage(
                            ":no_entry: I am missing the following permissions: `${missingPerms.joinToString(
                                    ", ") { it.getName() }}").queue()
                return
            }
        }
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
    fun addContextResolver(name: String, resolver: CommandContextResolver) {
        if (this.resolvers.containsKey(name.toLowerCase()))
            throw IllegalArgumentException("The argument resolver '$name' is already registered")
        this.resolvers[name] = resolver
    }

    /**
     * Adds a context resolver for resolving arguments.
     *
     * **Note:** This wraps the Kotlin lambda in a [CommandContextResolver]
     */
    fun addKContextResolver(name: String, resolver: (LinkedList<String>) -> Any?) {
        addContextResolver(name, object : CommandContextResolver {
            override fun resolve(params: LinkedList<String>): Any? {
                return resolver.invoke(params)
            }
        })
    }

    /**
     * Gets a context resolver by its name
     *
     * @param name The name of the resolver
     */
    fun getContextResolver(name: String) = this.resolvers[name]


    /**
     * Gets the command help. If a member is provided, only the commands that the member can access
     * will be displayed in the help
     *
     * @param member The member to check clearance against
     * @param node The command node to start from
     * @param path The current command path
     */
    @JvmOverloads
    fun getHelp(member: Member? = null, node: CommandNode = parentNode,
                path: String = ""): List<HelpEntry> {
        val entries = mutableListOf<HelpEntry>()
        node.getChildren().forEach { child ->
            if (child is ResolvedCommandNode) {
                val metadata = child.metadata
                if (member == null || metadata.clearance <= clearanceResolver.resolve(member)) {
                    if (!metadata.hidden)
                        entries.add(HelpEntry(path + " " + child.getName(), metadata.arguments,
                                metadata.arguments.joinToString(" "), metadata.help))
                }
            }
            entries.addAll(getHelp(member, child, path + " " + child.getName()))
        }
        return entries;
    }


    private fun registerDefaultResolvers() {
        // String resolver -- Matches strings surrounded in quotes
        addKContextResolver("string") { args ->
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
        addKContextResolver("string...") { args ->
            buildString {
                while (args.peek() != null)
                    append("${args.pop()} ")
            }.trim().replace(Regex("^(?<!\\\\)\\\""), "").replace(Regex("(?<!\\\\)\\\"\$"), "")
        }
        addKContextResolver("snowflake") { args ->
            val first = args.pop()
            if (first.matches(Regex("<@!?\\d{17,18}>"))) {
                val pattern = Pattern.compile("\\d{17,18}")
                val matcher = pattern.matcher(first)
                if (matcher.find()) {
                    try {
                        return@addKContextResolver matcher.group()
                    } catch (e: IllegalStateException) {
                        throw ArgumentParseException(
                                "Could not convert `$first` to `snowflake`")
                    }
                }
            }
            if (!first.matches(Regex("\\d{17,18}")))
                throw ArgumentParseException("Could not convert `$first` to `snowflake`")
            return@addKContextResolver first
        }
        addKContextResolver("user") { args ->
            val m = args.peek()
            val id = getContextResolver("snowflake")?.resolve(args) as? String
                    ?: throw ArgumentParseException("Could not convert `$m` to `user`")

            when {
                shardManager != null -> shardManager.getUserById(id)
                jda != null -> jda.getUserById(id)
                else -> null
            }
        }
        addKContextResolver("number") { args ->
            val num = args.pop()
            num.toDoubleOrNull() ?: throw ArgumentParseException(
                    "Could not convert `$num` to `number`")
        }
        addKContextResolver("int") { args ->
            val m = args.peek()
            (getContextResolver("number")?.resolve(args) as? Double)?.roundToInt()
                    ?: throw ArgumentParseException("Could not convert `$m` to `int`")
        }
    }


    /**
     * Overrides the bot's prefix resolver with the given [resolver]
     */
    fun overridePrefixResolver(resolver: (Message) -> String) {
        this.prefixResolver = resolver
    }

    /**
     * Overrides the bot's prefix resolver with the given [resolver]
     */
    fun overridePrefixResolver(resolver: Function<Message, String>) {
        overridePrefixResolver {
            resolver.apply(it)
        }
    }

    /**
     * Metadata about commands
     */
    data class CommandMetadata(val name: String, val clearance: Int,
                               val arguments: List<String>,
                               val hidden: Boolean, val help: String,
                               val permissions: Array<Permission>)

    enum class MentionMode {
        DISABLED,
        OPTIONAL,
        REQUIRED
    }
}