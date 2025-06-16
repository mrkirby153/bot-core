package com.mrkirby153.botcore.builder.componentsv2

import com.mrkirby153.botcore.builder.Builder
import net.dv8tion.jda.api.components.Component
import net.dv8tion.jda.api.components.actionrow.ActionRow
import net.dv8tion.jda.api.components.buttons.Button
import net.dv8tion.jda.api.components.buttons.ButtonStyle
import net.dv8tion.jda.api.components.container.Container
import net.dv8tion.jda.api.components.container.ContainerChildComponent
import net.dv8tion.jda.api.components.filedisplay.FileDisplay
import net.dv8tion.jda.api.components.mediagallery.MediaGallery
import net.dv8tion.jda.api.components.mediagallery.MediaGalleryItem
import net.dv8tion.jda.api.components.section.Section
import net.dv8tion.jda.api.components.section.SectionAccessoryComponent
import net.dv8tion.jda.api.components.section.SectionContentComponent
import net.dv8tion.jda.api.components.selections.EntitySelectMenu
import net.dv8tion.jda.api.components.selections.SelectMenu
import net.dv8tion.jda.api.components.selections.SelectOption
import net.dv8tion.jda.api.components.selections.StringSelectMenu
import net.dv8tion.jda.api.components.separator.Separator
import net.dv8tion.jda.api.components.textdisplay.TextDisplay
import net.dv8tion.jda.api.components.textinput.TextInput
import net.dv8tion.jda.api.components.textinput.TextInputStyle
import net.dv8tion.jda.api.components.thumbnail.Thumbnail
import net.dv8tion.jda.api.entities.Role
import net.dv8tion.jda.api.entities.SkuSnowflake
import net.dv8tion.jda.api.entities.UserSnowflake
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel
import net.dv8tion.jda.api.entities.emoji.Emoji
import net.dv8tion.jda.api.utils.FileUpload
import java.util.UUID

@DslMarker
annotation class ComponentsV2Dsl

fun container(id: Int? = null, builder: ContainerBuilder.() -> Unit): Container =
    ContainerBuilder(id).apply(builder).buildWithId()

fun randomCustomId(): String {
    return UUID.randomUUID().toString()
}

/**
 * Base class for component builders
 */
abstract class BaseComponentBuilder<T : Component>(
    private val id: Int? = null,
) : Builder<T> {

    /**
     * Builds the component, optionally applying the unique ID if it is set
     */
    fun buildWithId(): T {
        return build().apply {
            if (id != null) {
                withUniqueId(id)
            }
        }
    }
}

@ComponentsV2Dsl
class ContainerBuilder(id: Int?) : BaseComponentBuilder<Container>(id) {

    private val components = mutableListOf<ContainerChildComponent>()

    /**
     * Adds a new [Section] to this container
     */
    fun section(id: Int? = null, builder: SectionBuilder.() -> Unit) {
        components.add(SectionBuilder(id).apply(builder).buildWithId())
    }

    fun actionRow(id: Int? = null, builder: ActionRowBuilder.() -> Unit) {
        components.add(ActionRowBuilder(id, false).apply(builder).buildWithId())
    }

    fun text(id: Int? = null, builder: TextComponentBuilder.() -> Unit) {
        components.add(TextComponentBuilder(id).apply(builder).buildWithId())
    }

    fun text(text: String, id: Int? = null) {
        components.add(TextComponentBuilder(id).apply { this.text(text) }.buildWithId())
    }

    fun mediaGallery(id: Int? = null, builder: MediaGalleryBuilder.() -> Unit) {
        components.add(MediaGalleryBuilder(id).apply(builder).buildWithId())
    }

    fun separator(id: Int? = null, builder: SeparatorBuilder.() -> Unit = {}) {
        components.add(SeparatorBuilder(id).apply(builder).buildWithId())
    }

    fun file(id: Int? = null, builder: FileBuilder.() -> Unit) {
        components.add(FileBuilder(id).apply(builder).buildWithId())
    }

    override fun build() = Container.of(components)
}

@ComponentsV2Dsl
class ActionRowBuilder(id: Int?, private val isModal: Boolean) :
    BaseComponentBuilder<ActionRow>(id) {

    private val components = mutableListOf<Button>()
    private var select: SelectMenu? = null
    private var textInput: TextInput? = null

    fun button(
        customId: String = randomCustomId(),
        id: Int? = null,
        builder: ButtonBuilder.() -> Unit
    ): String {
        check(select == null) { "ActionRow cannot contain both buttons and a select menu" }
        check(textInput == null) { "ActionRow cannot contain both buttons and a text input" }
        components.add(ButtonBuilder(customId, id).apply(builder).buildWithId())
        return customId
    }

    fun text(
        customId: String = randomCustomId(),
        label: String,
        id: Int? = null,
        builder: TextInputBuilder.() -> Unit = {}
    ): String {
        check(isModal) { "Text input can only be added to a modal ActionRow" }
        check(select == null) { "ActionRow cannot contain both text input and a select menu" }
        check(components.isEmpty()) { "ActionRow cannot contain both text input and buttons" }
        textInput = TextInputBuilder(label, customId, id).apply(builder).buildWithId()
        return customId
    }

    fun select(
        customId: String = randomCustomId(),
        id: Int? = null,
        builder: StringSelectBuilder.() -> Unit
    ): String {
        check(textInput == null) { "ActionRow cannot contain both a select menu and a text input" }
        check(components.isEmpty()) { "ActionRow cannot contain both a select menu and buttons" }
        select = StringSelectBuilder(id, customId).apply(builder).buildWithId()
        return customId
    }

    fun userSelect(
        customId: String = randomCustomId(),
        id: Int? = null,
        builder: EntitySelectMenuBuilder.() -> Unit
    ): String {
        check(textInput == null) { "ActionRow cannot contain both a select menu and a text input" }
        check(components.isEmpty()) { "ActionRow cannot contain both a select menu and buttons" }
        select =
            EntitySelectMenuBuilder(customId, id, EntitySelectMenu.SelectTarget.USER).apply(builder)
                .buildWithId()
        return customId
    }

    fun roleSelect(
        customId: String = randomCustomId(),
        id: Int? = null,
        builder: EntitySelectMenuBuilder.() -> Unit
    ): String {
        check(textInput == null) { "ActionRow cannot contain both a select menu and a text input" }
        check(components.isEmpty()) { "ActionRow cannot contain both a select menu and buttons" }
        select =
            EntitySelectMenuBuilder(customId, id, EntitySelectMenu.SelectTarget.ROLE).apply(builder)
                .buildWithId()
        return customId
    }

    fun mentionableSelect(
        customId: String = randomCustomId(),
        id: Int? = null,
        builder: EntitySelectMenuBuilder.() -> Unit
    ): String {
        check(textInput == null) { "ActionRow cannot contain both a select menu and a text input" }
        check(components.isEmpty()) { "ActionRow cannot contain both a select menu and buttons" }
        select =
            EntitySelectMenuBuilder(
                customId, id, EntitySelectMenu.SelectTarget.USER,
                EntitySelectMenu.SelectTarget.ROLE, EntitySelectMenu.SelectTarget.CHANNEL
            ).apply(builder)
                .buildWithId()
        return customId
    }

    fun channelSelect(
        customId: String = randomCustomId(),
        id: Int? = null,
        builder: EntitySelectMenuBuilder.() -> Unit
    ): String {
        check(textInput == null) { "ActionRow cannot contain both a select menu and a text input" }
        check(components.isEmpty()) { "ActionRow cannot contain both a select menu and buttons" }
        select =
            EntitySelectMenuBuilder(customId, id, EntitySelectMenu.SelectTarget.CHANNEL).apply(
                builder
            )
                .buildWithId()
        return customId
    }

    override fun build(): ActionRow {
        if (components.isEmpty()) {
            check(select != null || textInput != null) { "ActionRow must contain at least one component or select menu" }
        }

        return ActionRow.of(
            *listOfNotNull(select, textInput, *components.toTypedArray()).toTypedArray()
        )
    }
}

@ComponentsV2Dsl
class SectionBuilder(
    id: Int?
) : BaseComponentBuilder<Section>(id) {

    private var accessoryComponent: SectionAccessoryComponent? = null
    private var components = mutableListOf<SectionContentComponent>()

    fun thumbnail(id: Int? = null, builder: ThumbnailBuilder.() -> Unit) {
        check(accessoryComponent == null) { "Accessory component is already set" }
        accessoryComponent = ThumbnailBuilder(id).apply(builder).buildWithId()
    }

    fun button(customId: String, id: Int? = null, builder: ButtonBuilder.() -> Unit) {
        check(accessoryComponent == null) { "Accessory component is already set" }
        accessoryComponent = ButtonBuilder(customId, id).apply(builder).buildWithId()
    }

    fun text(id: Int? = null, builder: TextComponentBuilder.() -> Unit) {
        check(components.size < 3) { "Section can only have up to 3 text components" }
        components.add(TextComponentBuilder(id).apply(builder).buildWithId())
    }

    override fun build(): Section {
        val accessory = this.accessoryComponent
        checkNotNull(accessory) { "accessoryComponent was not set" }
        return Section.of(accessory, components)
    }
}

@ComponentsV2Dsl
class ThumbnailBuilder(
    id: Int?
) : BaseComponentBuilder<Thumbnail>(id) {

    private var fileUpload: FileUpload? = null
    private var url: String? = null
    private var description: String? = null
    private var spoiler: Boolean? = null

    fun spoiler(spoiler: Boolean = true) {
        this.spoiler = spoiler
    }

    fun description(description: String) {
        this.description = description
    }

    fun file(file: FileUpload) {
        check(url == null) { "URL is already set" }
        fileUpload = file
    }

    fun url(url: String) {
        check(fileUpload == null) { "File upload is already set" }
        this.url = url
    }

    fun description(builder: StringBuilder.() -> Unit) {
        description = StringBuilder().apply(builder).toString()
    }

    override fun build(): Thumbnail {
        check(fileUpload != null || url != null) { "Either file or URL must be set for Thumbnail" }
        var thumbnail = if (fileUpload != null) {
            Thumbnail.fromFile(fileUpload!!)
        } else {
            Thumbnail.fromUrl(url!!)
        }
        thumbnail = description?.let { thumbnail.withDescription(it) } ?: thumbnail
        thumbnail = spoiler?.let { thumbnail.withSpoiler(it) } ?: thumbnail
        return thumbnail
    }
}

@ComponentsV2Dsl
class ButtonBuilder(
    private val customId: String, id: Int?
) : BaseComponentBuilder<Button>(id) {

    private var style: ButtonStyle = ButtonStyle.PRIMARY
    private var label: String? = null
    private var disabled: Boolean = false
    private var url: String? = null
    private var sku: SkuSnowflake? = null

    fun style(style: ButtonStyle) {
        this.style = style
    }

    fun label(builder: StringBuilder.() -> Unit) {
        label = StringBuilder().apply(builder).toString()
    }

    fun label(label: String) {
        this.label = label
    }

    fun url(url: String) {
        this.url = url
    }

    fun disable(disabled: Boolean = true) {
        this.disabled = disabled
    }

    fun sku(id: String) {
        this.sku = SkuSnowflake.fromId(id)
    }

    fun sku(sku: Long) {
        this.sku = SkuSnowflake.fromId(sku)
    }

    override fun build(): Button {
        return Button.of(style, url ?: customId, label ?: "").withDisabled(disabled).run {
            if (sku != null) {
                this.withSku(sku!!)
            } else {
                this
            }
        }
    }
}

@ComponentsV2Dsl
class TextComponentBuilder(id: Int?) : BaseComponentBuilder<TextDisplay>(id) {
    private var text: String = ""

    fun text(builder: StringBuilder.() -> Unit) {
        text = StringBuilder().apply(builder).toString()
    }

    fun text(text: String) {
        this.text = text
    }

    override fun build() = TextDisplay.of(text)
}

data class Option(
    val label: String,
    val description: String?,
    val value: String,
    val emoji: Emoji?,
    val default: Boolean = false
)

@ComponentsV2Dsl
class StringSelectBuilder(id: Int?, private val customId: String) :
    BaseComponentBuilder<StringSelectMenu>(id) {

    private var placeholder: String? = null
    private var minValues: Int? = null
    private var maxValues: Int? = null
    private var disabled: Boolean = false
    private val options = mutableListOf<Option>()

    fun placeholder(placeholder: String) {
        this.placeholder = placeholder
    }

    fun minValues(minValues: Int) {
        check(minValues >= 0) { "Min values must be >= 0" }
        this.minValues = minValues
    }

    fun maxValues(maxValues: Int) {
        check(maxValues >= 1) { "Max values must be >= 1" }
        this.maxValues = maxValues
    }

    fun disable(disabled: Boolean = true) {
        this.disabled = disabled
    }

    fun option(label: String, value: String, builder: OptionBuilder.() -> Unit) {
        this.options.add(OptionBuilder(label, value).apply(builder).build())
    }

    override fun build(): StringSelectMenu {
        return StringSelectMenu.create(this.customId).apply {
            placeholder?.let { placeholder(it) }
            this@StringSelectBuilder.minValues?.let { minValues(it) }
            this@StringSelectBuilder.maxValues?.let { maxValues(it) }
            isDisabled = disabled
            val options = options.map { selectOption ->
                SelectOption.of(selectOption.label, selectOption.value)
                    .withDefault(selectOption.isDefault).withDescription(selectOption.description)
                    .withEmoji(selectOption.emoji)
            }
            addOptions(options)
        }.build()
    }
}

@ComponentsV2Dsl
class OptionBuilder(
    private val label: String,
    private val value: String,
) : Builder<Option> {

    private var description: String? = null
    private var emoji: Emoji? = null
    private var default: Boolean = false

    fun description(description: String) {
        this.description = description
    }

    fun description(builder: StringBuilder.() -> Unit) {
        this.description = StringBuilder().apply(builder).toString()
    }

    fun emoji(emoji: Emoji) {
        this.emoji = emoji
    }

    fun default(default: Boolean = true) {
        this.default = default
    }

    override fun build(): Option {
        return Option(label, description, value, emoji, default)
    }

}

@ComponentsV2Dsl
class TextInputBuilder(
    private val label: String,
    private val customId: String,
    id: Int?
) : BaseComponentBuilder<TextInput>(id) {

    private var style: TextInputStyle = TextInputStyle.SHORT
    private var minLength: Int? = null
    private var maxLength: Int? = null
    private var required: Boolean = true
    private var value: String? = null
    private var placeholder: String? = null

    fun style(style: TextInputStyle) {
        this.style = style
    }

    fun minLength(minLength: Int) {
        check(minLength >= 0) { "Min length must be >= 0" }
        this.minLength = minLength
    }

    fun maxLength(maxLength: Int) {
        check(maxLength >= 1) { "Max length must be >= 1" }
        this.maxLength = maxLength
    }

    fun required(required: Boolean = true) {
        this.required = required
    }

    fun value(value: String) {
        this.value = value
    }

    fun value(builder: StringBuilder.() -> Unit) {
        this.value = StringBuilder().apply(builder).toString()
    }

    fun placeholder(placeholder: String) {
        this.placeholder = placeholder
    }

    fun placeholder(builder: StringBuilder.() -> Unit) {
        this.placeholder = StringBuilder().apply(builder).toString()
    }

    override fun build(): TextInput {
        return TextInput.create(customId, label, style).apply {
            minLength?.let { setMinLength(it) }
            maxLength?.let { setMaxLength(it) }
            isRequired = required
            value?.let { setValue(it) }
            placeholder?.let { placeholder = it }
        }.build()
    }
}

@ComponentsV2Dsl
class EntitySelectMenuBuilder(
    private val customId: String,
    id: Int?,
    private vararg val types: EntitySelectMenu.SelectTarget
) : BaseComponentBuilder<SelectMenu>(id) {

    private var placeholder: String? = null
    private val defaultValues: MutableList<EntitySelectMenu.DefaultValue> = mutableListOf()
    private var minValues: Int? = null
    private var maxValues: Int? = null
    private var disabled: Boolean = false

    fun placeholder(placeholder: String) {
        this.placeholder = placeholder
    }

    fun placeholder(builder: StringBuilder.() -> Unit) {
        this.placeholder = StringBuilder().apply(builder).toString()
    }

    fun minValues(minValues: Int) {
        check(minValues >= 0) { "Min values must be >= 0" }
        this.minValues = minValues
    }

    fun maxValues(maxValues: Int) {
        check(maxValues >= 1) { "Max values must be >= 1" }
        this.maxValues = maxValues
    }

    fun disable(disabled: Boolean = true) {
        this.disabled = disabled
    }

    fun default(snowflake: UserSnowflake) {
        check(types.contains(EntitySelectMenu.SelectTarget.USER)) {
            "Default value can only be set for USER type"
        }
        defaultValues.add(EntitySelectMenu.DefaultValue.from(snowflake))
    }

    fun default(role: Role) {
        check(types.contains(EntitySelectMenu.SelectTarget.ROLE)) {
            "Default value can only be set for ROLE type"
        }
        defaultValues.add(EntitySelectMenu.DefaultValue.from(role))
    }

    fun default(channel: GuildChannel) {
        check(types.contains(EntitySelectMenu.SelectTarget.CHANNEL)) {
            "Default value can only be set for CHANNEL type"
        }
        defaultValues.add(EntitySelectMenu.DefaultValue.from(channel))
    }

    fun default(id: String, selectTarget: EntitySelectMenu.SelectTarget) {
        when (selectTarget) {
            EntitySelectMenu.SelectTarget.ROLE -> EntitySelectMenu.DefaultValue.role(id)
            EntitySelectMenu.SelectTarget.USER -> EntitySelectMenu.DefaultValue.user(id)
            EntitySelectMenu.SelectTarget.CHANNEL -> EntitySelectMenu.DefaultValue.channel(id)
        }
    }

    fun default(id: Long, selectTarget: EntitySelectMenu.SelectTarget) {
        when (selectTarget) {
            EntitySelectMenu.SelectTarget.ROLE -> EntitySelectMenu.DefaultValue.role(id)
            EntitySelectMenu.SelectTarget.USER -> EntitySelectMenu.DefaultValue.user(id)
            EntitySelectMenu.SelectTarget.CHANNEL -> EntitySelectMenu.DefaultValue.channel(id)
        }
    }

    override fun build(): SelectMenu {
        return EntitySelectMenu.create(customId, types.toList()).apply {
            placeholder?.let { setPlaceholder(it) }
            minValues?.let { setMinValues(it) }
            maxValues?.let { setMaxValues(it) }
            isDisabled = disabled
            setDefaultValues(defaultValues.toList())
        }.build()
    }

}

@ComponentsV2Dsl
class FileBuilder(
    id: Int?
) : BaseComponentBuilder<FileDisplay>(id) {

    private var name: String? = null
    private var upload: FileUpload? = null
    private var spoiler: Boolean = false

    fun name(string: String) {
        check(upload == null) { "Upload is already set" }
        this.name = string
    }

    fun upload(upload: FileUpload) {
        check(name == null) { "Name is already set" }
        this.upload = upload
    }

    fun spoiler(spoiler: Boolean = true) {
        this.spoiler = spoiler
    }

    override fun build(): FileDisplay {
        check(name != null || upload != null) {
            "Either name or upload must be set for FileDisplay"
        }
        val component = if (name != null) {
            FileDisplay.fromFile(name!!)
        } else if (upload != null) {
            FileDisplay.fromFile(upload!!)
        } else {
            throw IllegalStateException("Either name or upload must be set for FileDisplay")
        }

        return component.withSpoiler(spoiler)
    }

}

@ComponentsV2Dsl
class SeparatorBuilder(
    id: Int?
) : BaseComponentBuilder<Separator>(id) {

    private var spacing: Separator.Spacing = Separator.Spacing.SMALL
    private var divider: Boolean = true

    override fun build(): Separator {
        return Separator.create(divider, spacing)
    }
}

@ComponentsV2Dsl
class MediaGalleryBuilder(
    id: Int?
) : BaseComponentBuilder<MediaGallery>(id) {

    private val items: MutableList<MediaGalleryItem> = mutableListOf()

    fun file(upload: FileUpload) {
        items.add(MediaGalleryItem.fromFile(upload))
    }

    fun url(url: String) {
        items.add(MediaGalleryItem.fromUrl(url))
    }

    override fun build(): MediaGallery {
        return MediaGallery.of(items)
    }

}