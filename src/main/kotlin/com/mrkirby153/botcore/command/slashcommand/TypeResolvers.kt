package com.mrkirby153.botcore.command.slashcommand

import net.dv8tion.jda.api.entities.Category
import net.dv8tion.jda.api.entities.GuildChannel
import net.dv8tion.jda.api.entities.TextChannel
import net.dv8tion.jda.api.entities.VoiceChannel
import net.dv8tion.jda.api.interactions.commands.OptionMapping
import net.dv8tion.jda.api.interactions.commands.OptionType

/**
 * Data class representing a type resolver for slash commands
 */
data class TypeResolver<T>(
    /**
     * The option type to report to Discord for resolution
     */
    val optionType: OptionType,
    /**
     * The actual resolver
     */
    val resolver: (OptionMapping) -> T?
)

/**
 * An exception thrown when an error occurs during type resolution
 */
class TypeResolutionException(e: String) : Exception(e)

/**
 * Resolves a channel from the provided [channel]. Specify [friendlyType] to tell the user what the
 * channel is not
 */
private inline fun <reified T : GuildChannel> resolveChannel(
    channel: GuildChannel,
    friendlyType: String
): T {
    if (channel !is T) {
        throw TypeResolutionException("Provided channel is not a $friendlyType")
    }
    return channel
}

/**
 * The type resolver for resolving [String]
 */
val STRING_TYPE_RESOLVER = TypeResolver(OptionType.STRING) {
    return@TypeResolver it.asString
}

/**
 * The type resolver for resolving [Int]
 */
val INT_TYPE_RESOLVER = TypeResolver(OptionType.INTEGER) {
    return@TypeResolver it.asLong.toInt()
}

/**
 * The type resolver for resolving [Long]
 */
val LONG_TYPE_RESOLVER = TypeResolver(OptionType.INTEGER) {
    return@TypeResolver it.asLong
}

/**
 * The type resolver for resolving [Boolean]
 */
val BOOLEAN_TYPE_RESOLVER = TypeResolver(OptionType.BOOLEAN) {
    return@TypeResolver it.asBoolean
}

/**
 * The type resolver for resolving Users
 */
val USER_TYPE_RESOLVER = TypeResolver(OptionType.USER) {
    return@TypeResolver it.asUser
}

/**
 * The type resolver for resolving [TextChannel]
 */
val TEXT_CHANNEL_TYPE_RESOLVER = TypeResolver(OptionType.CHANNEL) {
    return@TypeResolver resolveChannel<TextChannel>(it.asGuildChannel, "Text Channel")
}

/**
 * The type resolver for resolving [VoiceChannel]
 */
val VOICE_CHANNEL_TYPE_RESOLVER = TypeResolver(OptionType.CHANNEL) {
    return@TypeResolver resolveChannel<VoiceChannel>(it.asGuildChannel, "Voice Channel")
}

/**
 * The type resolver for resolving [Category]
 */
val CATEGORY_TYPE_RESOLVER = TypeResolver(OptionType.MENTIONABLE) {
    return@TypeResolver resolveChannel<Category>(it.asGuildChannel, "Category")
}

/**
 * The type resolver for resolving roles
 */
val ROLE_TYPE_RESOLVER = TypeResolver(OptionType.ROLE) {
    return@TypeResolver it.asRole
}

/**
 * The type resolver for resolving a generic mentionable
 */
val MENTIONABLE_TYPE_RESOLVER = TypeResolver(OptionType.MENTIONABLE) {
    return@TypeResolver it.asMentionable
}