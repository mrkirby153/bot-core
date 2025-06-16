package com.mrkirby153.botcore.builder

import com.mrkirby153.botcore.builder.componentsv2.ActionRowBuilder
import net.dv8tion.jda.api.entities.IMentionable
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.Message.MentionType
import net.dv8tion.jda.api.entities.Role
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.entities.channel.Channel
import net.dv8tion.jda.api.entities.emoji.CustomEmoji
import net.dv8tion.jda.api.utils.MarkdownSanitizer
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder
import net.dv8tion.jda.api.utils.messages.MessageCreateData
import net.dv8tion.jda.api.utils.messages.MessageEditBuilder
import net.dv8tion.jda.api.utils.messages.MessageEditData

/**
 * Marker annotation for the Messasge DSL
 */
@DslMarker
annotation class MessageDsl

/**
 * Builds a [Message]
 *
 * @return A [MessageBuilder]
 */
fun message(builder: MessageBuilder.() -> Unit) = MessageBuilder().apply(builder)

/**
 * A [Message] builder
 */
@MessageDsl
class MessageBuilder {
    /**
     * If this message should be spoken aloud
     */
    var tts = false

    /**
     * The content of this message
     */
    var content = ""

    /**
     * The embeds in this message
     */
    val embeds = mutableListOf<EmbedBuilder>()

    /**
     * The message's action rows
     */
    val actionRows = mutableListOf<ActionRowBuilder>()

    private val mentions = mutableListOf<IMentionable>()
    private val allowMentions = mutableSetOf<MentionType>()

    fun create(): MessageCreateData {
        val jdaMessageBuilder = MessageCreateBuilder()
        jdaMessageBuilder.setContent(content)
        jdaMessageBuilder.setComponents(actionRows.map { it.build() })
        jdaMessageBuilder.setEmbeds(embeds.map { it.build() })
        jdaMessageBuilder.setAllowedMentions(allowMentions)
        jdaMessageBuilder.mention(mentions)
        jdaMessageBuilder.setTTS(tts)
        return jdaMessageBuilder.build()
    }

    fun edit(): MessageEditData {
        val jdaMessageBuilder = MessageEditBuilder()
        jdaMessageBuilder.setContent(content)
        jdaMessageBuilder.setComponents(actionRows.map { it.build() })
        jdaMessageBuilder.setEmbeds(embeds.map { it.build() })
        jdaMessageBuilder.setAllowedMentions(allowMentions)
        jdaMessageBuilder.mention(mentions)
        return jdaMessageBuilder.build()
    }

    /**
     * Adds the provided [mentionable] to the message's mentions.
     *
     * By default, the allow_mentions will be modified, but this can be disabled with [modifyAllowMentions]
     */
    fun mention(mentionable: IMentionable, modifyAllowMentions: Boolean = true) {
        if (modifyAllowMentions) {
            when (mentionable) {
                is User -> allowMentions.add(MentionType.USER)
                is Role -> allowMentions.add(MentionType.ROLE)
                is Channel -> allowMentions.add(MentionType.CHANNEL)
                is CustomEmoji -> allowMentions.add(MentionType.EMOJI)
            }
        }
        mentions.add(mentionable)
    }

    /**
     * Adds the given [type] to the allow_mentions list
     */
    fun allowMention(type: MentionType) {
        allowMentions.add(type)
    }

    /**
     * Removes the given [type] from the allow_mentions list
     */
    fun denyMention(type: MentionType) {
        allowMentions.remove(type)
    }

    /**
     * Sets the message's content.
     *
     * Specify [safe] to automatically escape markdown in the message (Default behavior)
     */
    inline fun text(safe: Boolean = true, builder: MessageContentBuilder.() -> Unit) {
        this.content = MessageContentBuilder(safe).apply(builder).build()
    }

    /**
     * Adds an embed to the message
     */
    inline fun embed(builder: EmbedBuilder.() -> Unit) {
        embeds.add(EmbedBuilder().apply(builder))
    }

    /**
     * Adds an action row to the message
     */
    inline fun actionRow(id: Int? = null, builder: ActionRowBuilder.() -> Unit) {
        actionRows.add(ActionRowBuilder(id, false).apply(builder))
    }
}

/**
 * Builder for a [Message]'s content
 *
 * @param safe If markdown should be escaped
 */
@MessageDsl
class MessageContentBuilder(private val safe: Boolean) : Builder<String> {

    private val stringBuilder = StringBuilder()

    /**
     * Appends the bold [message] to the message
     */
    fun bold(message: String) {
        appendMarkdown(
            "**", if (safe) {
                MarkdownSanitizer.escape(message)
            } else {
                message
            }
        )
    }

    /**
     * Appends the italic [message] to the message
     */
    fun italic(message: String) {
        appendMarkdown(
            "*", if (safe) {
                MarkdownSanitizer.escape(message)
            } else {
                message
            }
        )
    }

    /**
     * Appends the underline [message] to the message
     */
    fun underline(message: String) {
        appendMarkdown(
            "__", if (safe) {
                MarkdownSanitizer.escape(message)
            } else {
                message
            }
        )
    }

    /**
     * Appends an inline code block of [message] to the message
     */
    fun appendBlock(message: String) {
        appendMarkdown(
            "`", if (safe) {
                MarkdownSanitizer.escape(message)
            } else {
                message
            }
        )
    }

    /**
     * Appends the raw [message] to the message
     */
    fun append(message: String) {
        stringBuilder.append(
            if (safe) {
                MarkdownSanitizer.escape(message)
            } else {
                message
            }
        )
    }

    /**
     * Appends the raw [message] to the message, with a newline
     */
    fun appendLine(message: String = "") {
        stringBuilder.appendLine(
            if (safe) {
                MarkdownSanitizer.escape(message)
            } else {
                message
            }
        )
    }

    /**
     * Appends a code block in the provided [language] to the message
     */
    fun code(text: String, language: String = "") {
        stringBuilder.append("```$language\n$text\n```")
    }

    private fun appendMarkdown(format: String, content: String) {
        stringBuilder.append(format).append(content).append(format)
    }

    override fun build(): String = stringBuilder.toString()
}