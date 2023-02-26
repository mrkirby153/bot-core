package com.mrkirby153.botcore.command.slashcommand.dsl

import com.mrkirby153.botcore.command.CommandException
import com.mrkirby153.botcore.utils.PrerequisiteCheck
import kotlinx.coroutines.runBlocking
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.Command
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions


/**
 * The top-level slash command object from which both [SlashCommands][SlashCommand] and
 * [SubCommands][SubCommand] inherit from.
 *
 * All slash commands have a [name] and [description] which must be set before the command is
 * committed to discord. In addition, [arguments] may be provided to identify the arguments for
 * the slash command
 *
 * @param arguments A function returning a new instance of the class to use for arguments. Arguments
 * are exposed under the `args` variable during execution
 * @param A An arguments class from which slash command arguments are derived from
 * @see [subCommand]
 */
open class AbstractSlashCommand<A : Arguments>(
    private val arguments: (() -> A)?
) {
    internal var body: (SlashContext<A>.() -> Unit)? = null
    internal var coroAction: (suspend SlashContext<A>.() -> Unit)? = null
    private var contexts = mutableListOf<(A.() -> Unit)>()
    lateinit var name: String
    lateinit var description: String

    private var checks = mutableListOf<CommandPrerequisiteCheck<A>.() -> Unit>()

    private var evaluatingContexts = false
    private var checksModified = false

    /**
     * Adds a prerequisite check to this slash command. This check is run before the command is
     * executed. To prevent the command from running and optionally display a message to the user
     * invoke [PrerequisiteCheck.fail] from inside the check.
     *
     * Additional checks can be created by adding extension functions to `CommandPrerequisiteCheck<out Arguments>`
     */
    fun check(builder: CommandPrerequisiteCheck<A>.() -> Unit) {
        checks.add(builder)
        if (evaluatingContexts)
            checksModified = true
    }

    /**
     * Returns a new instance of the arguments class
     */
    internal fun args() = arguments?.invoke()

    /**
     * Executes the slash command. If [body] is null, this no-ops
     */
    internal fun execute(event: SlashCommandInteractionEvent) {
        val ctx = SlashContext(this, event)
        ctx.load()

        val oldChecks = checks.toMutableList()
        val oldBody = body

        try {

            try {
                evaluatingContexts = true
                contexts.forEach {
                    it(ctx.args)
                }
            } finally {
                evaluatingContexts = false
            }

            val checkCtx = CommandPrerequisiteCheck(ctx)
            checks.forEach {
                it(checkCtx)
                if (checkCtx.failed) {
                    return@forEach
                }
            }
            if (checkCtx.failed) {
                throw CommandException(
                    checkCtx.failureMessage ?: "Command prerequisites did not pass"
                )
            }
            body?.invoke(ctx)
            runBlocking {
                coroAction?.invoke(ctx)
            }
        } finally {
            body = oldBody
            if (checksModified)
                checks = oldChecks
            checksModified = false
        }
    }

    /**
     * Handle the autocomplete [event]
     * @return The list of choices derived from the commands autocomplete function, or an empty list
     * if no autocomplete function has been defined
     */
    internal fun handleAutocomplete(event: CommandAutoCompleteInteractionEvent): List<Command.Choice> {
        val argInst =
            args() ?: return listOf(Command.Choice("<<INVALID AUTOCOMPLETE SETTING>>", -1))
        val focused = event.focusedOption.name
        val inst = argInst.getArgument(focused) ?: return emptyList()
        val builder = inst.builder
        val choices = if (builder.autoCompleteCallback != null) {
            builder.autoCompleteCallback!!.invoke(event)
        } else {
            emptyList()
        }
        return choices.map { (k, v) ->
            Command.Choice(k, v)
        }
    }

    /**
     * A function evaluated after arguments are parsed but before the command is executed
     */
    fun context(body: A.() -> Unit) {
        check(!evaluatingContexts) { "Cannot add contexts inside of contexts" }
        contexts.add(body)
    }
}

/**
 * A top level slash command.
 *
 * Slash commands can either have sub-command groups or sub-commands, but not both.
 *
 * The following is how sub-commands and groups relate
 * * `/command subCommand`
 * * `/command group subCommand`
 */
@SlashDsl
class SlashCommand<A : Arguments>(
    arguments: (() -> A)? = null
) : AbstractSlashCommand<A>(arguments) {

    val subCommands = mutableMapOf<String, SubCommand<*>>()
    val groups = mutableMapOf<String, Group>()
    internal var commandPermissions = DefaultMemberPermissions.ENABLED
    var availableInDms = false

    /**
     * Defines the action run when this slash command is invoked
     */
    fun action(action: SlashContext<A>.() -> Unit) {
        check(groups.isEmpty()) { "Cannot mix groups and non-grouped commands" }
        this.body = action
    }

    fun run(action: suspend SlashContext<A>.() -> Unit) {
        check(groups.isEmpty()) { "Cannot mix groups and non-grouped commands" }
        this.coroAction = action
    }

    /**
     * Get a sub command by its [name]
     */
    fun getSubCommand(name: String) = subCommands[name]

    /**
     * Get a sub command by its [group] and [name]
     */
    fun getSubCommand(group: String, name: String) = groups[group]?.getCommand(name)

    /**
     * Sets the default [permissions] for this command.
     *
     * If a user does not have these permissions, the command will not show in the client.
     */
    fun defaultPermissions(vararg permissions: Permission) {
        commandPermissions = DefaultMemberPermissions.enabledFor(*permissions)
    }

    /**
     * Disable this command by default. Server administrators will need to enable it manually
     */
    fun disabledByDefault() {
        commandPermissions = DefaultMemberPermissions.DISABLED
    }

}

/**
 * Wrapper object for command groups
 *
 * @param name The name of the group
 */
@SlashDsl
class Group(
    val name: String,
    val parent: SlashCommand<*>
) {
    val commands = mutableListOf<SubCommand<*>>()
    lateinit var description: String
    fun getCommand(name: String) = commands.firstOrNull { it.name == name }
}

/**
 * Object for sub commands. Behaves identically to [SlashCommand] except it does not allow sub-commands
 * or groups.
 */
@SlashDsl
class SubCommand<A : Arguments>(arguments: (() -> A)? = null) :
    AbstractSlashCommand<A>(arguments) {

    /**
     * The action that this sub-command will run when invoked
     */
    fun action(action: SlashContext<A>.() -> Unit) {
        this.body = action
    }
}