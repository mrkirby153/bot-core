package com.mrkirby153.botcore.command.slashcommand.dsl

@DslMarker
annotation class SlashDsl

fun <T : Arguments> slashCommand(
    arguments: () -> T,
    body: SlashCommand<T>.() -> Unit
): SlashCommand<T> {
    val command = SlashCommand(arguments)
    body(command)
    return command
}

fun slashCommand(body: SlashCommand<Arguments>.() -> Unit): SlashCommand<Arguments> {
    return slashCommand(::Arguments, body)
}

fun SlashCommand<*>.group(name: String, body: Group.() -> Unit) {
    val group = Group()
    body(group)
    if (this.groups[name] != null) {
        throw IllegalArgumentException("Duplicate group $name")
    }
    this.groups[name] = group.commands
}

fun <T : Arguments> Group.slashCommand(arguments: () -> T, body: SubCommand<T>.() -> Unit) {
    val cmd = SubCommand(arguments)
    body(cmd)
    this.commands.add(cmd)
}

fun Group.slashCommand(body: SubCommand<Arguments>.() -> Unit) {
    slashCommand(::Arguments, body)
}