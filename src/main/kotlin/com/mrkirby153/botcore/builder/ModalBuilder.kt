package com.mrkirby153.botcore.builder

import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent
import net.dv8tion.jda.api.interactions.components.Modal
import java.util.UUID
import java.util.function.Consumer


/**
 * Marker for the Modal DSL
 */
@DslMarker
annotation class ModalDsl

/**
 * Builder for a [Modal]
 *
 * @param id The id of the modal. If left blank, a random UUID will be generated
 */
@ModalDsl
class ModalBuilder(
    id: String? = null
) : Builder<Modal> {

    /**
     * The modal's ID
     */
    val id = id ?: UUID.randomUUID().toString()

    /**
     * The title of the modal
     */
    var title = ""

    /**
     * The action ran when the modal is submitted
     */
    var onSubmit: ((ModalInteractionEvent) -> Unit) = { _ -> }

    /**
     * The modal's action rows
     */
    val actionRows = mutableListOf<ActionRowBuilder>()

    /**
     * Adds a text input with the given [id] to the modal
     */
    inline fun textInput(id: String? = null, builder: TextInputBuilder.() -> Unit) {
        actionRows.add(ActionRowBuilder(Type.MODAL).apply {
            this.textInput(id, builder)
        })
    }

    /**
     * Adds a text input with the given [id] to the modal
     *
     * _This method exists for Java compatibility_
     */
    @JvmOverloads
    fun textInput(id: String? = null, builder: Consumer<TextInputBuilder>) {
        textInput(id) {
            builder.accept(this)
        }
    }

    /**
     * Action called when the modal is submitted
     */
    fun onSubmit(event: (ModalInteractionEvent) -> Unit) {
        this.onSubmit = event
    }

    /**
     * Action called when the modal is submitted
     *
     * _This method exists for Java compatibility_
     */
    fun onSubmit(event: Consumer<ModalInteractionEvent>) {
        this.onSubmit = { it -> event.accept(it) }
    }

    override fun build(): Modal {
        return Modal.create(id, title).addActionRows(actionRows.map { it.build() }).build()
    }
}