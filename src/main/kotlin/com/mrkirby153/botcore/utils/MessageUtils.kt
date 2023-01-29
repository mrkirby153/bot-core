package com.mrkirby153.botcore.utils

import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel
import net.dv8tion.jda.api.utils.messages.MessageCreateData
import net.dv8tion.jda.api.utils.messages.MessageEditData
import java.util.concurrent.CompletableFuture

/**
 * Sends a collection of [MessageCreateData] to the given [channel]. Returns a [CompletableFuture]
 * completed when all messages are sent
 */
fun List<MessageCreateData>.send(channel: MessageChannel): CompletableFuture<Void> {
    return CompletableFuture.allOf(*this.map {
        channel.sendMessage(it).submit()
    }.toTypedArray())
}

/**
 * Sends this message to the given [channel]
 */
fun MessageCreateData.send(channel: MessageChannel) = channel.sendMessage(this)

/**
 * Edits the given [message] with this [MessageEditData]
 */
fun MessageEditData.edit(message: Message) = message.editMessage(this)