package com.mrkirby153.botcore.command.slashcommand.dsl

@DslMarker
annotation class SlashDsl

/**
 * Declares a new slash command with the provided [arguments]
 *
 * Example Invocation
 * ```kotlin
 * slashCommand {
 *   name = "testing"
 *   description = "A test command"
 *   action {
 *      reply("Hello, World!").queue()
 *   }
 * }
 * ```
 *
 * Sub-commands can be declared with [subCommand], and groups can be declared using [group]
 *
 * @param arguments A function generating a new [Arguments] class that determines the arguments
 * for the slash command
 */
inline fun <T : Arguments> slashCommand(
    noinline arguments: () -> T,
    body: SlashCommand<T>.() -> Unit
) = SlashCommand(arguments).apply(body)

/**
 * Declares a slash command with no arguments.
 *
 * @see [slashCommand]
 */
@JvmName("slashCommandNoArguments")
inline fun slashCommand(body: SlashCommand<Arguments>.() -> Unit) = slashCommand(::Arguments, body)

/**
 * Declares a slash command with the provided arguments.
 *
 * @see [slashCommand]
 */
inline fun <T : Arguments> DslCommandExecutor.slashCommand(
    noinline arguments: () -> T,
    body: SlashCommand<T>.() -> Unit
) = SlashCommand(arguments).apply(body).also { this.register(it) }

/**
 * Declares a slash command with no arguments.
 *
 * @see [slashCommand]
 */
inline fun DslCommandExecutor.slashCommand(body: SlashCommand<Arguments>.() -> Unit) =
    this.register(slashCommand(::Arguments, body))

/**
 * Declares a message context command
 */
inline fun DslCommandExecutor.messageContextCommand(body: MessageContextCommand.() -> Unit) =
    MessageContextCommand().apply(body).also { this.register(it) }

/**
 * Declares a user context command
 */
inline fun DslCommandExecutor.userContextCommand(body: UserContextCommand.() -> Unit) =
    UserContextCommand().apply(body).also { this.register(it) }


/**
 * Declares a sub-command with the provided [arguments]
 */
inline fun <T : Arguments> SlashCommand<*>.subCommand(
    noinline arguments: () -> T,
    body: SubCommand<T>.() -> Unit
) = SubCommand(arguments).apply(body).also {
    check(this.subCommands[it.name] == null) { "Registering a duplicate sub-command $name" }
    this.subCommands[it.name] = it
}

/**
 * Declares a sub-command with no arguments
 */
@JvmName("subCommandDefaultArguments")
inline fun SlashCommand<*>.subCommand(body: SubCommand<Arguments>.() -> Unit) =
    subCommand(::Arguments, body)

/**
 * Declares a group with the provided [name]
 */
inline fun SlashCommand<*>.group(name: String, body: Group.() -> Unit) =
    Group(name).apply(body).also {
        check(this.groups[it.name] != null) { "Duplicate group $name" }
        this.groups[it.name] = it
    }

/**
 * Declares a sub command with the given [arguments]
 */
inline fun <T : Arguments> Group.slashCommand(
    noinline arguments: () -> T,
    body: SubCommand<T>.() -> Unit
) = SubCommand(arguments).apply(body).also { this.commands.add(it) }

/**
 * Declares a sub-command with no arguments
 */
inline fun Group.slashCommand(body: SubCommand<Arguments>.() -> Unit) =
    slashCommand(::Arguments, body)

/**
 * Declares a user context command
 *
 * User context commands show up under the `Apps` section when right-clicking on a user in the client
 * ```kotlin
 * messageContextCommand {
 *  name = "Lookup message"
 *  action {
 *      reply("Looking up message $message").queue()
 *  }
 * }
 * ```
 */
inline fun userContextCommand(body: UserContextCommand.() -> Unit) =
    UserContextCommand().apply(body)

/**
 * Declares a message context command
 *
 * Message context commands show up under the `Apps` section when right-clicking on a message in
 * the client
 *
 * Example invocation
 * ```kotlin
 * messageContextCommand {
 *  name = "Lookup user"
 *  action {
 *      reply("Looking up user $user").queue()
 *  }
 * }
 * ```
 */
inline fun messageContextCommand(body: MessageContextCommand.() -> Unit) =
    MessageContextCommand().apply(body)