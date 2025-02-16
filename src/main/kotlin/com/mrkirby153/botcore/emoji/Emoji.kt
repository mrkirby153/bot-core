package com.mrkirby153.botcore.emoji

import net.dv8tion.jda.api.entities.emoji.ApplicationEmoji

/**
 * A simple wrapper around [ApplicationEmoji] whose [toString] method returns the emoji as a mention
 */
class Emoji(applicationEmoji: ApplicationEmoji) : ApplicationEmoji by applicationEmoji {

    /**
     * Returns this emoji as a mention
     */
    override fun toString(): String {
        return asMention
    }
}