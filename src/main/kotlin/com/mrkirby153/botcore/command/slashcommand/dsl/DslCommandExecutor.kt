package com.mrkirby153.botcore.command.slashcommand.dsl

import com.mrkirby153.botcore.coroutine.CoroutineEventListener
import com.mrkirby153.botcore.coroutine.await
import com.mrkirby153.botcore.i18n.TranslationProvider
import com.mrkirby153.botcore.i18n.TranslationProviderLocalizationFunction
import com.mrkirby153.botcore.utils.SLF4J
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.events.GenericEvent
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent
import net.dv8tion.jda.api.events.interaction.command.MessageContextInteractionEvent
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.events.interaction.command.UserContextInteractionEvent
import net.dv8tion.jda.api.interactions.commands.Command
import net.dv8tion.jda.api.interactions.commands.build.CommandData
import net.dv8tion.jda.api.interactions.commands.build.Commands
import net.dv8tion.jda.api.interactions.commands.build.OptionData
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData
import net.dv8tion.jda.api.interactions.commands.build.SubcommandGroupData
import net.dv8tion.jda.api.interactions.commands.context.ContextInteraction
import net.dv8tion.jda.api.interactions.commands.localization.LocalizationFunction
import net.dv8tion.jda.api.sharding.ShardManager
import org.slf4j.Logger
import java.util.concurrent.CompletableFuture
import kotlin.coroutines.EmptyCoroutineContext

/**
 * Command executor for Kotlin based DSL slash commands.
 *
 * Use [subCommand] to create slash commands, [messageContextCommand] to create message context
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
class DslCommandExecutor private constructor(
    translationBundle: String?,
    translationProvider: TranslationProvider?
) {
    constructor() : this(null, null)

    private val log: Logger by SLF4J

    private val registeredCommands = mutableMapOf<String, SlashCommand>()
    private val userContextCommands = mutableListOf<UserContextCommand>()
    private val messageContextCommands = mutableListOf<MessageContextCommand>()

    private val localizationFunction: LocalizationFunction? =
        if (translationProvider != null && translationBundle != null) TranslationProviderLocalizationFunction(
            translationBundle,
            translationProvider
        ) else null


    private fun getSlashCommand(
        name: String,
        group: String?,
        subCommand: String?
    ): AbstractSlashCommand? {
        val command = registeredCommands[name] ?: return null
        return when {
            group != null -> {
                command.getGroup(group)
            }

            subCommand != null -> {
                command.getSubCommand(group, subCommand)
            }

            else ->
                command
        }
    }

    private fun getSlashCommand(event: SlashCommandInteractionEvent) =
        getSlashCommand(event.name, event.subcommandGroup, event.subcommandName)

    private fun getSlashCommand(event: CommandAutoCompleteInteractionEvent) =
        getSlashCommand(event.name, event.subcommandGroup, event.subcommandName)

    private fun createOption(name: String, container: ArgumentContainer<*, *>) =
        OptionData(container.builder.type, name, container.builder.description).apply {
            container.builder.augmentOption(this)
            isRequired = container.required
        }

    private fun populateArgs(data: SubcommandData, subCommand: SubCommand) {
        data.addOptions(subCommand.arguments.map { (name, container) ->
            createOption(name, container)
        })
    }

    private fun buildCommandData(): List<CommandData> {
        val commands: MutableList<CommandData> = registeredCommands.map { (_, cmd) ->
            val commandData = Commands.slash(cmd.name, cmd.description).apply {
                isGuildOnly = !cmd.availableInDms
                if (localizationFunction != null) {
                    setLocalizationFunction(localizationFunction)
                }
            }
            commandData.defaultPermissions = cmd.commandPermissions
            if (cmd.subCommands.isNotEmpty()) {
                commandData.addSubcommands(cmd.subCommands.map { (_, sub) ->
                    SubcommandData(sub.name, sub.description).apply {
                        populateArgs(this, sub)
                    }
                })
            }
            if (cmd.groups.isNotEmpty()) {
                commandData.addSubcommandGroups(cmd.groups.map { (_, group) ->
                    SubcommandGroupData(group.name, group.description).addSubcommands(
                        group.commands.map { (_, subCommand) ->
                            SubcommandData(subCommand.name, subCommand.description).apply {
                                populateArgs(this, subCommand)
                            }
                        }
                    )
                })
            }
            if (cmd.arguments.isNotEmpty()) {
                commandData.addOptions(cmd.arguments.entries.sortedBy { !it.value.required }
                    .map { (name, container) ->
                        createOption(name, container)
                    })
            }
            commandData
        }.toMutableList()
        val registeredContextCommands = mutableListOf<ContextCommand<*>>()
        registeredContextCommands.addAll(userContextCommands)
        registeredContextCommands.addAll(messageContextCommands)
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
    fun commit(jda: JDA, vararg guilds: String) = doCommit({ jda.getGuildById(it) }, guilds)

    fun commit(shardManager: ShardManager, vararg guilds: String) =
        doCommit({ shardManager.getGuildById(it) }, guilds)

    private fun doCommit(
        getGuilds: (String) -> Guild?,
        guilds: Array<out String>
    ): CompletableFuture<Void> {
        val commands = buildCommandData()
        log.debug("Committing {} commands to the following guilds: {}", commands.size, guilds)
        val futures = guilds.mapNotNull { getGuilds(it) }
            .map { it.updateCommands().addCommands(commands).submit() }
        return CompletableFuture.allOf(*futures.toTypedArray())
    }


    /**
     * Gets a list of [CommandData] for all commands registered with this executor
     */
    fun getCommandData() = buildCommandData()

    /**
     * Executes a slash command from the provided [event]
     */
    suspend fun execute(event: SlashCommandInteractionEvent, scope: CoroutineScope) {
        suspend fun replyOrEdit(
            message: String,
            ephemeral: Boolean = true
        ) =
            if (event.isAcknowledged) {
                event.hook.editOriginal(message).setEmbeds().setComponents()
            } else {
                event.reply(message).setEphemeral(ephemeral).setEmbeds().setComponents()
            }.await()

        val cmd = getSlashCommand(event) ?: return
        log.trace("Executing slash command ${event.fullCommandName}")
        try {
            cmd.execute(event, scope)
        } catch (e: CommandException) {
            replyOrEdit(":no_entry: ${e.message ?: "An unknown error occurred"}")
        } catch (e: BatchArgumentParseException) {
            val msg = buildString {
                if (e.exceptions.size > 1) {
                    appendLine(":no_entry: Multiple errors occurred:")
                    e.exceptions.forEach { (fieldName, exception) ->
                        appendLine(" `$fieldName`: ${exception.message ?: "An unknown error occurred!"}")
                    }
                } else {
                    val (fieldName, ex) = e.exceptions.entries.first()
                    appendLine(":no_entry: `$fieldName`: ${ex.message ?: "An unknown error occurred!"}")
                }
            }
            replyOrEdit(msg)
        } catch (e: Exception) {
            log.error("Error executing slash command ${event.fullCommandName}", e)
            replyOrEdit(":no_entry: Something went wrong when processing this command")
        }
    }

    /**
     * Handle an autocompletion [event] and return the results to the user
     */
    fun handleAutocomplete(event: CommandAutoCompleteInteractionEvent) {
        val cmd = getSlashCommand(event) ?: return
        log.trace("Handling autocomplete for {}", event.fullCommandName)
        val options = cmd.handleAutocomplete(event)
        log.trace("Suggested options: [{}]", options.joinToString(",") { it.name })
        event.replyChoices(options).queue()
    }

    /**
     * Register the provided [commands] with this executor
     *
     * @see SlashCommand
     */
    fun register(vararg commands: SlashCommand) {
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


    inner class Listener(dispatcher: CoroutineDispatcher) :
        CoroutineEventListener {

        private val scope =
            CoroutineScope(dispatcher + SupervisorJob() + EmptyCoroutineContext)

        override suspend fun onEvent(event: GenericEvent) {
            when (event) {
                is SlashCommandInteractionEvent -> {
                    val slashCommand = getSlashCommand(event)
                    if (slashCommand != null) {
                        scope.launch {
                            log.trace("Running command ${slashCommand.name} with a timeout of ${slashCommand.timeout}")
                            if (slashCommand.timeout != 0L)
                                withTimeout(slashCommand.timeout) {
                                    execute(event, this)
                                }
                            else
                                execute(event, this)
                        }
                    }
                }

                is CommandAutoCompleteInteractionEvent -> {
                    val slashCommand = getSlashCommand(event)
                    if (slashCommand != null) {
                        scope.launch {
                            log.trace("Running autocomplete handler ${slashCommand.name} with a timeout of ${slashCommand.timeout}")
                            withTimeout(slashCommand.autocompleteTimeout) {
                                handleAutocomplete(event)
                            }
                        }
                    }
                }

                is UserContextInteractionEvent -> {
                    scope.launch {
                        userContextCommands.firstOrNull { it.name == event.name }
                            ?.apply {
                                if (timeout != 0L)
                                    withTimeout(timeout) {
                                        execute(UserContext(event, scope))
                                    }
                                else
                                    execute(UserContext(event, scope))
                            }
                    }
                }

                is MessageContextInteractionEvent -> {
                    scope.launch {
                        messageContextCommands.firstOrNull { it.name == event.name }
                            ?.apply {
                                if (timeout != 0L)
                                    withTimeout(timeout) {
                                        execute(MessageContext(event, scope))
                                    }
                                else
                                    execute(MessageContext(event, scope))
                            }
                    }
                }
            }
        }
    }

    fun getListener(dispatcher: CoroutineDispatcher = Dispatchers.Default): Listener {
        return Listener(dispatcher)
    }

    companion object {

        fun translatable(bundle: String, provider: TranslationProvider): DslCommandExecutor {
            return DslCommandExecutor(bundle, provider)
        }
    }
}