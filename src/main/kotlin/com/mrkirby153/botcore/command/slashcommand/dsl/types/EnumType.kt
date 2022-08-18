package com.mrkirby153.botcore.command.slashcommand.dsl.types

import com.mrkirby153.botcore.command.args.ArgumentParseException
import com.mrkirby153.botcore.command.slashcommand.dsl.ArgumentConverter
import com.mrkirby153.botcore.command.slashcommand.dsl.Arguments
import net.dv8tion.jda.api.interactions.commands.Command
import net.dv8tion.jda.api.interactions.commands.OptionMapping
import net.dv8tion.jda.api.interactions.commands.OptionType

class EnumConverter<T : Enum<T>>(
    private val getter: (OptionMapping) -> T?,
    private val validEnums: Array<T>
) : ArgumentConverter<T> {
    override fun convert(input: OptionMapping): T {
        return getter(input) ?: throw ArgumentParseException("${input.asString} is not one of ${
            validEnums.joinToString(
                ", "
            ) { it.toString() }
        }")
    }

    override val type = OptionType.STRING
}

class EnumArgumentBuilder<T : Enum<T>>(
    inst: Arguments,
    getter: (OptionMapping) -> T?,
    private val validEnums: Array<T>
) : ArgumentBuilder<T>(inst, EnumConverter(getter, validEnums)) {

    override fun createOption() = super.createOption().apply {
        addChoices(validEnums.map { Command.Choice(it.toString(), it.name) })
    }
}

inline fun <reified T : Enum<T>> Arguments.enum(body: EnumArgumentBuilder<T>.() -> Unit): EnumArgumentBuilder<T> {
    val getter: (OptionMapping) -> T? = { map ->
        enumValues<T>().firstOrNull { it.name.equals(map.asString, true) }
    }
    return EnumArgumentBuilder(this, getter, enumValues()).apply(body)
}