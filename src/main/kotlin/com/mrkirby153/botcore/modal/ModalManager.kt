package com.mrkirby153.botcore.modal

import com.mrkirby153.botcore.builder.ModalBuilder
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.dv8tion.jda.api.interactions.components.Modal
import java.util.UUID
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.ScheduledThreadPoolExecutor
import java.util.concurrent.ThreadFactory
import java.util.concurrent.TimeUnit

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

    companion object {
        @JvmStatic
        var instance: ModalManager? = null

        fun build(
            timeout: Long = 5,
            timeUnit: TimeUnit = TimeUnit.MINUTES,
            builder: ModalBuilder.() -> Unit
        ): Modal {
            if (instance == null)
                instance = ModalManager()
            return instance!!.build(timeout, timeUnit, builder)
        }
    }

    private val registeredModals = CopyOnWriteArrayList<RegisteredModal>()

    fun register(modal: ModalBuilder, timeout: Long = 5, timeUnit: TimeUnit = TimeUnit.MINUTES) {
        val timeoutMs =
            if (timeout == -1L) timeout else TimeUnit.MILLISECONDS.convert(timeout, timeUnit)
        if (registeredModals.firstOrNull { it.id == modal.id } != null)
            throw IllegalArgumentException("Cannot register the same modal twice")
        registeredModals.add(
            RegisteredModal(
                modal.id,
                timeout == -1L,
                modal.onSubmit,
                System.currentTimeMillis() + timeoutMs
            )
        )
    }

    fun build(
        timeout: Long = 5,
        timeUnit: TimeUnit = TimeUnit.MINUTES,
        builder: ModalBuilder.() -> Unit
    ): Modal {
        val modalBuilder = ModalBuilder(UUID.randomUUID().toString()).apply(builder)
        register(modalBuilder, timeout, timeUnit)
        return modalBuilder.build()
    }

    override fun onModalInteraction(event: ModalInteractionEvent) {
        val toExecute = registeredModals.firstOrNull { it.id == event.modalId } ?: return
        toExecute.callback.invoke(event)
        if (!toExecute.permanent)
            registeredModals.remove(toExecute)
    }

    private fun garbageCollect() {
        registeredModals.removeIf {
            if (it.timeout == -1L)
                return@removeIf false
            it.timeout < System.currentTimeMillis()
        }
    }

    private data class RegisteredModal(
        val id: String,
        val permanent: Boolean,
        val callback: (ModalInteractionEvent) -> Unit,
        val timeout: Long
    )
}