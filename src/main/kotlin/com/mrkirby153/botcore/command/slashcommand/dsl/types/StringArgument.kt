package com.mrkirby153.botcore.command.slashcommand.dsl.types

import com.mrkirby153.botcore.command.slashcommand.dsl.Argument
import com.mrkirby153.botcore.command.slashcommand.dsl.ArgumentConverter
import com.mrkirby153.botcore.command.slashcommand.dsl.Arguments
import com.mrkirby153.botcore.command.slashcommand.dsl.NullableArgument


class StringConverter : ArgumentConverter<String> {
    override fun convert(input: String): String = input
}

class StringArgument : ArgBuilder<String>() {

    override fun build(arguments: Arguments): Argument<String> =
        Argument(displayName, description, StringConverter())
}

class OptionalStringArgument : NullableArgBuilder<String>() {
    override fun build(arguments: Arguments): NullableArgument<String> =
        NullableArgument(displayName, description, StringConverter())
}


fun Arguments.string(body: ArgBuilder<String>.() -> Unit): Argument<String> {
    val builder = StringArgument()
    body(builder)
    val built = builder.build(this)
    this.addArgument(built)
    return built
}

fun Arguments.optionalString(body: NullableArgBuilder<String>.() -> Unit): NullableArgument<String> {
    val builder = OptionalStringArgument()
    body(builder)
    val built = builder.build(this)
    this.addNullable(built)
    return built
}