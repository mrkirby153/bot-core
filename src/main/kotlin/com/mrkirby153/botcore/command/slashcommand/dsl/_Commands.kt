package com.mrkirby153.botcore.command.slashcommand.dsl

@DslMarker
annotation class SlashDsl

inline fun <T : Arguments> slashCommand(
    noinline arguments: () -> T,
    body: SlashCommand<T>.() -> Unit
): SlashCommand<T> {
    val command = SlashCommand(arguments)
    body(command)
    return command
}

inline fun slashCommand(body: SlashCommand<Arguments>.() -> Unit) = slashCommand(::Arguments, body)

inline fun <T : Arguments> SlashCommand<*>.subCommand(
    noinline arguments: () -> T,
    body: SubCommand<T>.() -> Unit
) {
    val command = SubCommand(arguments)
    body(command)
    this.subCommands[command.name] = command
}

inline fun SlashCommand<Arguments>.subCommand(body: SubCommand<Arguments>.() -> Unit) {
    return subCommand(::Arguments, body)
}

inline fun SlashCommand<Arguments>.slashCommand(body: SlashCommand<Arguments>.() -> Unit): SlashCommand<Arguments> {
    return slashCommand(::Arguments, body)
}

inline fun SlashCommand<*>.group(name: String, body: Group.() -> Unit) {
    val group = Group(name)
    body(group)
    if (this.groups[name] != null) {
        throw IllegalArgumentException("Duplicate group $name")
    }
    this.groups[name] = group
}

inline fun <T : Arguments> Group.slashCommand(
    noinline arguments: () -> T,
    body: SubCommand<T>.() -> Unit
) {
    val cmd = SubCommand(arguments)
    body(cmd)
    this.commands.add(cmd)
}

inline fun Group.slashCommand(body: SubCommand<Arguments>.() -> Unit) {
    slashCommand(::Arguments, body)
}

inline fun userContextCommand(body: UserContextCommand.() -> Unit): ContextCommand<UserContext> {
    val builder = UserContextCommand()
    body(builder)
    return builder
}

inline fun messageContextCommand(body: MessageContextCommand.() -> Unit): ContextCommand<MessageContext> {
    val builder = MessageContextCommand()
    body(builder)
    return builder
}