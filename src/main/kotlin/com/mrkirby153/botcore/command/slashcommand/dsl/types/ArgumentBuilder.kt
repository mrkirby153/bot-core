package com.mrkirby153.botcore.command.slashcommand.dsl.types

import com.mrkirby153.botcore.command.slashcommand.dsl.ArgumentContainer
import com.mrkirby153.botcore.command.slashcommand.dsl.ArgumentConverter
import com.mrkirby153.botcore.command.slashcommand.dsl.Arguments
import com.mrkirby153.botcore.log
import net.dv8tion.jda.api.interactions.commands.build.OptionData

/**
 * A generic Argument builder for building arguments
 */
open class ArgumentBuilder<T : Any>(
    private val inst: Arguments,
    private val converter: ArgumentConverter<T>
) {
    lateinit var name: String
    lateinit var description: String

    internal var autoCompleteCallback: AutoCompleteCallback? = null

    fun required() = ArgumentContainer(converter, this, true).also { inst.addArgument(it) }

    fun optional() = ArgumentContainer<T?, T>(converter, this, false).also { inst.addArgument(it) }

    fun autocomplete(callback: AutoCompleteCallback) {
        autoCompleteCallback = callback
    }

    internal open fun createOption(): OptionData =
        OptionData(converter.type, name, description).apply {
            isAutoComplete = autoCompleteCallback != null
            log.trace("Created option $name ($description) with type $type. Autocomplete? $isAutoComplete")
        }
}