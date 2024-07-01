package com.mrkirby153.botcore

import com.mrkirby153.botcore.builder.ButtonBuilder
import com.mrkirby153.botcore.builder.MessageBuilder
import com.mrkirby153.botcore.command.slashcommand.dsl.SlashContext
import com.mrkirby153.botcore.coroutine.await
import com.mrkirby153.botcore.utils.SLF4J
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.suspendCancellableCoroutine
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.dv8tion.jda.api.interactions.InteractionHook
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle
import java.util.concurrent.atomic.AtomicLong
import kotlin.coroutines.resume

object ConfirmationHandler : ListenerAdapter() {

    private val log by SLF4J
    private var nextId = AtomicLong(1)

    private val interactions = mutableMapOf<Long, QueuedConfirmation>()
    private val componentsToInteractions = mutableMapOf<String, Long>()


    override fun onButtonInteraction(event: ButtonInteractionEvent) {
        val id = event.componentId
        val interactionId = componentsToInteractions[id] ?: return
        log.debug("Processing interaction $interactionId")
        val interaction = interactions.remove(interactionId) ?: return
        try {
            if (id == interaction.yes) {
                log.debug("$interactionId confirmed")
                event.deferReply(interaction.ephemeral).queue()
                interaction.callback.onSuccess(event.hook)
            } else if (id == interaction.no) {
                log.debug("$interactionId rejected")
                event.deferReply(interaction.ephemeral).queue()
                interaction.callback.onFail(event.hook)
            }
        } finally {
            componentsToInteractions.remove(interaction.yes)
            componentsToInteractions.remove(interaction.no)
        }
    }

    internal fun enqueue(
        callback: Callback,
        yes: String,
        no: String,
        allowedUsers: List<User>,
        ephemeral: Boolean
    ): Long {
        val id = nextId.getAndIncrement()
        log.debug("Enqueueing a callback with Y:$yes, N:$no as ID $id")
        interactions[id] =
            QueuedConfirmation(callback, yes, no, allowedUsers.map { it.idLong }, ephemeral)
        componentsToInteractions[yes] = id
        componentsToInteractions[no] = id
        return id
    }

    internal fun dequeue(id: Long) {
        log.debug("De-queueing a callback with id $id")
        val queued = interactions.remove(id)
        if (queued != null) {
            componentsToInteractions.remove(queued.yes)
            componentsToInteractions.remove(queued.no)
        }
    }
}

private class ConfirmationCallback(
    private val continuation: CancellableContinuation<Pair<InteractionHook, Boolean>>
) : Callback {
    override fun onSuccess(hook: InteractionHook) {
        continuation.resume(Pair(hook, true))
    }

    override fun onFail(hook: InteractionHook) {
        continuation.resume(Pair(hook, false))
    }

}

/**
 * Confirms an interaction with two buttons, a "yes" button and a "no" button. Specify [yesButton] or
 * [noButton] to determine what the respective buttons look like. Specify [allowedUsers] to dictate
 * who can interact with the button.
 */
suspend fun SlashContext.confirm(
    ephemeral: Boolean = false,
    yesButton: (ButtonBuilder.() -> Unit)? = null,
    noButton: (ButtonBuilder.() -> Unit)? = null,
    allowedUsers: List<User> = listOf(user),
    builder: MessageBuilder.() -> Unit,
): Pair<InteractionHook, Boolean> {
    if (!ephemeral)
        check(allowedUsers.isNotEmpty()) { "At least one allowedUser must be specified with a non-ephemeral message" }

    val message = MessageBuilder().apply(builder)
    var buttons: Pair<String, String>
    message.apply {
        buttons = buildRow(yesButton, noButton)
    }
    reply(message.create()).setEphemeral(ephemeral).await()
    return suspendCancellableCoroutine { continuation ->
        val id = ConfirmationHandler.enqueue(
            ConfirmationCallback(continuation),
            buttons.first,
            buttons.second,
            allowedUsers,
            ephemeral
        )
        continuation.invokeOnCancellation { ConfirmationHandler.dequeue(id) }
    }

}

/**
 * Confirms an interaction with two buttons, a "yes" button and a "no" button. Specify [yesButton] or
 * [noButton] to determine what the respective buttons look like. Specify [allowedUser] to dictate
 * who can interact with the button.
 */
suspend fun InteractionHook.confirm(
    allowedUser: User,
    ephemeral: Boolean = false,
    yesButton: (ButtonBuilder.() -> Unit)? = null,
    noButton: (ButtonBuilder.() -> Unit)? = null,
    builder: MessageBuilder.() -> Unit
) = confirm(listOf(allowedUser), ephemeral, yesButton, noButton, builder)

/**
 * Confirms an interaction with two buttons, a "yes" button and a "no" button. Specify [yesButton] or
 * [noButton] to determine what the respective buttons look like. Specify [allowedUsers] to dictate
 * who can interact with the button.
 */
suspend fun InteractionHook.confirm(
    allowedUsers: List<User>,
    ephemeral: Boolean = false,
    yesButton: (ButtonBuilder.() -> Unit)? = null,
    noButton: (ButtonBuilder.() -> Unit)? = null,
    builder: MessageBuilder.() -> Unit
): Pair<InteractionHook, Boolean> {
    val message = MessageBuilder().apply(builder)
    var buttons: Pair<String, String>
    message.apply {
        buttons = buildRow(yesButton, noButton)
    }
    editOriginal(message.edit()).await()
    return suspendCancellableCoroutine { continuation ->
        val id = ConfirmationHandler.enqueue(
            ConfirmationCallback(continuation),
            buttons.first,
            buttons.second,
            allowedUsers,
            ephemeral
        )
        continuation.invokeOnCancellation { ConfirmationHandler.dequeue(id) }
    }
}


private fun MessageBuilder.buildRow(
    yesButton: (ButtonBuilder.() -> Unit)?,
    noButton: (ButtonBuilder.() -> Unit)?
): Pair<String, String> {
    var yesId: String? = null
    var noId: String? = null
    actionRow {
        yesId = button {
            if (yesButton != null) {
                yesButton.invoke(this)
            } else {
                text = "Yes"
                style = ButtonStyle.SUCCESS
            }
        }
        noId = button {
            if (noButton != null) {
                noButton.invoke(this)
            } else {
                text = "No"
                style = ButtonStyle.DANGER
            }
        }
    }
    checkNotNull(yesId) { "Yes button was not invoked" }
    checkNotNull(noId) { "No button was not invoked" }
    return Pair(yesId!!, noId!!)
}

internal interface Callback {
    fun onSuccess(hook: InteractionHook)
    fun onFail(hook: InteractionHook)
}

private data class QueuedConfirmation(
    val callback: Callback,
    val yes: String,
    val no: String,
    val allowedUsers: List<Long>,
    val ephemeral: Boolean
)