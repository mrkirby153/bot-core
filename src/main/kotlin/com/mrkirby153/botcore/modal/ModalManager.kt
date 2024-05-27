package com.mrkirby153.botcore.modal

import com.mrkirby153.botcore.builder.ModalBuilder
import com.mrkirby153.botcore.command.slashcommand.dsl.SlashContext
import com.mrkirby153.botcore.coroutine.await
import com.mrkirby153.botcore.modal.ModalManager.RegisteredModal
import kotlinx.coroutines.suspendCancellableCoroutine
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.dv8tion.jda.api.interactions.modals.Modal
import net.dv8tion.jda.api.sharding.ShardManager
import okhttp3.internal.toImmutableMap
import java.util.UUID
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.ScheduledThreadPoolExecutor
import java.util.concurrent.ThreadFactory
import java.util.concurrent.TimeUnit
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException


interface ModalCallback {
    fun onCompleted(event: ModalInteractionEvent)

    fun onTimeout()
}

/**
 * Manager for interaction modals. The manager must be registered with a [JDA] or [ShardManager]
 * instance to function correctly
 *
 * @param threadFactory The factory to use when spawning the GC pool
 * @param gcPeriod The period over which the manager garbage collects
 * @param gcUnits The time unit of the garbage collection period
 */
class ModalManager(
    threadFactory: ThreadFactory? = null,
    gcPeriod: Long = 1,
    gcUnits: TimeUnit = TimeUnit.SECONDS
) : ListenerAdapter() {

    private val cleanupThreadPool =
        if (threadFactory != null)
            ScheduledThreadPoolExecutor(1, threadFactory)
        else
            ScheduledThreadPoolExecutor(1, ThreadFactory {
                Thread(it).apply {
                    name = "ModalCleanupThread"
                    isDaemon = true
                }
            })

    init {
        cleanupThreadPool.scheduleAtFixedRate({ garbageCollect() }, 0, gcPeriod, gcUnits)
    }

    @PublishedApi
    internal val registeredModals = CopyOnWriteArrayList<RegisteredModal>()

    /**
     * Registers the given [modal] with the manager. The modal will automatically be garbage
     * collected and will no longer respond after [timeout]. Specify the units using [timeUnit].
     *
     * The default garbage collection timeout is 5 minutes
     */
    @JvmOverloads
    fun register(modal: ModalBuilder, timeout: Long = 5, timeUnit: TimeUnit = TimeUnit.MINUTES) {
        val timeoutMs =
            if (timeout == -1L) timeout else TimeUnit.MILLISECONDS.convert(timeout, timeUnit)
        if (registeredModals.firstOrNull { it.id == modal.id } != null)
            throw IllegalArgumentException("Cannot register the same modal twice")

        val callback = object : ModalCallback {
            override fun onCompleted(event: ModalInteractionEvent) {
                modal.onSubmit(event)
            }

            override fun onTimeout() {
                // Do nothing
            }
        }
        registeredModals.add(
            RegisteredModal(
                modal.id,
                timeout == -1L,
                callback, System.currentTimeMillis() + timeoutMs
            )
        )
    }

    /**
     * Builds a modal
     */
    fun build(
        builder: ModalBuilder.() -> Unit
    ): Modal {
        val modalBuilder = ModalBuilder(UUID.randomUUID().toString()).apply(builder)
        return modalBuilder.build()
    }

    override fun onModalInteraction(event: ModalInteractionEvent) {
        val toExecute = registeredModals.firstOrNull { it.id == event.modalId } ?: return
        toExecute.callback.onCompleted(event)
        if (!toExecute.permanent)
            registeredModals.remove(toExecute)
    }

    private fun garbageCollect() {
        val toRemove = registeredModals.filter {
            if (it.timeout == -1L)
                return@filter false
            it.timeout < System.currentTimeMillis()
        }
        toRemove.forEach {
            it.callback.onTimeout()
        }
        registeredModals.removeAll(toRemove.toSet())
    }

    data class RegisteredModal(
        val id: String,
        val permanent: Boolean,
        val callback: ModalCallback,
        val timeout: Long
    )
}

class ModalResult(event: ModalInteractionEvent, val data: Map<String, String>) :
    ModalInteractionEvent(event.jda, event.responseNumber, event.interaction)


context(SlashContext)
suspend fun ModalManager.await(
    timeout: Long = 5,
    timeUnit: TimeUnit = TimeUnit.MINUTES,
    builder: ModalBuilder.() -> Unit
): ModalResult {
    val modal = ModalBuilder(UUID.randomUUID().toString()).apply(builder).build()
    replyModal(modal).await()
    return await(modal, timeout, timeUnit)
}

@PublishedApi
internal suspend fun ModalManager.await(
    modal: Modal,
    timeout: Long = 5,
    timeUnit: TimeUnit = TimeUnit.MINUTES
) =
    suspendCancellableCoroutine { continuation ->
        val timeoutMs =
            if (timeout == -1L) timeout else TimeUnit.MILLISECONDS.convert(timeout, timeUnit)
        if (registeredModals.firstOrNull { it.id == modal.id } != null)
            throw IllegalArgumentException("Cannot register the same modal twice")

        val callback: ModalCallback = object : ModalCallback {
            override fun onCompleted(event: ModalInteractionEvent) {
                val values = mutableMapOf<String, String>()
                event.values.forEach { value ->
                    values[value.id] = value.asString
                }
                continuation.resume(ModalResult(event, values.toImmutableMap()))
            }

            override fun onTimeout() {
                continuation.resumeWithException(InterruptedException())
            }

        }
        registeredModals.add(
            RegisteredModal(
                modal.id,
                timeout == -1L,
                callback,
                System.currentTimeMillis() + timeoutMs
            )
        )
    }