package com.mrkirby153.botcore.command.slashcommand

import com.mrkirby153.botcore.command.ClearanceResolver
import com.mrkirby153.botcore.command.CommandException
import net.dv8tion.jda.api.entities.Category
import net.dv8tion.jda.api.entities.IMentionable
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.Role
import net.dv8tion.jda.api.entities.TextChannel
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.entities.VoiceChannel
import net.dv8tion.jda.api.events.interaction.command.GenericContextInteractionEvent
import net.dv8tion.jda.api.events.interaction.command.MessageContextInteractionEvent
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.events.interaction.command.UserContextInteractionEvent
import net.dv8tion.jda.api.interactions.commands.build.CommandData
import net.dv8tion.jda.api.interactions.commands.build.Commands
import net.dv8tion.jda.api.interactions.commands.build.OptionData
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData
import net.dv8tion.jda.api.interactions.commands.build.SubcommandGroupData
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method
import javax.annotation.Nullable
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.jvm.javaType
import kotlin.reflect.jvm.kotlinFunction

private val DEFAULT_CLEARANCE_RESOLVER = object : ClearanceResolver {
    override fun resolve(member: Member): Int {
        return if (member.isOwner) {
            100
        } else {
            0
        }
    }

}

class SlashCommandExecutor(
    private val clearanceResolver: ClearanceResolver = DEFAULT_CLEARANCE_RESOLVER
) {

    /**
     * The root node in the slash command tree
     */
    private val rootNode = SlashCommandNode("__ROOT__", path = "")

    private val messageContextCommands = mutableListOf<ContextCommand>()
    private val userContextCommands = mutableListOf<ContextCommand>()

    /**
     * A list of custom slash command resolvers
     */
    private val slashCommandResolvers = mutableMapOf<Class<*>, TypeResolver<*>>()

    private var defaultAvailability: Array<out SlashCommandAvailability> =
        arrayOf(SlashCommandAvailability.DM, SlashCommandAvailability.GUILD)


    init {
        // Add default slash command resolvers
        slashCommandResolvers[String::class.java] = STRING_TYPE_RESOLVER
        slashCommandResolvers[Integer::class.java] = INT_TYPE_RESOLVER
        slashCommandResolvers[Int::class.javaPrimitiveType!!] = INT_TYPE_RESOLVER
        slashCommandResolvers[Long::class.java] = LONG_TYPE_RESOLVER
        slashCommandResolvers[Long::class.javaPrimitiveType!!] = LONG_TYPE_RESOLVER
        slashCommandResolvers[java.lang.Boolean::class.java] = BOOLEAN_TYPE_RESOLVER
        slashCommandResolvers[Boolean::class.java] = BOOLEAN_TYPE_RESOLVER
        slashCommandResolvers[Boolean::class.javaPrimitiveType!!] = BOOLEAN_TYPE_RESOLVER
        slashCommandResolvers[User::class.java] = USER_TYPE_RESOLVER
        slashCommandResolvers[TextChannel::class.java] = TEXT_CHANNEL_TYPE_RESOLVER
        slashCommandResolvers[VoiceChannel::class.java] = VOICE_CHANNEL_TYPE_RESOLVER
        slashCommandResolvers[Category::class.java] = CATEGORY_TYPE_RESOLVER
        slashCommandResolvers[Role::class.java] = ROLE_TYPE_RESOLVER
        slashCommandResolvers[IMentionable::class.java] = MENTIONABLE_TYPE_RESOLVER
        slashCommandResolvers[Enum::class.java] = ENUM_TYPE_RESOLVER
    }

    /**
     * Discovers all slash commands on the provided instance and registers them. Specify [clazz] to
     * override the class when discovering commands. If left blank, [Object.getClass] will be
     * used instead
     */
    @JvmOverloads
    fun discoverAndRegisterSlashCommands(instance: Any, clazz: Class<*>? = null) {
        val providedClass = clazz ?: instance.javaClass
        providedClass.declaredMethods.filter { it.isAnnotationPresent(SlashCommand::class.java) }
            .forEach { method ->
                if (method.getAnnotation(SlashCommand::class.java) != null) {
                    registerSlashCommand(instance, method)
                } else if (method.getAnnotation(UserCommand::class.java) != null) {
                    registerContextCommand(
                        instance,
                        method,
                        userContextCommands,
                        UserCommand::class.java
                    )
                } else if (method.getAnnotation(MessageCommand::class.java) != null) {
                    registerContextCommand(
                        instance,
                        method,
                        messageContextCommands,
                        MessageCommand::class.java
                    )
                }
            }
    }

    private fun registerSlashCommand(instance: Any, method: Method) {
        val annotation = method.getAnnotation(SlashCommand::class.java)
        val node = resolveNode(annotation.name, true)!!
        node.options = discoverOptions(method)
        node.classInstance = instance
        node.description = annotation.description
        node.clearance = annotation.clearance
        node.availability = annotation.availability
        node.method = method
    }

    private fun registerContextCommand(
        instance: Any,
        method: Method,
        list: MutableList<ContextCommand>,
        clazz: Class<out Annotation>
    ) {
        val cmd = when (val annotation = method.getAnnotation(clazz)) {
            is UserCommand -> {
                if (list.size > Commands.MAX_USER_COMMANDS)
                    throw IllegalArgumentException("Can't register more than ${Commands.MAX_USER_COMMANDS} user context commands")
                ContextCommand(annotation.name, method, instance, annotation.clearance)
            }
            is MessageCommand -> {
                if (list.size > Commands.MAX_MESSAGE_COMMANDS)
                    throw IllegalArgumentException("Can't register more than ${Commands.MAX_MESSAGE_COMMANDS} message context commands")
                ContextCommand(annotation.name, method, instance, annotation.clearance)
            }
            else -> throw IllegalArgumentException("Unrecognized type $annotation")
        }
        list.add(cmd)
    }

    /**
     * Associates the provided [typeResolver] with the given [class]
     */
    fun registerTypeResolver(`class`: Class<*>, typeResolver: TypeResolver<*>) {
        if (slashCommandResolvers.containsKey(`class`)) {
            throw java.lang.IllegalArgumentException("$`class` has a previously associated resolver")
        }
        slashCommandResolvers[`class`] = typeResolver
    }

    /**
     * Override the default availability of slash commands
     */
    fun setDefaultCommandAvailability(vararg availability: SlashCommandAvailability) {
        defaultAvailability = availability
    }

    /**
     * Flattens the slash command tree into a list of [CommandData] for registration
     */
    fun flattenSlashCommands(): List<CommandData> {
        val commands = mutableListOf<CommandData>()
        rootNode.children.forEach { node ->
            val data = Commands.slash(node.name, node.description)
            val nodeChildren = node.children
            var hasGroupChildren = false
            var hasSubCommand = false
            nodeChildren.forEach { child ->
                if (child.children.size > 0) {
                    if (hasSubCommand) {
                        throw IllegalArgumentException("Cannot flatten: ${child.path} has sub commands, and this command is a group which is not permitted by the API")
                    }
                    hasGroupChildren = true
                } else {
                    if (hasGroupChildren) {
                        throw java.lang.IllegalArgumentException("Cannot flatten: ${child.path} has group children and this command does not, which is not permitted by the API")
                    }
                    hasSubCommand = true;
                }
            }
            when {
                hasSubCommand -> {
                    nodeChildren.forEach {
                        val subcommandData = SubcommandData(it.name, it.description)
                        subcommandData.addOptions(it.options)
                        data.addSubcommands(subcommandData)
                    }
                }
                hasGroupChildren -> {
                    nodeChildren.forEach { child ->
                        val subChildren = child.children
                        val subcommandGroupData = SubcommandGroupData(child.name, child.description)
                        subChildren.forEach { subChild ->
                            val subCommandData = SubcommandData(subChild.name, subChild.description)
                            subCommandData.addOptions(subChild.options)
                            subcommandGroupData.addSubcommands(subCommandData)
                        }
                        data.addSubcommandGroups(subcommandGroupData)
                    }
                }
                else -> {
                    data.addOptions(node.options)
                }
            }
            commands.add(data)
        }

        commands.addAll(userContextCommands.map { Commands.user(it.name) })
        commands.addAll(messageContextCommands.map { Commands.message(it.name) })
        return commands
    }


    /**
     * Resolves a [SlashCommandNode] for the given [path]. Set [create] to true to create
     * intermediary nodes
     */
    private fun resolveNode(path: String, create: Boolean = true): SlashCommandNode? {
        val parts = path.split(" ")
        var curr = this.rootNode
        parts.forEach {
            curr = curr.getChildByName(it)
                ?: if (create) {
                    val newChild = SlashCommandNode(it, path = "${curr.path}.${it}")
                    curr.children.add(newChild)
                    newChild
                } else {
                    return null
                }
        }
        return curr
    }

    private fun discoverOptions(method: Method): List<OptionData> {
        val options = mutableListOf<OptionData>()
        method.trySetAccessible()
        fun addChoices(resolver: TypeResolver<*>, type: Class<*>, data: OptionData) {
            if (resolver.optionType.canSupportChoices()) {
                val choices = resolver.optionResolver.invoke(type)
                if (choices.size > 25)
                    throw IllegalArgumentException("More than 25 choices for option $type")
                choices.forEach { (internal, friendly) ->
                    data.addChoice(friendly, internal)
                }
            }
        }
        if (method.kotlinFunction != null) {
            // This is a kotlin function
            val kFunction =
                method.kotlinFunction ?: throw IllegalArgumentException("This should never happen")
            kFunction.parameters.filter { it.findAnnotation<SlashCommandParameter>() != null }
                .forEach { p ->
                    val type = p.type.javaType as Class<*>
                    val resolver = getResolver(type)
                        ?: throw IllegalArgumentException("No resolver for option type ${p.name} (${p.type}) on ${method.declaringClass}#${method.name}")
                    val annotation =
                        p.findAnnotation<SlashCommandParameter>() ?: throw IllegalArgumentException(
                            "This should never happen"
                        )
                    val data =
                        OptionData(resolver.optionType, annotation.name, annotation.description)
                    addChoices(resolver, type, data)
                    // If the type is non-null this option should be required
                    data.isRequired = !p.type.isMarkedNullable
                    if ((p.type as? Class<*>)?.isPrimitive == true) {
                        data.isRequired = true
                    }
                    options.add(data)
                }
        } else {
            // This is a java function
            method.parameters.filter { it.isAnnotationPresent(SlashCommandParameter::class.java) }
                .forEach { p ->
                    val resolver = getResolver(p.type)
                        ?: throw IllegalArgumentException("No resolver for option type ${p.name} (${p.type}) on ${method.declaringClass}#${method.name}")
                    val annotation = p.getAnnotation(SlashCommandParameter::class.java)
                    val data =
                        OptionData(resolver.optionType, annotation.name, annotation.description)
                    addChoices(resolver, p.type, data)
                    // If the nonnull annotation is present, set it as non-null
                    data.isRequired = !p.isAnnotationPresent(Nullable::class.java)
                    if (p.type.isPrimitive) {
                        data.isRequired = true
                    }
                    options.add(data)
                }
        }
        return options
    }

    /**
     * Returns true if this slash command executor can execute the command from the provided [event]
     */
    fun canExecute(event: SlashCommandInteractionEvent) =
        resolveNode(getCommandName(event), false) != null


    private fun getCommandName(event: SlashCommandInteractionEvent) = buildString {
        append(event.name)
        if (event.subcommandGroup != null) {
            append(" ${event.subcommandGroup} ${event.subcommandName}")
        } else if (event.subcommandName != null) {
            append(" ${event.subcommandName}")
        }
    }

    private fun getResolver(type: Class<*>): TypeResolver<*>? {
        var curr: Class<*>? = type
        var resolver: TypeResolver<*>?
        do {
            resolver = slashCommandResolvers[curr]
            curr = curr?.superclass
        } while (curr != null && resolver == null)
        return resolver
    }

    /**
     * Executes the provided [event]'s slash command if this executor is able (A command exists in
     * its tree). Returns true if this executor was able to execute the slash command
     */
    fun executeSlashCommandIfAble(event: SlashCommandInteractionEvent): Boolean {
        return if (canExecute(event)) {
            executeSlashCommand(event)
            true
        } else {
            false
        }
    }

    fun executeContextInteraction(event: GenericContextInteractionEvent<*>): Boolean {
        val toConsider = when (event) {
            is MessageContextInteractionEvent -> {
                messageContextCommands
            }
            is UserContextInteractionEvent -> {
                userContextCommands
            }
            else -> {
                // Ignore
                emptyList()
            }
        }
        val target = toConsider.firstOrNull { it.name == event.name } ?: return false
        try {
            val clearance = if (event.isFromGuild) clearanceResolver.resolve(event.member!!) else 0
            if (clearance < target.clearance) {
                event.reply(":lock: You do not have permission to use this command")
                    .setEphemeral(true).queue()
            } else {
                target.method.invoke(target.instance, event)
            }
        } catch (e: InvocationTargetException) {
            val cause = e.cause ?: e
            if (cause is CommandException) {
                event.reply(":no_entry: ${cause.message ?: "An unknown error occurred"}")
                    .setEphemeral(true).queue()
            } else {
                event.reply(":no_entry: Command Failed: ${cause.message ?: "An unknown error occurred"}")
                    .setEphemeral(true).queue()
            }
        }
        return true
    }

    /**
     * Executes the slash command invoked by the provided [event]
     */
    fun executeSlashCommand(event: SlashCommandInteractionEvent) {
        try {
            val command = getCommandName(event)
            val node = resolveNode(command, false)

            if (node != null) {
                // Check usability and permissions
                val availability: Array<out SlashCommandAvailability> =
                    if (node.availability.contentEquals(emptyArray())) defaultAvailability else node.availability
                if (event.isFromGuild) {
                    if (SlashCommandAvailability.GUILD !in availability) {
                        event.reply(":no_entry: You can only use this command in DMs")
                            .setEphemeral(true).queue()
                        return
                    }
                } else {
                    if (SlashCommandAvailability.DM !in availability) {
                        event.reply(":no_entry: You can only use this command in servers")
                            .setEphemeral(true).queue()
                        return
                    }
                }
                // Check permission
                val clearance = if (event.isFromGuild) {
                    clearanceResolver.resolve(event.member!!)
                } else {
                    0
                }
                if (clearance < node.clearance) {
                    event.reply(":lock: You do not have permission to perform this command")
                        .setEphemeral(true).queue()
                    return
                }
                val methodParams = node.method!!.parameters
                val parameters = arrayOfNulls<Any>(methodParams.size)
                parameters[0] = event
                methodParams.forEachIndexed { i, param ->
                    if (i == 0) {
                        // Param 0 is always the SlashCommandEvent
                        return@forEachIndexed
                    }
                    val annotation = param.getAnnotation(SlashCommandParameter::class.java)
                    val map = event.getOption(annotation.name) ?: return@forEachIndexed
                    val paramType = param.type
                    val resolver = getResolver(paramType)
                        ?: throw TypeResolutionException("Unknown type resolver for type $paramType")
                    try {
                        parameters[i] = resolver.resolver.invoke(map, paramType)
                    } catch (e: TypeResolutionException) {
                        event.reply(":no_entry: `${map.name}`: ${e.message ?: "An unknown error occurred"}")
                            .setEphemeral(true).queue()
                        return
                    }
                }
                try {
                    node.method!!.invoke(node.classInstance, *parameters)
                } catch (e: InvocationTargetException) {
                    val cause = e.cause
                    if (cause is CommandException) {
                        event.reply(":no_entry: " + if (cause.message != null) cause.message else "An unknown error occurred")
                            .setEphemeral(true).queue()
                        return
                    }
                    e.printStackTrace()
                    event.reply(":no_reply: Command Failed: " + if (e.message != null) e.message else "An unknown error occurred")
                        .setEphemeral(true).queue()
                }
            } else {
                event.reply(":no_entry: Something went wrong, and this command does not exist!")
                    .setEphemeral(true).queue()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            event.reply(":no_entry: Something went wrong executing your command: ${e.message}")
                .setEphemeral(true).queue()
        }
    }
}
