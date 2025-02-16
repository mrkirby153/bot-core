package com.mrkirby153.botcore.emoji


/**
 * Delegate used to map an emoji to an [Emoji]
 */
class EmojiDelegate(
    private val name: String,
) {
    operator fun getValue(thisRef: Any?, property: Any?): Emoji {
        check(thisRef is Emojis) { "EmojiDelegate can only be used in Emojis" }
        val emoji = thisRef.getEmoji(name)
        checkNotNull(emoji) { "Emoji $name not found" }
        return emoji
    }
}