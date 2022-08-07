package com.mrkirby153.botcore.builder

import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.entities.MessageEmbed.AuthorInfo
import net.dv8tion.jda.api.entities.MessageEmbed.Field
import net.dv8tion.jda.api.entities.MessageEmbed.Footer
import net.dv8tion.jda.api.entities.MessageEmbed.ImageInfo
import net.dv8tion.jda.api.entities.User
import java.awt.Color
import java.time.OffsetDateTime

/**
 * DSL marker for embeds
 */
@DslMarker
annotation class EmbedDsl

/**
 * Builder for a [MessageEmbed]
 */
@EmbedDsl
class EmbedBuilder : Builder<MessageEmbed> {

    /**
     * The embed's description
     */
    var description = ""

    /**
     * The embed's URL (Displayed in the title)
     */
    var url: String? = null

    /**
     * The title of this embed
     */
    var title: String? = null

    /**
     * The timestamp of this embed
     */
    var timestamp: OffsetDateTime? = null

    /**
     * A list of fields in this embed
     */
    val fields = mutableListOf<FieldBuilder>()

    /**
     * The Thumbnail builder
     */
    val thumbnailBuilder = ThumbnailBuilder()

    /**
     * The author builder
     */
    val authorBuilder = AuthorBuilder()

    /**
     * The footer builder
     */
    val footerBuilder = FooterBuilder()

    /**
     * The image builder
     */
    val imageBuilder = ImageBuilder()

    /**
     * The color builder
     */
    val colorBuilder: ColorBuilder = ColorBuilder()

    /**
     * Adds a thumbnail to the embed
     */
    inline fun thumbnail(builder: ThumbnailBuilder.() -> Unit) {
        thumbnailBuilder.apply(builder)
    }

    /**
     * Adds an author to the embed
     */
    inline fun author(builder: AuthorBuilder.() -> Unit) {
        authorBuilder.apply(builder)
    }

    /**
     * Adds a footer to the embed
     */
    inline fun footer(builder: FooterBuilder.() -> Unit) {
        footerBuilder.apply(builder)
    }

    /**
     * Adds an image to the embed
     */
    inline fun image(builder: ImageBuilder.() -> Unit) {
        imageBuilder.apply(builder)
    }

    /**
     * Adds a field to the embed
     */
    inline fun field(builder: FieldBuilder.() -> Unit) {
        fields.add(FieldBuilder().apply(builder))
    }

    /**
     * Sets the embed's color
     */
    inline fun color(builder: ColorBuilder.() -> Unit) {
        colorBuilder.apply(builder)
    }

    override fun build(): MessageEmbed = net.dv8tion.jda.api.EmbedBuilder().apply {
        setDescription(description)
        setTitle(title, url)
        setTimestamp(timestamp)

        val author = authorBuilder.build()
        setAuthor(author.name, author.url, author.iconUrl)
        val footer = footerBuilder.build()
        setFooter(footer.text, footer.iconUrl)

        setThumbnail(thumbnailBuilder.build())
        setImage(imageBuilder.build()?.url)
        setColor(colorBuilder.build())
    }.build()
}

/**
 * The builder for an embed's thumbnail
 */
class ThumbnailBuilder : Builder<String?> {
    /**
     * The URL to show in the thumbnail
     */
    var url = ""

    override fun build() = url.ifEmpty { null }
}

/**
 * A builder for a [Field]
 */
class FieldBuilder : Builder<Field> {
    /**
     * The name of the field
     */
    var name = ""

    /**
     * The field's value
     */
    var value = ""

    /**
     * If the field should render inline
     */
    var inline = false
    override fun build() = Field(name, value, inline)
}

/**
 * An embed's [AuthorInfo] builder
 */
class AuthorBuilder : Builder<AuthorInfo> {
    /**
     * The name of the author
     */
    var name = ""

    /**
     * The URL of the author
     */
    var url = ""

    /**
     * The URL of the author's icon
     */
    var iconUrl = ""

    /**
     * Sets the [iconUrl] to the given [user]'s avatar
     */
    fun userIcon(user: User) {
        iconUrl = user.avatarUrl ?: user.defaultAvatarUrl
    }

    override fun build() = AuthorInfo(name, url.ifEmpty { null }, iconUrl.ifEmpty { null }, null)

}

/**
 * An Embed's [Footer] builder
 */
class FooterBuilder : Builder<Footer> {

    /**
     * The text of the footer
     */
    var text = ""

    /**
     * An icon to show in the footer
     */
    var iconUrl = ""

    /**
     * Sets the [iconUrl] to the given [user]'s avatar
     */
    fun userIcon(user: User) {
        iconUrl = user.avatarUrl ?: user.defaultAvatarUrl
    }

    override fun build() = Footer(text, iconUrl.ifEmpty { null }, null)

}

/**
 * An embed's [ImageInfo] builder
 */
class ImageBuilder : Builder<ImageInfo?> {
    /**
     * The URL of the image
     */
    var url = ""

    override fun build() = if (url.isEmpty()) null else ImageInfo(url.ifEmpty { null }, null, 0, 0)

}

/**
 * A builder for an Embed's color
 */
class ColorBuilder : Builder<Color?> {
    /**
     * The color of the embed
     */
    var color: Color? = null

    /**
     * Sets the color to the provided numerical [red], [green], and [blue] values
     */
    fun rgb(red: Int, green: Int, blue: Int) {
        color = Color.decode(
            "${Integer.toHexString(red)}${Integer.toHexString(green)}${
                Integer.toHexString(blue)
            }"
        )
    }

    /**
     * Sets the color to the provided hex value
     */
    fun hex(string: String) {
        color = Color.getColor(string)
    }

    override fun build(): Color? = color
}