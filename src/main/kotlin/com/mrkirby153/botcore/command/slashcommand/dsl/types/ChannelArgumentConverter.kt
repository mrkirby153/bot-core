package com.mrkirby153.botcore.command.slashcommand.dsl.types

import com.mrkirby153.botcore.command.slashcommand.dsl.AbstractSlashCommand
import com.mrkirby153.botcore.command.slashcommand.dsl.ArgumentConverter
import com.mrkirby153.botcore.command.slashcommand.dsl.ArgumentParseException
import net.dv8tion.jda.api.entities.channel.Channel
import net.dv8tion.jda.api.entities.channel.ChannelType
import net.dv8tion.jda.api.entities.channel.concrete.ForumChannel
import net.dv8tion.jda.api.entities.channel.concrete.StageChannel
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel
import net.dv8tion.jda.api.interactions.commands.OptionMapping
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.OptionData

class ChannelArgumentConverter<T>(val mapper: (OptionMapping) -> T) : ArgumentConverter<T> {
    override fun convert(input: OptionMapping): T {
        return mapper(input)
    }

    override val type = OptionType.CHANNEL
}

class ChannelArgumentBuilder<T : Channel>(
    inst: AbstractSlashCommand,
    converter: ChannelArgumentConverter<T>,
    private vararg val allowedTypes: ChannelType
) : SimpleArgumentBuilder<T>(inst, converter) {

    override fun augmentOption(option: OptionData) {
        super.augmentOption(option)
        option.setChannelTypes(*allowedTypes)
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

private val forumChannelConverter = ChannelArgumentConverter {
    it.asChannel as? ForumChannel
        ?: throw ArgumentParseException("Provided channel was not a forum")
}

fun AbstractSlashCommand.textChannel(
    name: String? = null,
    body: ChannelArgumentBuilder<TextChannel>.() -> Unit = {}
) = ChannelArgumentBuilder(this, textChannelConverter, ChannelType.TEXT).apply(body)
    .apply { if (name != null) this@apply.name = name }

fun AbstractSlashCommand.voiceChannel(
    name: String? = null,
    body: ChannelArgumentBuilder<VoiceChannel>.() -> Unit = {}
) = ChannelArgumentBuilder(this, voiceChannelConverter, ChannelType.VOICE).apply(body)
    .apply { if (name != null) this@apply.name = name }

fun AbstractSlashCommand.guildChannel(
    name: String? = null,
    body: ChannelArgumentBuilder<GuildChannel>.() -> Unit = {}
) = ChannelArgumentBuilder(
    this,
    guildChannelConverter,
    ChannelType.TEXT,
    ChannelType.VOICE,
    ChannelType.NEWS,
    ChannelType.STAGE
).apply(body)
    .apply { if (name != null) this@apply.name = name }

fun AbstractSlashCommand.messageChannel(
    name: String? = null,
    body: ChannelArgumentBuilder<MessageChannel>.() -> Unit = {}
) = ChannelArgumentBuilder(
    this,
    messageChannelConverter,
    ChannelType.TEXT,
    ChannelType.PRIVATE,
    ChannelType.VOICE
).apply(body)
    .apply { if (name != null) this@apply.name = name }

fun AbstractSlashCommand.stageChannel(
    name: String? = null,
    body: ChannelArgumentBuilder<StageChannel>.() -> Unit = {}
) = ChannelArgumentBuilder(this, stageChannelConverter, ChannelType.STAGE).apply(body)
    .apply { if (name != null) this@apply.name = name }

fun AbstractSlashCommand.thread(
    name: String? = null,
    body: ChannelArgumentBuilder<ThreadChannel>.() -> Unit = {}
) = ChannelArgumentBuilder(
    this,
    threadChannelConverter,
    ChannelType.GUILD_NEWS_THREAD,
    ChannelType.GUILD_PUBLIC_THREAD,
    ChannelType.GUILD_PRIVATE_THREAD
).apply(body)
    .apply { if (name != null) this@apply.name = name }

fun AbstractSlashCommand.forum(
    name: String? = null,
    body: ChannelArgumentBuilder<ForumChannel>.() -> Unit = {}
) = ChannelArgumentBuilder(this, forumChannelConverter, ChannelType.FORUM).apply(body)
    .apply { if (name != null) this@apply.name = name }
