package com.mrkirby153.botcore.command.slashcommand.dsl.types

import com.mrkirby153.botcore.command.args.ArgumentParseException
import com.mrkirby153.botcore.command.slashcommand.dsl.Argument
import com.mrkirby153.botcore.command.slashcommand.dsl.ArgumentConverter
import com.mrkirby153.botcore.command.slashcommand.dsl.Arguments
import com.mrkirby153.botcore.command.slashcommand.dsl.NullableArgument
import net.dv8tion.jda.api.interactions.commands.Command
import net.dv8tion.jda.api.interactions.commands.OptionMapping
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.OptionData

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
}

interface IEnumArgument<T : Enum<T>> : ModifiesOption {
    val getter: (OptionMapping) -> T?
    val validEnums: Array<T>

    override fun modify(option: OptionData) {
        option.addChoices(validEnums.map { Command.Choice(it.toString(), it.name) })
    }
}

private fun <T : Enum<T>> IEnumArgument<T>.getAutocompleteFunc(): AutoCompleteCallback {
    return { ac ->
        val value = ac.focusedOption.value.lowercase().replace("_", " ")
        validEnums.filter {
            val enumName = it.name.lowercase().replace("_", " ")
            enumName.startsWith(value)
        }.map {
            Command.Choice(it.toString(), it.name)
        }.take(OptionData.MAX_CHOICES).toList()
    }
}

class EnumArgument<T : Enum<T>>(
    override val getter: (OptionMapping) -> T?,
    override val validEnums: Array<T>
) : GenericArgument<T>(OptionType.STRING, { EnumConverter(getter, validEnums) }), IEnumArgument<T> {
    override var autocompleteFunction: AutoCompleteCallback?
        get() = getAutocompleteFunc()
        set(_) {
            throw IllegalArgumentException("Cannot set autocomplete functions for enum types")
        }
}

class OptionalEnumArgument<T : Enum<T>>(
    override val getter: (OptionMapping) -> T?,
    override val validEnums: Array<T>
) : GenericNullableArgument<T>(OptionType.STRING, { EnumConverter(getter, validEnums) }),
    IEnumArgument<T> {
    override var autocompleteFunction: AutoCompleteCallback?
        get() = getAutocompleteFunc()
        set(_) {
            throw IllegalArgumentException("Cannot set autocomplete functions for enum types")
        }
}

inline fun <reified T : Enum<T>> Arguments.enum(
    noinline body: EnumArgument<T>.() -> Unit
): Argument<T> {
    val getter: (OptionMapping) -> T? = { map ->
        enumValues<T>().firstOrNull { it.name.equals(map.asString, true) }
    }
    return genericArgument({
        EnumArgument(getter, enumValues())
    }, body)
}

inline fun <reified T : Enum<T>> Arguments.optionalEnum(noinline body: OptionalEnumArgument<T>.() -> Unit): NullableArgument<T> {
    val getter: (OptionMapping) -> T? = { map ->
        enumValues<T>().firstOrNull { it.name.equals(map.asString, true) }
    }
    return optionalGenericArgument({ OptionalEnumArgument(getter, enumValues()) }, body)
}