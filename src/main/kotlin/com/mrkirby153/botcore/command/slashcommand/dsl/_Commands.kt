package com.mrkirby153.botcore.command.slashcommand.dsl

@DslMarker
annotation class SlashDsl

fun slashCommand(name: String, body: SlashCommand.() -> Unit) = SlashCommand(name).apply(body)

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