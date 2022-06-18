package com.mrkirby153.botcore.command.slashcommand.dsl.types

import com.mrkirby153.botcore.command.slashcommand.dsl.ArgumentConverter
import com.mrkirby153.botcore.command.slashcommand.dsl.Arguments
import net.dv8tion.jda.api.entities.Role
import net.dv8tion.jda.api.interactions.commands.OptionMapping
import net.dv8tion.jda.api.interactions.commands.OptionType

class RoleConverter : ArgumentConverter<Role> {
    override fun convert(input: OptionMapping): Role {
        return input.asRole
    }
}

class RoleArgument : GenericArgument<Role>(OptionType.ROLE, ::RoleConverter)
class OptionalRoleArgument : GenericNullableArgument<Role>(OptionType.ROLE, ::RoleConverter)

fun Arguments.role(body: RoleArgument.() -> Unit) = genericArgument(::RoleArgument, body)
fun Arguments.optionalRole(body: OptionalRoleArgument.() -> Unit) =
    optionalGenericArgument(::OptionalRoleArgument, body)