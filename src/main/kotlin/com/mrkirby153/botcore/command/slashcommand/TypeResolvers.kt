package com.mrkirby153.botcore.command.slashcommand

import net.dv8tion.jda.api.entities.channel.concrete.Category
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel
import net.dv8tion.jda.api.interactions.commands.OptionMapping
import net.dv8tion.jda.api.interactions.commands.OptionType
import java.util.Locale

/**
 * Data class representing a type resolver for slash commands
 */
data class TypeResolver<T>(
    /**
     * The option type to report to Discord for resolution
     */
    val optionType: OptionType,

    val options: Map<String, String> = emptyMap(),

    val optionResolver: ((Class<*>) -> Map<String, String>) = { options },
    /**
     * The actual resolver
     */
    val resolver: (OptionMapping, Class<*>) -> T?
)

/**
 * An exception thrown when an error occurs during type resolution
 */
class TypeResolutionException(e: String) : RuntimeException(e)

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
val STRING_TYPE_RESOLVER = TypeResolver(OptionType.STRING) { it, _ ->
    return@TypeResolver it.asString
}

/**
 * The type resolver for resolving [Int]
 */
val INT_TYPE_RESOLVER = TypeResolver(OptionType.INTEGER) { it, _ ->
    return@TypeResolver it.asLong.toInt()
}

/**
 * The type resolver for resolving [Long]
 */
val LONG_TYPE_RESOLVER = TypeResolver(OptionType.INTEGER) { it, _ ->
    return@TypeResolver it.asLong
}

/**
 * The type resolver for resolving [Boolean]
 */
val BOOLEAN_TYPE_RESOLVER = TypeResolver(OptionType.BOOLEAN) { it, _ ->
    return@TypeResolver it.asBoolean
}

/**
 * The type resolver for resolving Users
 */
val USER_TYPE_RESOLVER = TypeResolver(OptionType.USER) { it, _ ->
    return@TypeResolver it.asUser
}

/**
 * The type resolver for resolving [TextChannel]
 */
val TEXT_CHANNEL_TYPE_RESOLVER = TypeResolver(OptionType.CHANNEL) { it, _ ->
    return@TypeResolver resolveChannel<TextChannel>(it.asChannel.asTextChannel(), "Text Channel")
}

/**
 * The type resolver for resolving [VoiceChannel]
 */
val VOICE_CHANNEL_TYPE_RESOLVER = TypeResolver(OptionType.CHANNEL) { it, _ ->
    return@TypeResolver resolveChannel<VoiceChannel>(it.asChannel.asVoiceChannel(), "Voice Channel")
}

/**
 * The type resolver for resolving [Category]
 */
val CATEGORY_TYPE_RESOLVER = TypeResolver(OptionType.MENTIONABLE) { it, _ ->
    return@TypeResolver resolveChannel<Category>(it.asChannel.asCategory(), "Category")
}

/**
 * The type resolver for resolving roles
 */
val ROLE_TYPE_RESOLVER = TypeResolver(OptionType.ROLE) { it, _ ->
    return@TypeResolver it.asRole
}

/**
 * The type resolver for resolving a generic mentionable
 */
val MENTIONABLE_TYPE_RESOLVER = TypeResolver(OptionType.MENTIONABLE) { it, _ ->
    return@TypeResolver it.asMentionable
}

/**
 * The type resolver for resolving [Enum]
 */
val ENUM_TYPE_RESOLVER = TypeResolver(OptionType.STRING, optionResolver = { type ->
    (type as Class<Enum<*>>).enumConstants.associateBy({ it.name }, { enum ->
        enum.name.split("_").joinToString(" ") { str ->
            str.lowercase().replaceFirstChar {
                if (it.isLowerCase()) it.titlecase(
                    Locale.getDefault()
                ) else it.toString()
            }
        }
    })
}) { optionType, type ->
    val enumClazz = type as Class<Enum<*>>
    val names = enumClazz.enumConstants.joinToString(", ") { it.name }
    enumClazz.enumConstants.firstOrNull { it.name.equals(optionType.asString, true) }
        ?: throw TypeResolutionException("Parameter must be one of $names")
}

/**
 * THe type resolver for resolving uploaded files
 */
val FILE_TYPE_RESOLVER = TypeResolver(OptionType.ATTACHMENT) { it, _ ->
    return@TypeResolver it.asAttachment
}