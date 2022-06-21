package com.mrkirby153.botcore.builder

import net.dv8tion.jda.api.entities.Emoji
import net.dv8tion.jda.api.interactions.components.ActionRow
import net.dv8tion.jda.api.interactions.components.ItemComponent
import net.dv8tion.jda.api.interactions.components.buttons.Button
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle
import net.dv8tion.jda.api.interactions.components.selections.SelectMenu
import net.dv8tion.jda.api.interactions.components.selections.SelectOption
import net.dv8tion.jda.api.interactions.components.text.TextInput
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle
import java.util.UUID


/**
 * Marker annotation for action rows
 */
@DslMarker
annotation class ActionRowDsl

/**
 * The type of action row that is being built
 */
enum class Type {
    /**
     * A message action row. All components are supported
     */
    MESSAGE,

    /**
     * A modal action row. Only a subset of components are supported
     */
    MODAL
}

/**
 * Builder for action rows
 *
 * @param type The type of the action row
 */
@ActionRowDsl
class ActionRowBuilder(
    val type: Type = Type.MESSAGE
) : Builder<ActionRow> {

    /**
     * The list of components in this action row
     */
    val components =
        mutableMapOf<Class<out ItemComponent>, MutableList<Builder<out ItemComponent>>>()

    /**
     * Adds a [Button] to the action row.
     *
     * _This component can only be used in messages_
     *
     * If [id] is provided, it will be used. Otherwise, a random UUID will be generated instead
     */
    inline fun button(id: String? = null, builder: ButtonBuilder.() -> Unit): String {
        if (type != Type.MESSAGE)
            throw IllegalArgumentException("Can only use buttons in messages")
        validateComponentType(Button::class.java)
        val buttonId = id ?: UUID.randomUUID().toString()
        components.computeIfAbsent(Button::class.java) {
            mutableListOf()
        }.add(ButtonBuilder(buttonId).apply(builder))
        return buttonId
    }

    /**
     * Adds a [SelectMenu] to the action row.
     *
     * _This component can only be used in messages_
     *
     * If [id] is provided, it will be used. Otherwise, a random UUID will be generated instead
     */
    inline fun select(id: String? = null, builder: SelectMenuBuilder.() -> Unit): String {
        if (type != Type.MESSAGE)
            throw IllegalArgumentException("Can only use selects in messages")
        validateComponentType(SelectMenu::class.java)
        val selectId = id ?: UUID.randomUUID().toString()
        components.computeIfAbsent(SelectMenu::class.java) {
            mutableListOf()
        }.add(SelectMenuBuilder(selectId).apply(builder))
        return selectId
    }

    /**
     * Adds a [TextInput] to the action row.
     *
     * _This component can only be used in modals_
     *
     * If [id] is provided, it will be used. Otherwise, a random UUID will be generated instead
     */
    inline fun textInput(id: String? = null, builder: TextInputBuilder.() -> Unit): String {
        if (type != Type.MODAL)
            throw IllegalArgumentException("Can only use text input components in modals")
        validateComponentType(TextInput::class.java)
        val textId = id ?: UUID.randomUUID().toString()
        components.computeIfAbsent(TextInput::class.java) {
            mutableListOf()
        }.add(TextInputBuilder(textId).apply(builder))
        return textId
    }

    override fun build(): ActionRow =
        ActionRow.of(components.flatMap { it.value }.map { it.build() })

    /**
     * Ensures that the provided component is able to be added into the action row. Action rows can
     * only consist of one component type
     */
    @kotlin.jvm.Throws(IllegalArgumentException::class)
    fun validateComponentType(type: Class<out ItemComponent>) {
        if (components.isEmpty())
            return
        // Enforce a single component type in the action row
        if (type in components.keys)
            return
        throw IllegalArgumentException("Action rows can only consist of a single type of component")
    }
}


/**
 * Builder for a [Button]
 *
 * @param id The id of the button
 */
@ActionRowDsl
class ButtonBuilder(
    private val id: String
) : Builder<Button> {
    /**
     * The text displayed on the button
     */
    var text = ""

    /**
     * The style of the button
     */
    var style = ButtonStyle.SECONDARY

    /**
     * The emoji to show on the  button
     */
    var emoji: Emoji? = null

    /**
     * If this button should be enabled
     */
    var enabled = true

    /**
     * The URL this button will navigate to when clicked
     */
    var url: String? = null

    override fun build(): Button = Button.of(style, url ?: id, text, emoji).withDisabled(!enabled)

}

/**
 * A [SelectMenu] builder
 *
 * @param id The ID of the select menu
 */
@ActionRowDsl
class SelectMenuBuilder(
    private val id: String
) : Builder<SelectMenu> {

    /**
     * A list of options in the select menu
     *
     * @see SelectMenuBuilder
     */
    var options = mutableListOf<SelectOptionBuilder>()

    /**
     * The minimum number of selectable items
     */
    var min = 1

    /**
     * The maximum number of selectable items
     */
    var max = 1

    /**
     * A placeholder to display if no item is selected
     */
    var placeholder: String? = null

    /**
     * If the select menu should be enabled
     */
    var enabled = true

    /**
     * Adds an option with the provided [id] to the select menu
     */
    inline fun option(id: String, builder: SelectOptionBuilder.() -> Unit) {
        options.add(SelectOptionBuilder(id).apply(builder))
    }

    override fun build(): SelectMenu {
        if (min > max) {
            throw IllegalArgumentException("Min cannot be greater than max")
        }
        return SelectMenu.create(id).setRequiredRange(min, max).setPlaceholder(placeholder)
            .setDisabled(!enabled)
            .addOptions(options.map { it.build() }).build()
    }
}

/**
 * A [SelectOption] builder
 *
 * @param id The ID of the select option
 */
@ActionRowDsl
class SelectOptionBuilder(
    val id: String
) : Builder<SelectOption> {

    /**
     * If this select option should be default
     */
    var default = false

    /**
     * The description of the option
     */
    var description: String? = null

    /**
     * The displayed text of the option
     */
    var value = ""

    /**
     * The icon of the option
     */
    var icon: Emoji? = null
    override fun build(): SelectOption =
        SelectOption.of(value, id).withDescription(description).withDefault(default).withEmoji(icon)
}

/**
 * A [TextInput] builder
 *
 * @param id The ID of the builder
 */
@ActionRowDsl
class TextInputBuilder(
    val id: String
) : Builder<TextInput> {

    /**
     * The Style of the text input
     *
     * @see TextInputStyle
     */
    var style = TextInputStyle.SHORT

    /**
     * The name of the text input
     */
    var name = ""

    /**
     * The placeholder of the text input, displayed if there is nothing in the text box
     */
    var placeholder: String? = null

    /**
     * The value of the text input
     */
    var value: String? = null

    /**
     * The minimum number of characters in the text input
     */
    var min = 0
        set(value) {
            if (value < 0) {
                throw IllegalArgumentException("Minimum cannot be less than 0")
            }
            field = value
        }

    /**
     * The maximum number of characters in the text input
     */
    var max = 4000
        set(value) {
            if (value > 4000) {
                throw IllegalArgumentException("Maximum cannot be greater than 4000")
            }
            if (value < min) {
                throw IllegalArgumentException("Maximum cannot be less than min")
            }
            field = value
        }

    /**
     * If the text input should be required
     */
    var required = false

    override fun build(): TextInput {
        return TextInput.create(id, name, style).setPlaceholder(placeholder).setValue(value)
            .setRequiredRange(min, max).setRequired(required).build()
    }
}