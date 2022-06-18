package com.mrkirby153.botcore.command.slashcommand.dsl.types

import com.mrkirby153.botcore.command.slashcommand.dsl.Argument
import com.mrkirby153.botcore.command.slashcommand.dsl.ArgumentConverter
import com.mrkirby153.botcore.command.slashcommand.dsl.Arguments
import com.mrkirby153.botcore.command.slashcommand.dsl.NullableArgument
import net.dv8tion.jda.api.interactions.commands.OptionMapping
import net.dv8tion.jda.api.interactions.commands.OptionType


class StringConverter : ArgumentConverter<String> {
    override fun convert(input: OptionMapping): String = input.asString
}

class StringArgument : ArgBuilder<String>(OptionType.STRING), AutocompleteEligible {

    override var autocompleteFunction: AutoCompleteCallback? = null
    override fun build(arguments: Arguments): Argument<String> =
        Argument(type, displayName, description, StringConverter(), this)

}

class OptionalStringArgument : NullableArgBuilder<String>(OptionType.STRING),
    AutocompleteEligible {
    override var autocompleteFunction: AutoCompleteCallback? = null
    override fun build(arguments: Arguments): NullableArgument<String> =
        NullableArgument(type, displayName, description, StringConverter(), this)
}


fun Arguments.string(body: StringArgument.() -> Unit): Argument<String> {
    val builder = StringArgument()
    body(builder)
    val built = builder.build(this)
    this.addArgument(built)
    return built
}

fun Arguments.optionalString(body: OptionalStringArgument.() -> Unit): NullableArgument<String> {
    val builder = OptionalStringArgument()
    body(builder)
    val built = builder.build(this)
    this.addNullable(built)
    return built
}