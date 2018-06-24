package com.mrkirby153.botcore.command

/**
 * Marks a method as a command annotation.
 */
@Target(AnnotationTarget.FUNCTION)
annotation class Command(val name: String, val clearance: Int = 0,
                         val arguments: Array<String> = [], val parent: String = "")
