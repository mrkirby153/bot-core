package com.mrkirby153.botcore.command.slashcommand.dsl.types

import com.mrkirby153.botcore.command.args.ArgumentParseException
import com.mrkirby153.botcore.command.slashcommand.dsl.ArgumentConverter
import com.mrkirby153.botcore.command.slashcommand.dsl.Arguments
import com.mrkirby153.botcore.command.slashcommand.dsl.NullableArgument
import net.dv8tion.jda.api.entities.Channel
import net.dv8tion.jda.api.entities.ChannelType
import net.dv8tion.jda.api.entities.GuildChannel
import net.dv8tion.jda.api.entities.GuildMessageChannel
import net.dv8tion.jda.api.entities.StageChannel
import net.dv8tion.jda.api.entities.TextChannel
import net.dv8tion.jda.api.entities.ThreadChannel
import net.dv8tion.jda.api.entities.VoiceChannel
import net.dv8tion.jda.api.interactions.commands.OptionMapping
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.OptionData

class ChannelArgumentConverter<T>(val mapper: (OptionMapping) -> T) : ArgumentConverter<T> {
    override fun convert(input: OptionMapping): T {
        return mapper(input)
    }
}

open class ChannelArgument<T : Channel>(
    converter: () -> ArgumentConverter<T>,
    private vararg val allowedTypes: ChannelType
) : GenericArgument<T>(OptionType.CHANNEL, converter), ModifiesOption {
    override fun modify(option: OptionData) {
        if (allowedTypes.isNotEmpty()) {
            option.setChannelTypes(*allowedTypes)
        }
    }
}

open class NullableChannelArgument<T : Channel>(
    converter: () -> ArgumentConverter<T>,
    private vararg val allowedTypes: ChannelType
) : GenericNullableArgument<T>(OptionType.CHANNEL, converter), ModifiesOption {
    override fun modify(option: OptionData) {
        if (allowedTypes.isNotEmpty()) {
            option.setChannelTypes(*allowedTypes)
        }
    }
}

private val textChannelConverter = {
    ChannelArgumentConverter {
        it.asTextChannel ?: throw ArgumentParseException("Provided channel was not a text channel")
    }
}

private val voiceChannelConverter = {
    ChannelArgumentConverter {
        it.asVoiceChannel
            ?: throw ArgumentParseException("Provided channel was not a voice channel")
    }
}

private val guildChannelConverter = {
    ChannelArgumentConverter {
        it.asGuildChannel
    }
}

private val messageChannelConverter = {
    ChannelArgumentConverter {
        it.asMessageChannel
            ?: throw ArgumentParseException("Provided channel was not a message channel")
    }
}

private val stageChannelConverter = {
    ChannelArgumentConverter {
        it.asStageChannel
            ?: throw ArgumentParseException("Provided channel was not a stage channel")
    }
}

private val threadChannelConverter = {
    ChannelArgumentConverter {
        it.asThreadChannel ?: throw ArgumentParseException("Provided channel was not a thread")
    }
}

class TextChannelArgument : ChannelArgument<TextChannel>(textChannelConverter, ChannelType.TEXT)
class OptionalTextChannelArgument :
    NullableChannelArgument<TextChannel>(textChannelConverter, ChannelType.TEXT)

class VoiceChannelArgument :
    ChannelArgument<VoiceChannel>(voiceChannelConverter, ChannelType.VOICE)

class OptionalVoiceChannelArgument :
    NullableChannelArgument<VoiceChannel>(voiceChannelConverter, ChannelType.VOICE)

class GuildChannelArgument :
    ChannelArgument<GuildChannel>(
        guildChannelConverter,
        ChannelType.TEXT,
        ChannelType.VOICE,
        ChannelType.NEWS,
        ChannelType.STAGE
    )

class OptionalGuildChannelArgument :
    NullableChannelArgument<GuildChannel>(
        guildChannelConverter,
        ChannelType.TEXT,
        ChannelType.VOICE,
        ChannelType.NEWS,
        ChannelType.STAGE
    )

class MessageChannelArgument :
    ChannelArgument<GuildMessageChannel>(
        messageChannelConverter,
        ChannelType.TEXT,
        ChannelType.PRIVATE,
        ChannelType.NEWS
    )

class OptionalMessageChannelArgument :
    NullableChannelArgument<GuildMessageChannel>(
        messageChannelConverter,
        ChannelType.TEXT,
        ChannelType.PRIVATE,
        ChannelType.NEWS
    )

class StageChannelArgument :
    ChannelArgument<StageChannel>(stageChannelConverter, ChannelType.STAGE)

class OptionalStageChannelArgument :
    NullableChannelArgument<StageChannel>(stageChannelConverter, ChannelType.STAGE)

class ThreadChannelArgument :
    ChannelArgument<ThreadChannel>(
        threadChannelConverter,
        ChannelType.GUILD_NEWS_THREAD,
        ChannelType.GUILD_PUBLIC_THREAD,
        ChannelType.GUILD_PRIVATE_THREAD
    )

class OptionalThreadChannelArgument :
    NullableChannelArgument<ThreadChannel>(
        threadChannelConverter,
        ChannelType.GUILD_NEWS_THREAD,
        ChannelType.GUILD_PUBLIC_THREAD,
        ChannelType.GUILD_PRIVATE_THREAD
    )

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