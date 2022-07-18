package com.mrkirby153.botcore.command.slashcommand.dsl

import com.mrkirby153.botcore.command.CommandException
import com.mrkirby153.botcore.command.args.BatchArgumentParseException
import com.mrkirby153.botcore.command.slashcommand.dsl.types.AutocompleteEligible
import com.mrkirby153.botcore.command.slashcommand.dsl.types.IArgBuilder
import com.mrkirby153.botcore.command.slashcommand.dsl.types.ModifiesOption
import com.mrkirby153.botcore.i18n.TranslationProvider
import com.mrkirby153.botcore.i18n.TranslationProviderLocalizationFunction
import com.mrkirby153.botcore.log
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent
import net.dv8tion.jda.api.events.interaction.command.MessageContextInteractionEvent
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.events.interaction.command.UserContextInteractionEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.dv8tion.jda.api.interactions.commands.Command
import net.dv8tion.jda.api.interactions.commands.build.CommandData
import net.dv8tion.jda.api.interactions.commands.build.Commands
import net.dv8tion.jda.api.interactions.commands.build.OptionData
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData
import net.dv8tion.jda.api.interactions.commands.build.SubcommandGroupData
import net.dv8tion.jda.api.interactions.commands.context.ContextInteraction
import net.dv8tion.jda.api.interactions.commands.localization.LocalizationFunction
import java.util.concurrent.CompletableFuture

/**
 * Command executor for Kotlin based DSL slash commands.
 *
 * Use [slashCommand] to create slash commands, [messageContextCommand] to create message context
 * commands, and [userContextCommands] to create user context commands.
 *
 * Slash, message, and user context commands that are declared in [registerCommands] are
 * automatically added to the executor.
 *
 * **Note:** The executor must be registered as an event listener with JDA
 *
 * @param translationBundle An optional translation bundle to retrieve command localizations from
 * @param translationProvider An optional translation provider to retrieve command localizations from
 */
class DslCommandExecutor(
    translationBundle: String? = null,
    translationProvider: TranslationProvider? = null
) : ListenerAdapter() {

    private val registeredCommands = mutableMapOf<String, SlashCommand<out Arguments>>()
    private val userContextCommands = mutableListOf<UserContextCommand>()
    private val messageContextCommands = mutableListOf<MessageContextCommand>()

    private val localizationFunction: LocalizationFunction? =
        if (translationProvider != null && translationBundle != null) TranslationProviderLocalizationFunction(
            translationBundle,
            translationProvider
        ) else null

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

    private fun createOption(arg: IArgument<*, out IArgBuilder<*>>) = OptionData(
        arg.type,
        arg.displayName,
        arg.description,
        arg !is NullableArgument,
        arg.builder is AutocompleteEligible && (arg.builder as AutocompleteEligible).autocompleteFunction != null
    ).apply {
        if (arg.builder is ModifiesOption) {
            (arg.builder as ModifiesOption).modify(this)
        }
    }

    private fun populateArgs(data: SubcommandData, args: Arguments?) {
        if (args != null) {
            data.addOptions(
                args.get().map { arg ->
                    createOption(arg)
                })
        }
    }

    private fun buildCommandData(): List<CommandData> {
        val commands: MutableList<CommandData> = registeredCommands.map {
            val cmd = it.value
            val commandData = Commands.slash(cmd.name, cmd.description).apply {
                if (localizationFunction != null) {
                    setLocalizationFunction(localizationFunction)
                }
            }
            commandData.defaultPermissions = cmd.commandPermissions
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
                    args.get().sortedBy { a -> if (a is NullableArgument) -1 else 1 }.map { arg ->
                        createOption(arg)
                    })
            }
            commandData
        }.toMutableList()
        val registeredContextCommands =
            listOf(*userContextCommands.toTypedArray(), *messageContextCommands.toTypedArray())
        commands.addAll(registeredContextCommands.mapNotNull { cmd ->
            val c = when (cmd) {
                is UserContextCommand -> Commands.user(cmd.name)
                is MessageContextCommand -> Commands.message(cmd.name)
                else -> null
            }.apply {
                if (this != null && localizationFunction != null) {
                    setLocalizationFunction(localizationFunction)
                }
            }
            c
        })
        return commands
    }

    /**
     * Commits all the slash commands currently registered to Discord.
     *
     * @param jda The JDA instance to use to commit
     * @return A [CompletableFuture] completed with the [Commands][Command] that wer committed to Discord
     */
    fun commit(jda: JDA): CompletableFuture<MutableList<Command>> {
        val commands = buildCommandData()
        log.debug("Committing {} commands globally", commands.size)
        return jda.updateCommands().addCommands(commands).submit()
    }

    /**
     * Commits all the slash commands currently registered to the provided [guilds].
     *
     * @param jda The JDA instance to use during commit
     * @return A [CompletableFuture] completed when the commands have been committed to all guilds
     */
    fun commit(jda: JDA, vararg guilds: String): CompletableFuture<Void> {
        val commands = buildCommandData()
        log.debug("Committing {} commands to the following guilds: {}", commands.size, guilds)
        val futures = guilds.mapNotNull { jda.getGuildById(it) }
            .map { it.updateCommands().addCommands(commands).submit() }
        return CompletableFuture.allOf(*futures.toTypedArray())
    }

    /**
     * Executes a slash command from the provided [event]
     */
    fun execute(event: SlashCommandInteractionEvent) {
        val cmd = getSlashCommand(event) ?: return
        log.trace("Executing slash command ${event.commandPath}")
        try {
            cmd.execute(event)
        } catch (e: CommandException) {
            event.reply(":no_entry: ${e.message ?: "An unknown error occurred!"}")
                .setEphemeral(true).queue()
        } catch (e: BatchArgumentParseException) {
            event.reply(buildString {
                if (e.exceptions.size > 1) {
                    appendLine(":no_entry: Multiple errors occurred:")
                    e.exceptions.forEach { (fieldName, exception) ->
                        appendLine(" `$fieldName`: ${exception.message ?: "An unknown error occurred!"}")
                    }
                } else {
                    val (fieldName, ex) = e.exceptions.entries.first()
                    appendLine(":no_entry: `$fieldName`: ${ex.message ?: "An unknown error occurred!"}")
                }
            }).setEphemeral(true).queue()
        }
    }

    /**
     * Handle an autocompletion [event] and return the results to the user
     */
    fun handleAutocomplete(event: CommandAutoCompleteInteractionEvent) {
        val cmd = getSlashCommand(event) ?: return
        log.trace("Handling autocomplete for {}", event.commandPath)
        val options = cmd.handleAutocomplete(event)
        log.trace("Suggested options: [{}]", options.joinToString(",") { it.name })
        event.replyChoices(options).queue()
    }

    /**
     * Register the provided [commands] with this executor
     *
     * @see SlashCommand
     */
    fun register(vararg commands: SlashCommand<out Arguments>) {
        commands.forEach { command ->
            log.trace("Registering command {}", command.name)
            registeredCommands[command.name] = command
        }
    }

    /**
     * Register the provided commands with this executor
     *
     * @param commands A list of context commands to register
     * @see UserContextCommand
     * @see MessageContextCommand
     */
    fun register(vararg commands: ContextCommand<out ContextInteraction<*>>) {
        commands.forEach {
            log.trace("Registering context command {}", it.name)
            when (it) {
                is MessageContextCommand -> messageContextCommands.add(it)
                is UserContextCommand -> userContextCommands.add(it)
            }
        }
    }

    /**
     * Convenience function for registering commands. All commands that are declared in this
     * block are automatically registered with this executor
     */
    fun registerCommands(body: DslCommandExecutor.() -> Unit) {
        body(this)
    }

    override fun onSlashCommandInteraction(event: SlashCommandInteractionEvent) {
        if (getSlashCommand(event) != null)
            execute(event)
    }

    override fun onCommandAutoCompleteInteraction(event: CommandAutoCompleteInteractionEvent) {
        if (getSlashCommand(event) != null)
            handleAutocomplete(event)
    }

    override fun onUserContextInteraction(event: UserContextInteractionEvent) {
        userContextCommands.firstOrNull { it.name == event.name }?.execute(UserContext(event))
    }

    override fun onMessageContextInteraction(event: MessageContextInteractionEvent) {
        messageContextCommands.firstOrNull { it.name == event.name }?.execute(MessageContext(event))
    }
}