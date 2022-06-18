package com.mrkirby153.botcore.command.slashcommand.dsl.types

import com.mrkirby153.botcore.command.slashcommand.dsl.ArgumentConverter
import com.mrkirby153.botcore.command.slashcommand.dsl.Arguments
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.interactions.commands.OptionMapping
import net.dv8tion.jda.api.interactions.commands.OptionType

class UserConverter : ArgumentConverter<User> {
    override fun convert(input: OptionMapping): User {
        return input.asUser
    }
}

class UserArgument : GenericArgument<User>(OptionType.USER, ::UserConverter)
class OptionalUserArgument : GenericNullableArgument<User>(OptionType.USER, ::UserConverter)

fun Arguments.user(body: UserArgument.() -> Unit) = genericArgument(::UserArgument, body)
fun Arguments.optionalUser(body: OptionalUserArgument.() -> Unit) =
    optionalGenericArgument(::OptionalUserArgument, body)