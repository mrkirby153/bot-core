package com.mrkirby153.botcore.command.slashcommand.dsl.types

import com.mrkirby153.botcore.command.slashcommand.dsl.AbstractSlashCommand
import com.mrkirby153.botcore.command.slashcommand.dsl.ArgumentConverter
import com.mrkirby153.botcore.command.slashcommand.dsl.ArgumentParseException
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent
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

    override val type = OptionType.STRING
}

class EnumArgumentBuilder<T : Enum<T>>(
    inst: AbstractSlashCommand,
    getter: (OptionMapping) -> T?,
    private val validEnums: Array<T>
) : ArgumentBuilder<T>(inst, EnumConverter(getter, validEnums)) {

    init {
        if (validEnums.size > 25) {
            autocomplete { event ->
                validEnums.filter {
                    it.name.lowercase().startsWith(event.focusedOption.value.lowercase())
                }.map {
                    Pair(it.name, it.name)
                }.take(25)
            }
        }
    }

    override fun augmentOption(option: OptionData) {
        super.augmentOption(option)
        if (validEnums.size < 25)
            option.addChoices(validEnums.map { Command.Choice(it.toString(), it.name) })
        else
            option.isAutoComplete = true
    }
}

inline fun <reified T : Enum<T>> AbstractSlashCommand.enum(
    name: String? = null,
    body: EnumArgumentBuilder<T>.() -> Unit = {}
): EnumArgumentBuilder<T> {
    val getter: (OptionMapping) -> T? = { map ->
        enumValues<T>().firstOrNull { it.name.equals(map.asString, true) }
    }
    return EnumArgumentBuilder(this, getter, enumValues()).apply(body)
        .apply { if (name != null) this@apply.name = name }
}


typealias ChoiceProvider = (CommandAutoCompleteInteractionEvent) -> List<Pair<String, String>>

class ChoicesArgumentBuilder(
    inst: AbstractSlashCommand,
    private val choices: List<String>? = null,
    choiceProvider: (ChoiceProvider)? = null
) : ArgumentBuilder<String>(inst, StringConverter) {

    init {
        if (choices == null && choiceProvider == null) {
            error("One of choices or choiceProvider must be specified")
        }
        if (choices != null && choiceProvider != null) {
            error("Both choices and choiceProvider cannot be specified")
        }

        if (choiceProvider != null) {
            super.autocomplete {
                choiceProvider.invoke(it)
            }
        }
    }

    override fun autocomplete(callback: AutoCompleteCallback) {
        error("Custom autocomplete is not supported")
    }

    override fun augmentOption(option: OptionData) {
        super.augmentOption(option)
        if (this.choices != null)
            option.addChoices(this.choices.map { Command.Choice(it, it) })
    }

}

inline fun AbstractSlashCommand.choices(
    name: String? = null,
    choices: List<String>? = null,
    noinline choiceProvider: ChoiceProvider? = null,
    body: ArgumentBuilder<String>.() -> Unit = {}
) = ChoicesArgumentBuilder(this, choices, choiceProvider).apply(body)
    .apply { if (name != null) this@apply.name = name }
