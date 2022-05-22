package com.mrkirby153.botcore.builder

import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent
import net.dv8tion.jda.api.interactions.components.Modal


@DslMarker
annotation class ModalDsl

@ModalDsl
class ModalBuilder(
    val id: String
) : Builder<Modal> {

    var title = ""
    var onSubmit: ((ModalInteractionEvent) -> Unit) = { _ -> }
    val actionRows = mutableListOf<ActionRowBuilder>()

    inline fun textInput(id: String? = null, builder: TextInputBuilder.() -> Unit) {
        actionRows.add(ActionRowBuilder(Type.MODAL).apply {
            this.textInput(id, builder)
        })
    }

    fun onSubmit(event: (ModalInteractionEvent) -> Unit) {
        this.onSubmit = event
    }

    override fun build(): Modal {
        return Modal.create(id, title).addActionRows(actionRows.map { it.build() }).build()
    }
}