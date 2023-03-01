package com.mrkirby153.botcore.builder

import net.dv8tion.jda.api.utils.messages.MessageCreateData


fun autoSplittingMessage(
    maxCharactersPerMessage: Int = 1990,
    body: AutoSplittingMessageBuilder.() -> Unit,
): List<MessageCreateData> {
    val builder = AutoSplittingMessageBuilder(maxCharactersPerMessage)
    body(builder)
    return builder.build()
}

/**
 * A message builder that will automatically split messages after a certain length
 */
class AutoSplittingMessageBuilder(private val maxCharactersPerMessage: Int = 1990) {
    private val messages = mutableListOf<String>()
    private var buffer = ""

    private var built = false

    /**
     * Appends a [data] to the message, splitting the message into multiple if needed.
     * Specify [splitMode] to control how the splitting occurs
     */
    @JvmOverloads
    fun append(data: Any?, splitMode: SplitMode = SplitMode.WHOLE_LINE) {
        check(!built) { "Cannot append to a message that's already been built" }
        val string = data.toString()
        when (splitMode) {
            SplitMode.WHOLE_LINE -> {
                check(string.length <= maxCharactersPerMessage) { "Cannot append $string to message as its length is greater than the maximum allowed per message. Use SplitMode.BREAK_LINE instead" }
                if (buffer.length + string.length > maxCharactersPerMessage) {
                    splitMessage()
                }
                buffer += string
            }

            SplitMode.BREAK_LINE -> {
                val remainingCharacters = maxCharactersPerMessage - buffer.length
                if (remainingCharacters < string.length) {
                    val first = string.substring(0 until remainingCharacters)
                    val second =
                        string.substring(remainingCharacters until string.length).trimStart()
                    buffer += first
                    splitMessage()
                    buffer += if (second.startsWith("\n"))
                        second.substring(1)
                    else
                        second
                }
            }

            SplitMode.BREAK_WORD -> {
                // Break at the closest word
                val parts = string.split(" ")
                parts.forEach { word ->
                    check(word.length <= maxCharactersPerMessage) { "Cannot append $word to message, as its length is greater than the maximum allowed per message" }
                    buffer += if (maxCharactersPerMessage - buffer.length - word.length >= 0) {
                        " $word"
                    } else {
                        splitMessage()
                        word
                    }
                }
            }
        }
    }

    /**
     * Appends [data] with a newline character.
     *
     * @see append
     */
    @JvmOverloads
    fun appendLine(data: Any? = null, splitMode: SplitMode = SplitMode.WHOLE_LINE) {
        val string = data.toString()
        append("$string\n", splitMode)
    }

    fun build(): List<MessageCreateData> {
        if (buffer.isNotBlank()) {
            messages.add(buffer)
        }
        return messages.map {
            message {
                content = it
            }.create()
        }
    }

    private fun splitMessage() {
        messages.add(buffer)
        this.buffer = ""
    }
}

enum class SplitMode {
    WHOLE_LINE,
    BREAK_LINE,
    BREAK_WORD
}