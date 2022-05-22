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


@DslMarker
annotation class ActionRowDsl

enum class Type {
    MESSAGE,
    MODAL
}

@ActionRowDsl
class ActionRowBuilder(
    val type: Type = Type.MESSAGE
) : Builder<ActionRow> {

    val components =
        mutableMapOf<Class<out ItemComponent>, MutableList<Builder<out ItemComponent>>>()

    inline fun button(id: String? = null, builder: ButtonBuilder.() -> Unit): String {
        if(type != Type.MESSAGE)
            throw IllegalArgumentException("Can only use buttons in messages")
        validateComponentType(Button::class.java)
        val buttonId = id ?: UUID.randomUUID().toString()
        components.computeIfAbsent(Button::class.java) {
            mutableListOf()
        }.add(ButtonBuilder(buttonId).apply(builder))
        return buttonId
    }

    inline fun select(id: String? = null, builder: SelectMenuBuilder.() -> Unit): String {
        if(type != Type.MESSAGE)
            throw IllegalArgumentException("Can only use selects in messages")
        validateComponentType(SelectMenu::class.java)
        val selectId = id ?: UUID.randomUUID().toString()
        components.computeIfAbsent(SelectMenu::class.java) {
            mutableListOf()
        }.add(SelectMenuBuilder(selectId).apply(builder))
        return selectId
    }

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

    fun validateComponentType(type: Class<out ItemComponent>) {
        if (components.isEmpty())
            return
        // Enforce a single component type in the action row
        if (type in components.keys)
            return
        throw IllegalArgumentException("Action rows can only consist of a single type of component")
    }
}


@ActionRowDsl
class ButtonBuilder(
    private val id: String
) : Builder<Button> {
    var text = ""
    var style = ButtonStyle.SECONDARY
    var emoji: Emoji? = null
    var enabled = true
    var url: String? = null

    override fun build(): Button = Button.of(style, url ?: id, text, emoji).withDisabled(!enabled)

}


@ActionRowDsl
class SelectMenuBuilder(
    private val id: String
) : Builder<SelectMenu> {
    var options = mutableListOf<SelectOptionBuilder>()
    var min = 1
    var max = 1
    var placeholder: String? = null
    var enabled = true

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

@ActionRowDsl
class SelectOptionBuilder(
    val id: String
) : Builder<SelectOption> {
    var default = false
    var description: String? = null
    var value = ""
    var icon: Emoji? = null
    override fun build(): SelectOption =
        SelectOption.of(id, value).withDescription(description).withDefault(default).withEmoji(icon)
}

@ActionRowDsl
class TextInputBuilder(
    val id: String
) : Builder<TextInput> {

    var style = TextInputStyle.SHORT
    var name = ""
    var placeholder: String? = null
    var value: String? = null
    var min = 0
    var max = 4000
    var required = false

    override fun build(): TextInput {
        return TextInput.create(id, name, style).setPlaceholder(placeholder).setValue(value)
            .setRequiredRange(min, max).setRequired(required).build()
    }
}