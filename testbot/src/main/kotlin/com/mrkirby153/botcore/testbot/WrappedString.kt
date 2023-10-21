package com.mrkirby153.botcore.testbot

import com.mrkirby153.botcore.command.slashcommand.dsl.AbstractSlashCommand
import com.mrkirby153.botcore.command.slashcommand.dsl.ArgumentConverter
import com.mrkirby153.botcore.command.slashcommand.dsl.WrappedData
import com.mrkirby153.botcore.command.slashcommand.dsl.types.ArgumentBuilder
import com.mrkirby153.botcore.utils.SLF4J
import net.dv8tion.jda.api.interactions.commands.OptionMapping

object WrappedStringConverter : ArgumentConverter<WrappedData<String>> {
    override fun convert(input: OptionMapping): WrappedData<String> {
        return SimpleWrappedData(input.asString)
    }

}

fun AbstractSlashCommand.wrappedString(
    name: String? = null,
    body: ArgumentBuilder<String, WrappedData<String>>.() -> Unit = {}
) =
    ArgumentBuilder<String, WrappedData<String>>(
        this,
        WrappedStringConverter
    ).apply { if (name != null) this@apply.name = name }
        .apply(body)


class SimpleWrappedData<T>(private val data: T) : WrappedData<T> {
    private val log by SLF4J

    override fun get(): T {
        log.warn("SimpleWrappedData invoked!")
        return data
    }
}