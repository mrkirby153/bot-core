package com.mrkirby153.botcore.command.slashcommand.dsl

import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.dv8tion.jda.api.interactions.commands.Command
import net.dv8tion.jda.api.interactions.commands.build.CommandData
import net.dv8tion.jda.api.interactions.commands.build.Commands
import net.dv8tion.jda.api.interactions.commands.build.OptionData
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData
import net.dv8tion.jda.api.interactions.commands.build.SubcommandGroupData
import java.util.concurrent.CompletableFuture

class DslSlashCommandExecutor : ListenerAdapter() {

    private val registeredCommands = mutableMapOf<String, SlashCommand<*>>()

    private fun getSlashCommand(event: SlashCommandInteractionEvent): AbstractSlashCommand<*>? {
        val command = registeredCommands[event.name] ?: return null
        val group = event.subcommandGroup
        val subCommandName = event.subcommandName
        return if (group != null) {
            command.getSubCommand(group, subCommandName!!)
        } else if (subCommandName != null) {
            command.getSubCommand(subCommandName)
        } else {
            command
        }
    }

    private fun getSlashCommand(event: CommandAutoCompleteInteractionEvent): AbstractSlashCommand<*>? {
        val command = registeredCommands[event.name] ?: return null
        val group = event.subcommandGroup
        val subCommandName = event.subcommandName
        return if (group != null) {
            command.getSubCommand(group, subCommandName!!)
        } else if (subCommandName != null) {
            command.getSubCommand(subCommandName)
        } else {
            command
        }
    }

    private fun populateArgs(data: SubcommandData, args: Arguments?) {
        if (args != null) {
            data.addOptions(
                args.get().map { arg ->
                    OptionData(
                        arg.type,
                        arg.displayName,
                        arg.description,
                        true, arg.autocompleteHandler != null
                    )
                })
            data.addOptions(
                args.getNullable().map { arg ->
                    OptionData(
                        arg.type,
                        arg.displayName,
                        arg.description,
                        false, arg.autocompleteHandler != null
                    )
                })
        }
    }

    private fun buildCommandData(): List<CommandData> = registeredCommands.map {
        val cmd = it.value
        val commandData = Commands.slash(cmd.name, cmd.description)
        if (cmd.subCommands.isNotEmpty()) {
            commandData.addSubcommands(cmd.subCommands.map { sub ->
                val subCmd = sub.value
                SubcommandData(subCmd.name, subCmd.description).apply {
                    populateArgs(this, subCmd.args())
                }
            })
        }
        if (cmd.groups.isNotEmpty()) {
            commandData.addSubcommandGroups(cmd.groups.map { group ->
                val grp = group.value
                SubcommandGroupData(grp.name, grp.description).addSubcommands(
                    grp.commands.map { sub ->
                        SubcommandData(
                            sub.name,
                            sub.description
                        ).apply { populateArgs(this, sub.args()) }
                    }
                )
            })
        }
        val args = cmd.args()
        if (args != null) {
            commandData.addOptions(
                args.get().map { arg ->
                    OptionData(
                        arg.type,
                        arg.displayName,
                        arg.description,
                        true, arg.autocompleteHandler != null
                    )
                })
            commandData.addOptions(
                args.getNullable().map { arg ->
                    OptionData(
                        arg.type,
                        arg.displayName,
                        arg.description,
                        false, arg.autocompleteHandler != null
                    )
                })
        }
        commandData
    }

    fun commit(jda: JDA): CompletableFuture<MutableList<Command>> {
        return jda.updateCommands().addCommands(buildCommandData()).submit()
    }

    fun commit(jda: JDA, vararg guilds: String): CompletableFuture<Void> {
        val commands = buildCommandData()
        val futures = guilds.mapNotNull { jda.getGuildById(it) }
            .map { it.updateCommands().addCommands(commands).submit() }
        return CompletableFuture.allOf(*futures.toTypedArray())
    }

    fun execute(event: SlashCommandInteractionEvent) {
        val cmd = getSlashCommand(event) ?: return
        cmd.execute(event)
    }

    fun handleAutocomplete(event: CommandAutoCompleteInteractionEvent) {
        val cmd = getSlashCommand(event) ?: return
        val options = cmd.handleAutocomplete(event)
        event.replyChoices(options).queue()
    }

    fun register(vararg commands: SlashCommand<*>) {
        commands.forEach { command ->
            registeredCommands[command.name] = command
        }
    }

    override fun onSlashCommandInteraction(event: SlashCommandInteractionEvent) {
        if (getSlashCommand(event) != null)
            execute(event)
    }

    override fun onCommandAutoCompleteInteraction(event: CommandAutoCompleteInteractionEvent) {
        if (getSlashCommand(event) != null)
            handleAutocomplete(event)
    }
}