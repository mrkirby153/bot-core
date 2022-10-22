package com.mrkirby153.botcore.command.slashcommand.dsl.types

import com.mrkirby153.botcore.command.args.ArgumentParseException
import com.mrkirby153.botcore.command.slashcommand.dsl.ArgumentConverter
import com.mrkirby153.botcore.command.slashcommand.dsl.Arguments
import net.dv8tion.jda.api.entities.channel.Channel
import net.dv8tion.jda.api.entities.channel.ChannelType
import net.dv8tion.jda.api.entities.channel.concrete.StageChannel
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel
import net.dv8tion.jda.api.interactions.commands.OptionMapping
import net.dv8tion.jda.api.interactions.commands.OptionType

class ChannelArgumentConverter<T>(val mapper: (OptionMapping) -> T) : ArgumentConverter<T> {
    override fun convert(input: OptionMapping): T {
        return mapper(input)
    }

    override val type = OptionType.CHANNEL
}

class ChannelArgumentBuilder<T : Channel>(
    inst: Arguments,
    converter: ChannelArgumentConverter<T>,
    private vararg val allowedTypes: ChannelType
) : ArgumentBuilder<T>(inst, converter) {

    override fun createOption() = super.createOption().apply {
        setChannelTypes(*allowedTypes)
    }
}

private val textChannelConverter = ChannelArgumentConverter {
    it.asChannel as? TextChannel
        ?: throw ArgumentParseException("Provided channel was not a text channel")
}

private val voiceChannelConverter = ChannelArgumentConverter {
    it.asChannel as? VoiceChannel
        ?: throw ArgumentParseException("Provided channel was not a voice channel")
}

private val guildChannelConverter = ChannelArgumentConverter {
    it.asChannel as? GuildChannel
        ?: throw ArgumentParseException("Provided channel was not a guild channel")
}

private val messageChannelConverter = ChannelArgumentConverter {
    it.asChannel as? MessageChannel
        ?: throw ArgumentParseException("Provided channel was not a message channel")
}

private val stageChannelConverter = ChannelArgumentConverter {
    it.asChannel as? StageChannel
        ?: throw ArgumentParseException("Provided channel was not a stage channel")
}

private val threadChannelConverter = ChannelArgumentConverter {
    it.asChannel as? ThreadChannel
        ?: throw ArgumentParseException("Provided channel was not a thread")
}

fun Arguments.textChannel(body: ChannelArgumentBuilder<TextChannel>.() -> Unit) =
    ChannelArgumentBuilder(this, textChannelConverter, ChannelType.TEXT).apply(body)

fun Arguments.voiceChannel(body: ChannelArgumentBuilder<VoiceChannel>.() -> Unit) =
    ChannelArgumentBuilder(this, voiceChannelConverter, ChannelType.VOICE).apply(body)

fun Arguments.guildChannel(body: ChannelArgumentBuilder<GuildChannel>.() -> Unit) =
    ChannelArgumentBuilder(
        this,
        guildChannelConverter,
        ChannelType.TEXT,
        ChannelType.VOICE,
        ChannelType.NEWS,
        ChannelType.STAGE
    ).apply(body)

fun Arguments.messageChannel(body: ChannelArgumentBuilder<MessageChannel>.() -> Unit) =
    ChannelArgumentBuilder(
        this,
        messageChannelConverter,
        ChannelType.TEXT,
        ChannelType.PRIVATE,
        ChannelType.VOICE
    ).apply(body)

fun Arguments.stageChannel(body: ChannelArgumentBuilder<StageChannel>.() -> Unit) =
    ChannelArgumentBuilder(this, stageChannelConverter, ChannelType.STAGE)

fun Arguments.thread(body: ChannelArgumentBuilder<ThreadChannel>.() -> Unit) =
    ChannelArgumentBuilder(
        this,
        threadChannelConverter,
        ChannelType.GUILD_NEWS_THREAD,
        ChannelType.GUILD_PUBLIC_THREAD,
        ChannelType.GUILD_PRIVATE_THREAD
    )