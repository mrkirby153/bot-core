package com.mrkirby153.botcore.coroutine

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.launchIn
import net.dv8tion.jda.api.events.GenericEvent
import kotlin.coroutines.EmptyCoroutineContext


private val sharedFlow = MutableSharedFlow<GenericEvent>(replay = 100)

object FlowEventListener : CoroutineEventListener {
    override suspend fun onEvent(event: GenericEvent) {
        sharedFlow.emit(event)
    }
}

/**
 * Flow for events
 */
val events: Flow<GenericEvent>
    get() {
        check(didEnableCoroutines) { "Coroutines are not enabled" }
        return sharedFlow
    }

private object DefaultEventListenerScope :
    CoroutineScope by CoroutineScope(Dispatchers.Default + SupervisorJob() + EmptyCoroutineContext)

/**
 * Launches the flow in a coroutine scope shared by all event handlers
 */
fun Flow<GenericEvent>.listen(scope: CoroutineScope = DefaultEventListenerScope) =
    this.launchIn(scope)