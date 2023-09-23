package com.mrkirby153.botcore.command.slashcommand.dsl

import kotlinx.coroutines.CoroutineScope
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.Command
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions

open class AbstractSlashCommand(
    val name: String
) {
    internal val arguments = mutableMapOf<String, ArgumentContainer<*, *>>()
    var description: String = "No description provided"

    internal var action: (suspend SlashContext.() -> Unit)? = null

    /**
     * Executes the slash command. If [body] is null, this is a no-op
     */
    internal suspend fun execute(event: SlashCommandInteractionEvent, scope: CoroutineScope) {
        val context = SlashContext(this, event, scope)
        action?.invoke(context)
    }

    internal fun handleAutocomplete(event: CommandAutoCompleteInteractionEvent): List<Command.Choice> {
        val focused = event.focusedOption.name
        val container = arguments[focused] ?: return emptyList()
        val builder = container.builder
        val choices = if (builder.autoCompleteCallback != null) {
            builder.autoCompleteCallback!!.invoke(event)
        } else {
            emptyList()
        }
        return choices.filter { (k, v) -> k.isNotEmpty() && v.isNotEmpty() }.map { (k, v) ->
            Command.Choice(k, v)
        }
    }

    internal open fun addArgument(name: String, argument: ArgumentContainer<*, *>) {
        arguments[name] = argument
    }
}

@SlashDsl
class SlashCommand(name: String) : AbstractSlashCommand(name) {
    internal val subCommands = mutableMapOf<String, SubCommand>()

    @PublishedApi
    internal val groups = mutableMapOf<String, Group>()
    internal var commandPermissions = DefaultMemberPermissions.ENABLED
    var availableInDms = false


    fun run(action: suspend SlashContext.() -> Unit) {
        check(groups.isEmpty()) { "Cannot mix groups and non-grouped commands" }
        this.action = action
    }

    fun defualtPermissions(vararg permissions: Permission) {
        commandPermissions = DefaultMemberPermissions.enabledFor(*permissions)
    }

    fun disableByDefault() {
        commandPermissions = DefaultMemberPermissions.DISABLED
    }

    internal fun getSubCommand(group: String?, name: String) = when {
        group != null -> getGroup(group)?.getCommand(name)
        else -> subCommands[name]
    }

    internal fun getGroup(name: String) = groups[name]
}

@SlashDsl
class SubCommand(name: String) : AbstractSlashCommand(name) {
    fun run(action: suspend SlashContext.() -> Unit) {
        this.action = action
    }
}

@SlashDsl
class Group(name: String) : AbstractSlashCommand(name) {
    internal val commands = mutableMapOf<String, SubCommand>()

    internal fun getCommand(name: String) = commands[name]

    @PublishedApi
    internal fun setCommand(command: SubCommand) {
        commands[command.name] = command
    }

    override fun addArgument(name: String, argument: ArgumentContainer<*, *>) {
        throw UnsupportedOperationException("Groups cannot have arguments")
    }
}