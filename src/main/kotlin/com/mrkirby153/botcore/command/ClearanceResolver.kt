package com.mrkirby153.botcore.command

import net.dv8tion.jda.api.entities.Member

/**
 * Interface to aid in resolving a member's clearance
 */
interface ClearanceResolver {

    /**
     * Resolves the clearance for the given member
     *
     * @param member The member to resolve the clearance for
     *
     * @return The member's clearance
     */
    fun resolve(member: Member): Int
}