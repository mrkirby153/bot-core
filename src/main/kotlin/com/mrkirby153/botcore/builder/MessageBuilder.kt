package com.mrkirby153.botcore.builder

import net.dv8tion.jda.api.entities.Channel
import net.dv8tion.jda.api.entities.Emote
import net.dv8tion.jda.api.entities.IMentionable
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.Message.MentionType
import net.dv8tion.jda.api.entities.Role
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.utils.MarkdownSanitizer

@DslMarker
annotation class MessageDsl

fun message(builder: MessageBuilder.() -> Unit) = MessageBuilder().apply(builder).build()

@MessageDsl
class MessageBuilder : Builder<Message> {
    var tts = false
    var content = ""
    val embeds = mutableListOf<EmbedBuilder>()
    val actionRows = mutableListOf<ActionRowBuilder>()

    private val mentions = mutableListOf<IMentionable>()
    private val allowMentions = mutableSetOf<MentionType>()

    override fun build(): Message {
        val jdaMessageBuilder = net.dv8tion.jda.api.MessageBuilder()
        jdaMessageBuilder.setContent(content)
        jdaMessageBuilder.setActionRows(actionRows.map { it.build() })
        jdaMessageBuilder.setEmbeds(embeds.map { it.build() })
        jdaMessageBuilder.setAllowedMentions(allowMentions)
        jdaMessageBuilder.mention(mentions)
        jdaMessageBuilder.setTTS(tts)
        return jdaMessageBuilder.build()
    }

    fun mention(mentionable: IMentionable, modifyAllowMentions: Boolean = true) {
        if (modifyAllowMentions) {
            when (mentionable) {
                is User -> allowMentions.add(MentionType.USER)
                is Role -> allowMentions.add(MentionType.ROLE)
                is Channel -> allowMentions.add(MentionType.CHANNEL)
                is Emote -> allowMentions.add(MentionType.EMOTE)
            }
        }
        mentions.add(mentionable)
    }

    fun allowMention(type: MentionType) {
        allowMentions.add(type)
    }

    fun denyMention(type: MentionType) {
        allowMentions.remove(type)
    }

    inline fun text(safe: Boolean = true, builder: MessageContentBuilder.() -> Unit) {
        this.content = MessageContentBuilder(safe).apply(builder).build()
    }

    inline fun embed(builder: EmbedBuilder.() -> Unit) {
        embeds.add(EmbedBuilder().apply(builder))
    }

    inline fun actionRow(builder: ActionRowBuilder.() -> Unit) {
        actionRows.add(ActionRowBuilder().apply(builder))
    }
}

@MessageDsl
class MessageContentBuilder(private val safe: Boolean) : Builder<String> {

    private val stringBuilder = StringBuilder()

    fun bold(message: String) {
        appendMarkdown(
            "**", if (safe) {
                MarkdownSanitizer.escape(message)
            } else {
                message
            }
        )
    }

    fun italic(message: String) {
        appendMarkdown(
            "*", if (safe) {
                MarkdownSanitizer.escape(message)
            } else {
                message
            }
        )
    }

    fun underline(message: String) {
        appendMarkdown(
            "__", if (safe) {
                MarkdownSanitizer.escape(message)
            } else {
                message
            }
        )
    }

    fun appendBlock(message: String) {
        appendMarkdown(
            "`", if (safe) {
                MarkdownSanitizer.escape(message)
            } else {
                message
            }
        )
    }

    fun append(message: String) {
        stringBuilder.append(
            if (safe) {
                MarkdownSanitizer.escape(message)
            } else {
                message
            }
        )
    }

    fun appendLine(message: String = "") {
        stringBuilder.appendLine(
            if (safe) {
                MarkdownSanitizer.escape(message)
            } else {
                message
            }
        )
    }

    fun code(text: String, language: String = "") {
        stringBuilder.append("```$language\n$text\n```")
    }

    private fun appendMarkdown(format: String, content: String) {
        stringBuilder.append(format).append(content).append(format)
    }

    override fun build(): String = stringBuilder.toString()
}