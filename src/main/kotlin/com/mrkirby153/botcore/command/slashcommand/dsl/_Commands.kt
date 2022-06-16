package com.mrkirby153.botcore.command.slashcommand.dsl

@DslMarker
annotation class SlashDsl

fun <T : Arguments> slashCommand(arguments: () -> T, body: SlashCommand<T>.() -> Unit): SlashCommand<T> {
    val command = SlashCommand(arguments)
    body(command)
    return command
}

fun slashCommand(body: SlashCommand<Arguments>.() -> Unit): SlashCommand<Arguments> {
    return slashCommand(::Arguments, body)
}