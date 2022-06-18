package com.mrkirby153.botcore.command.slashcommand.dsl.types

import com.mrkirby153.botcore.command.args.ArgumentParseException
import com.mrkirby153.botcore.command.slashcommand.dsl.ArgumentConverter
import com.mrkirby153.botcore.command.slashcommand.dsl.Arguments
import net.dv8tion.jda.api.entities.GuildChannel
import net.dv8tion.jda.api.entities.GuildMessageChannel
import net.dv8tion.jda.api.entities.StageChannel
import net.dv8tion.jda.api.entities.TextChannel
import net.dv8tion.jda.api.entities.ThreadChannel
import net.dv8tion.jda.api.entities.VoiceChannel
import net.dv8tion.jda.api.interactions.commands.OptionMapping
import net.dv8tion.jda.api.interactions.commands.OptionType

class ChannelArgument<T>(val mapper: (OptionMapping) -> T) : ArgumentConverter<T> {
    override fun convert(input: OptionMapping): T {
        return mapper(input)
    }
}

private val textChannelConverter = {
    ChannelArgument {
        it.asTextChannel ?: throw ArgumentParseException("Provided channel was not a text channel")
    }
}

private val voiceChannelConverter = {
    ChannelArgument {
        it.asVoiceChannel
            ?: throw ArgumentParseException("Provided channel was not a voice channel")
    }
}

private val guildChannelConverter = {
    ChannelArgument {
        it.asGuildChannel
    }
}

private val messageChannelConverter = {
    ChannelArgument {
        it.asMessageChannel
            ?: throw ArgumentParseException("Provided channel was not a message channel")
    }
}

private val stageChannelConverter = {
    ChannelArgument {
        it.asStageChannel
            ?: throw ArgumentParseException("Provided channel was not a stage channel")
    }
}

private val threadChannelConverter = {
    ChannelArgument {
        it.asThreadChannel ?: throw ArgumentParseException("Provided channel was not a thread")
    }
}

class TextChannelArgument : GenericArgument<TextChannel>(OptionType.CHANNEL, textChannelConverter)
class OptionalTextChannelArgument :
    GenericNullableArgument<TextChannel>(OptionType.CHANNEL, textChannelConverter)

class VoiceChannelArgument :
    GenericArgument<VoiceChannel>(OptionType.CHANNEL, voiceChannelConverter)

class OptionalVoiceChannelArgument :
    GenericNullableArgument<VoiceChannel>(OptionType.CHANNEL, voiceChannelConverter)

class GuildChannelArgument :
    GenericArgument<GuildChannel>(OptionType.CHANNEL, guildChannelConverter)

class OptionalGuildChannelArgument :
    GenericNullableArgument<GuildChannel>(OptionType.CHANNEL, guildChannelConverter)

class MessageChannelArgument :
    GenericArgument<GuildMessageChannel>(OptionType.CHANNEL, messageChannelConverter)

class OptionalMessageChannelArgument :
    GenericNullableArgument<GuildMessageChannel>(OptionType.CHANNEL, messageChannelConverter)

class StageChannelArgument :
    GenericArgument<StageChannel>(OptionType.CHANNEL, stageChannelConverter)

class OptionalStageChannelArgument :
    GenericNullableArgument<StageChannel>(OptionType.CHANNEL, stageChannelConverter)

class ThreadChannelArgument :
    GenericArgument<ThreadChannel>(OptionType.CHANNEL, threadChannelConverter)

class OptionalThreadChannelArgument :
    GenericNullableArgument<ThreadChannel>(OptionType.CHANNEL, threadChannelConverter)

fun Arguments.textChannel(body: TextChannelArgument.() -> Unit) =
    genericArgument(::TextChannelArgument, body)

fun Arguments.optionalTextChannel(body: OptionalTextChannelArgument.() -> Unit) =
    optionalGenericArgument(::OptionalTextChannelArgument, body)

fun Arguments.voiceChannel(body: VoiceChannelArgument.() -> Unit) =
    genericArgument(::VoiceChannelArgument, body)

fun Arguments.optionalVoiceChannel(body: OptionalVoiceChannelArgument.() -> Unit) =
    optionalGenericArgument(::OptionalVoiceChannelArgument, body)

fun Arguments.guildChannel(body: GuildChannelArgument.() -> Unit) =
    genericArgument(::GuildChannelArgument, body)

fun Arguments.optionalGuildChannel(body: OptionalGuildChannelArgument.() -> Unit) =
    optionalGenericArgument(::OptionalGuildChannelArgument, body)

fun Arguments.messageChannel(body: MessageChannelArgument.() -> Unit) =
    genericArgument(::MessageChannelArgument, body)

fun Arguments.optionalMessageChannel(body: OptionalMessageChannelArgument.() -> Unit) =
    optionalGenericArgument(::OptionalMessageChannelArgument, body)

fun Arguments.stageChannel(body: StageChannelArgument.() -> Unit) =
    genericArgument(::StageChannelArgument, body)

fun Arguments.optionalStageChannel(body: OptionalStageChannelArgument.() -> Unit) =
    optionalGenericArgument(::OptionalStageChannelArgument, body)

fun Arguments.threadChannel(body: ThreadChannelArgument.() -> Unit) =
    genericArgument(::ThreadChannelArgument, body)

fun Arguments.optionalThreadChannel(body: OptionalThreadChannelArgument.() -> Unit) =
    optionalGenericArgument(::OptionalThreadChannelArgument, body)