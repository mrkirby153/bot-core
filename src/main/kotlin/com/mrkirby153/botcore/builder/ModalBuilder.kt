package com.mrkirby153.botcore.builder

import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent
import net.dv8tion.jda.api.interactions.components.Modal
import java.util.UUID
import java.util.function.Consumer


@DslMarker
annotation class ModalDsl

@ModalDsl
class ModalBuilder(
    id: String? = null
) : Builder<Modal> {

    val id = id ?: UUID.randomUUID().toString()

    var title = ""
    var onSubmit: ((ModalInteractionEvent) -> Unit) = { _ -> }
    val actionRows = mutableListOf<ActionRowBuilder>()

    inline fun textInput(id: String? = null, builder: TextInputBuilder.() -> Unit) {
        actionRows.add(ActionRowBuilder(Type.MODAL).apply {
            this.textInput(id, builder)
        })
    }

    @JvmOverloads
    fun textInput(id: String? = null, builder: Consumer<TextInputBuilder>) {
        textInput(id) {
            builder.accept(this)
        }
    }

    fun onSubmit(event: (ModalInteractionEvent) -> Unit) {
        this.onSubmit = event
    }

    fun onSubmit(event: Consumer<ModalInteractionEvent>) {
        this.onSubmit = { it -> event.accept(it) }
    }

    override fun build(): Modal {
        return Modal.create(id, title).addActionRows(actionRows.map { it.build() }).build()
    }
}