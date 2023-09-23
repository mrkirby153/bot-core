package com.mrkirby153.botcore.command.slashcommand.dsl.types

import com.mrkirby153.botcore.command.slashcommand.dsl.ArgumentConverter
import com.mrkirby153.botcore.command.slashcommand.dsl.Arguments
import net.dv8tion.jda.api.entities.IMentionable
import net.dv8tion.jda.api.entities.Role
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.interactions.commands.OptionMapping
import net.dv8tion.jda.api.interactions.commands.OptionType

object MentionableConverter : ArgumentConverter<IMentionable> {
    override fun convert(input: OptionMapping): IMentionable {
        return input.asMentionable
    }

    override val type = OptionType.MENTIONABLE
}

object UserConverter : ArgumentConverter<User> {
    override fun convert(input: OptionMapping): User {
        return input.asUser
    }

    override val type = OptionType.USER
}

object RoleConverter : ArgumentConverter<Role> {
    override fun convert(input: OptionMapping): Role {
        return input.asRole
    }

    override val type = OptionType.ROLE
}

//fun Arguments.mentionable(body: ArgumentBuilder<IMentionable>.() -> Unit) =
//    ArgumentBuilder(this, MentionableConverter).apply(body)
//
//fun Arguments.user(body: ArgumentBuilder<User>.() -> Unit) =
//    ArgumentBuilder(this, UserConverter).apply(body)
//
//fun Arguments.role(body: ArgumentBuilder<Role>.() -> Unit) =
//    ArgumentBuilder(this, RoleConverter).apply(body)