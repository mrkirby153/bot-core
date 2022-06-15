package com.mrkirby153.botcore.command.slashcommand.dsl.types

import com.mrkirby153.botcore.command.slashcommand.dsl.Argument
import com.mrkirby153.botcore.command.slashcommand.dsl.ArgumentConverter
import com.mrkirby153.botcore.command.slashcommand.dsl.Arguments


class StringConverter : ArgumentConverter<String> {
    override fun convert(input: String): String = input
}

class StringArgument : ArgBuilder<String>() {

    override fun build(arguments: Arguments): Argument<String> =
        Argument(required, displayName, description, StringConverter())
}


fun Arguments.string(body: ArgBuilder<String>.() -> Unit): Argument<String> {
    val builder = StringArgument()
    body(builder)
    val built = builder.build(this)
    this.addArgument(built)
    return built
}