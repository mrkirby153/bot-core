package com.mrkirby153.botcore.builder

import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.entities.MessageEmbed.AuthorInfo
import net.dv8tion.jda.api.entities.MessageEmbed.Field
import net.dv8tion.jda.api.entities.MessageEmbed.Footer
import net.dv8tion.jda.api.entities.MessageEmbed.ImageInfo
import net.dv8tion.jda.api.entities.User
import java.awt.Color
import java.time.OffsetDateTime

@DslMarker
annotation class EmbedDsl

@EmbedDsl
class EmbedBuilder : Builder<MessageEmbed> {

    var description = ""
    var url: String? = null
    var title: String? = null
    var timestamp: OffsetDateTime? = null

    val fields = mutableListOf<FieldBuilder>()

    val thumbnailBuilder = ThumbnailBuilder()
    val authorBuilder = AuthorBuilder()
    val footerBuilder = FooterBuilder()
    val imageBuilder = ImageBuilder()
    val colorBuilder: ColorBuilder = ColorBuilder()

    inline fun thumbnail(builder: ThumbnailBuilder.() -> Unit) {
        thumbnailBuilder.apply(builder)
    }

    inline fun author(builder: AuthorBuilder.() -> Unit) {
        authorBuilder.apply(builder)
    }

    inline fun footer(builder: FooterBuilder.() -> Unit) {
        footerBuilder.apply(builder)
    }

    inline fun image(builder: ImageBuilder.() -> Unit) {
        imageBuilder.apply(builder)
    }

    inline fun field(builder: FieldBuilder.() -> Unit) {
        fields.add(FieldBuilder().apply(builder))
    }

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
        setImage(imageBuilder.build().url)
        setColor(colorBuilder.build())
    }.build()
}

class ThumbnailBuilder : Builder<String> {
    var url = ""

    override fun build() = url
}

class FieldBuilder : Builder<Field> {
    var name = ""
    var value = ""
    var inline = false
    override fun build() = Field(name, value, inline)
}

class AuthorBuilder : Builder<AuthorInfo> {
    var name = ""
    var url = ""
    var iconUrl = ""

    fun userIcon(user: User) {
        iconUrl = user.avatarUrl ?: user.defaultAvatarUrl
    }

    override fun build() = AuthorInfo(name, url, iconUrl, null)

}

class FooterBuilder : Builder<Footer> {
    var text = ""
    var iconUrl = ""

    fun userIcon(user: User) {
        iconUrl = user.avatarUrl ?: user.defaultAvatarUrl
    }

    override fun build() = Footer(text, iconUrl, null)

}

class ImageBuilder : Builder<ImageInfo> {
    var url = ""

    override fun build() = ImageInfo(url, null, 0, 0)

}

class ColorBuilder : Builder<Color?> {
    var color: Color? = null

    fun rgb(red: Int, green: Int, blue: Int) {
        color = Color.decode(
            "${Integer.toHexString(red)}${Integer.toHexString(green)}${
                Integer.toHexString(blue)
            }"
        )
    }

    fun hex(string: String) {
        color = Color.getColor(string)
    }

    override fun build(): Color? = color
}