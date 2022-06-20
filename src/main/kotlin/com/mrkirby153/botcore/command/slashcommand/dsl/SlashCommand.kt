package com.mrkirby153.botcore.command.slashcommand.dsl

import com.mrkirby153.botcore.command.slashcommand.dsl.types.AutocompleteEligible
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.Command
import net.dv8tion.jda.api.interactions.commands.CommandPermissions


open class AbstractSlashCommand<A : Arguments>(
    private val arguments: (() -> A)?
) {
    var body: (SlashContext<A>.() -> Unit)? = null
    lateinit var name: String
    lateinit var description: String

    fun args() = arguments?.invoke()

    fun execute(event: SlashCommandInteractionEvent) {
        val ctx = SlashContext(this, event)
        ctx.load()
        body?.invoke(ctx)
    }

    fun handleAutocomplete(event: CommandAutoCompleteInteractionEvent): List<Command.Choice> {
        val argInst =
            args() ?: return listOf(Command.Choice("<<INVALID AUTOCOMPLETE SETTING>>", -1))
        val focused = event.focusedOption.name
        val inst = argInst.get(focused) ?: return emptyList()
        val builder = inst.builder
        if (builder is AutocompleteEligible) {
            return if (builder.autocompleteFunction != null) {
                builder.autocompleteFunction!!.invoke(event)
            } else {
                listOf(Command.Choice("<<NO AUTOCOMPLETE HANDLER>>", -1))
            }
        }
        return emptyList()
    }
}

@SlashDsl
class SlashCommand<A : Arguments>(
    arguments: (() -> A)? = null
) : AbstractSlashCommand<A>(arguments) {

    val subCommands = mutableMapOf<String, SubCommand<*>>()
    val groups = mutableMapOf<String, Group>()
    internal var commandPermissions = CommandPermissions.ENABLED

    fun action(action: SlashContext<A>.() -> Unit) {
        if (groups.isNotEmpty()) {
            throw IllegalArgumentException("Cannot mix groups and non-grouped commands")
        }
        this.body = action
    }

    fun getSubCommand(name: String) = subCommands[name]
    fun getSubCommand(group: String, name: String) = groups[group]?.getCommand(name)

    fun defaultPermissions(vararg permissions: Permission) {
        commandPermissions = CommandPermissions.enabledFor(*permissions)
    }

    fun disabledByDefault() {
        commandPermissions = CommandPermissions.DISABLED
    }

}

@SlashDsl
class Group(
    val name: String
) {
    val commands = mutableListOf<SubCommand<*>>()
    lateinit var description: String
    fun getCommand(name: String) = commands.firstOrNull { it.name == name }
}

@SlashDsl
class SubCommand<A : Arguments>(arguments: (() -> A)? = null) :
    AbstractSlashCommand<A>(arguments) {
    fun action(action: SlashContext<A>.() -> Unit) {
        this.body = action
    }
}