package com.mrkirby153.botcore.command.slashcommand.dsl

@DslMarker
annotation class SlashDsl

/**
 * Declares a new slash command with the given [name].
 *
 * Example Invocation
 * ```
 * slashCommand("testing") {
 *     description = "this is a test"
 *     run {
 *         reply("Hello, World").await()
 *     }
 * }
 * ```
 *
 * Sub-commands can be declared with [subCommand], and groups can be declared using [group]
 */
fun slashCommand(name: String, body: SlashCommand.() -> Unit) = SlashCommand(name).apply(body)

/**
 * Convenience function for declaring [slashCommand]. This command will automatically be registered
 */
fun DslCommandExecutor.slashCommand(name: String, body: SlashCommand.() -> Unit) =
    SlashCommand(name).apply(body).also { this.register(it) }


/**
 * Declares a sub-command with the given [name]
 */
fun SlashCommand.subCommand(name: String, body: SubCommand.() -> Unit) =
    SubCommand(name).apply(body).also {
        check(this.subCommands[name] == null) { "Duplicate sub-command $name" }
        this.subCommands[name] = it
    }

/**
 * Declares a group with the provided [name]
 */
inline fun SlashCommand.group(name: String, body: Group.() -> Unit) = Group(name).apply(body).also {
    check(this.groups[name] == null) { "Duplicate group $name" }
    this.groups[name] = it
}

/**
 * Declares a sub-command with the given [name]
 */
inline fun Group.subCommand(name: String, body: SubCommand.() -> Unit) =
    SubCommand(name).apply(body).also {
        this.setCommand(it)
    }

/**
 * Declares a user context command
 *
 * User context commands show up under the `Apps` section when right-clicking on a user in the client
 * ```kotlin
 * messageContextCommand("Lookup message") {
 *  action {
 *      reply("Looking up message $message").queue()
 *  }
 * }
 * ```
 */
inline fun userContextCommand(name: String, body: UserContextCommand.() -> Unit) =
    UserContextCommand(name).apply(body)

/**
 * Declares a message context command
 *
 * Message context commands show up under the `Apps` section when right-clicking on a message in
 * the client
 *
 * Example invocation
 * ```kotlin
 * messageContextCommand("Lookup user") {
 *  action {
 *      reply("Looking up user $user").queue()
 *  }
 * }
 * ```
 */
inline fun messageContextCommand(name: String, body: MessageContextCommand.() -> Unit) =
    MessageContextCommand(name).apply(body)


/**
 * Declares a user context command and registers it with the current executor
 */
fun DslCommandExecutor.userContextCommand(name: String, body: UserContextCommand.() -> Unit) =
    UserContextCommand(name).apply(body).also {
        this.register(it)
    }

/**
 * Declares a message context command and registers it with the current executor
 */
fun DslCommandExecutor.messageContextCommand(name: String, body: MessageContextCommand.() -> Unit) =
    MessageContextCommand(name).apply(body).also {
        this.register(it)
    }