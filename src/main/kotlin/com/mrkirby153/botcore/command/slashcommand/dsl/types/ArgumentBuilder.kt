package com.mrkirby153.botcore.command.slashcommand.dsl.types

import com.mrkirby153.botcore.command.slashcommand.dsl.AbstractSlashCommand
import com.mrkirby153.botcore.command.slashcommand.dsl.ArgumentContainer
import com.mrkirby153.botcore.command.slashcommand.dsl.ArgumentConverter
import com.mrkirby153.botcore.command.slashcommand.dsl.SlashDsl
import com.mrkirby153.botcore.utils.SLF4J
import net.dv8tion.jda.api.interactions.commands.build.OptionData
import org.slf4j.Logger

/**
 * A generic Argument builder for building arguments
 */
@SlashDsl
open class ArgumentBuilder<T : Any>(
    private val inst: AbstractSlashCommand,
    private val converter: ArgumentConverter<T>
) {
    private val log: Logger by SLF4J

    @PublishedApi
    internal var name: String? = null
    var description: String = "No description provided"

    open val type = converter.type

    internal var autoCompleteCallback: AutoCompleteCallback? = null

    fun required() = ArgumentContainer(inst, converter, this, true)

    fun optional() = ArgumentContainer<T?, T>(inst, converter, this, false)

    fun optional(default: T) =
        ArgumentContainer(inst, converter, this, false, default)

    open fun autocomplete(callback: AutoCompleteCallback) {
        autoCompleteCallback = callback
    }

    open fun augmentOption(option: OptionData) {
        option.isAutoComplete = autoCompleteCallback != null
    }
}