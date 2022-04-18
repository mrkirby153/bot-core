package com.mrkirby153.botcore.builder

import net.dv8tion.jda.api.entities.Emoji
import net.dv8tion.jda.api.interactions.components.ActionRow
import net.dv8tion.jda.api.interactions.components.buttons.Button
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle
import net.dv8tion.jda.api.interactions.components.selections.SelectMenu
import net.dv8tion.jda.api.interactions.components.selections.SelectOption
import java.util.UUID


@DslMarker
annotation class ActionRowDsl

@ActionRowDsl
class ActionRowBuilder : Builder<ActionRow> {

    val buttons = mutableListOf<ButtonBuilder>()
    val selects = mutableListOf<SelectMenuBuilder>()

    inline fun button(id: String? = null, builder: ButtonBuilder.() -> Unit): String {
        if (selects.size > 0)
            throw IllegalArgumentException("Can't mix buttons and selects")
        val buttonId = id ?: UUID.randomUUID().toString()
        buttons.add(ButtonBuilder(buttonId).apply(builder))
        return buttonId
    }

    inline fun select(id: String? = null, builder: SelectMenuBuilder.() -> Unit): String {
        if (buttons.size > 0)
            throw IllegalArgumentException("Can't mix buttons and selects")
        val selectId = id ?: UUID.randomUUID().toString()
        selects.add(SelectMenuBuilder(selectId).apply(builder))
        return selectId
    }

    override fun build(): ActionRow = ActionRow.of(
        *buttons.map { it.build() }.toTypedArray(),
        *selects.map { it.build() }.toTypedArray()
    )
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
    var placeholder = ""
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
