package com.mrkirby153.botcore.command

import net.dv8tion.jda.api.Permission

/**
 * Marks a method as a command annotation.
 */
@Target(AnnotationTarget.FUNCTION)
annotation class Command(val name: String, val clearance: Int = 0,
                         val arguments: Array<String> = [], val parent: String = "", val permissions: Array<Permission> = [])
