package com.mrkirby153.botcore.coroutine

import kotlinx.coroutines.suspendCancellableCoroutine
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.events.GenericEvent
import net.dv8tion.jda.api.hooks.EventListener
import net.dv8tion.jda.api.hooks.SubscribeEvent
import net.dv8tion.jda.api.sharding.ShardManager
import kotlin.coroutines.resume

/**
 * Waits for the given event [T] that matches the [filter]
 */
suspend inline fun <reified T : GenericEvent> ShardManager.await(crossinline filter: (T) -> Boolean = { true }) =
    suspendCancellableCoroutine {
        val listener = object : EventListener {
            @SubscribeEvent
            override fun onEvent(event: GenericEvent) {
                if (event is T && filter(event)) {
                    removeEventListener(this)
                    it.resume(event)
                }
            }
        }
        addEventListener(listener)
        it.invokeOnCancellation {
            removeEventListener(listener)
        }
    }

/**
 * Waits for the given event [T] that matches the [filter]
 */
suspend inline fun <reified T : GenericEvent> JDA.await(crossinline filter: (T) -> Boolean = { true }) =
    suspendCancellableCoroutine {
        val listener = object : EventListener {
            @SubscribeEvent
            override fun onEvent(event: GenericEvent) {
                if (event is T && filter(event)) {
                    removeEventListener(this)
                    it.resume(event)
                }
            }
        }
        addEventListener(listener)
        it.invokeOnCancellation {
            removeEventListener(listener)
        }
    }