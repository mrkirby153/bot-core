package com.mrkirby153.botcore.utils

import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.Role
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.entities.channel.concrete.PrivateChannel
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import kotlin.reflect.KProperty

class Reference<T>(private var entity: T, private val refresh: (T) -> T?) {
    operator fun getValue(ref: Any?, prop: KProperty<*>): T {
        entity = refresh(entity) ?: entity
        return entity
    }

    operator fun setValue(ref: Any?, property: KProperty<*>, t: T) {
        this.entity = t
    }
}

fun User.ref() = Reference(this) {
    this.jda.getUserById(it.id)
}

fun Member.ref() = Reference(this) {
    this.guild.getMemberById(it.id)
}

fun Guild.ref() = Reference(this) {
    this.jda.getGuildById(it.id)
}

fun Role.ref() = Reference(this) {
    this.guild.getRoleById(it.id)
}

fun PrivateChannel.ref() = Reference(this) {
    this.jda.getPrivateChannelById(this.id)
}

@Suppress("UNCHECKED_CAST")
fun <T : GuildChannel> T.ref() = Reference(this) {
    jda.getGuildChannelById(type, idLong) as T
}

/**
 * SLF4J Wrapper
 */
object SLF4J {
    operator fun getValue(ref: Any?, prop: KProperty<*>): Logger {
        return LoggerFactory.getLogger(ref!!::class.java)!!
    }

    operator fun invoke(name: String) = lazy {
        LoggerFactory.getLogger(name)!!
    }

    inline operator fun <reified T> invoke() = lazy {
        LoggerFactory.getLogger(T::class.java)!!
    }
}