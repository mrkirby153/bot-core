package com.mrkirby153.botcore.builder

import com.mrkirby153.botcore.builder.componentsv2.ActionRowBuilder
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent
import net.dv8tion.jda.api.interactions.modals.Modal
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
    private var onSubmit: ((ModalInteractionEvent) -> Unit) = { _ -> }

    /**
     * The modal's action rows
     */
    private val actionRows = mutableListOf<ActionRowBuilder>()

    /**
     * Adds an ActionRow to this modal
     */
    fun actionRow(id: Int? = null, builder: ActionRowBuilder.() -> Unit) {
        val row = ActionRowBuilder(id, true)
        row.apply(builder)
        actionRows.add(row)
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
        return Modal.create(id, title).addComponents(actionRows.map { it.build() }).build()
    }

    operator fun invoke(event: ModalInteractionEvent) {
        this.onSubmit(event)
    }
}