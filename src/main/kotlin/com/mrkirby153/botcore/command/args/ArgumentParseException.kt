package com.mrkirby153.botcore.command.args

/**
 * An exception that's thrown if an error occurs when parsing an argument
 *
 * @param msg The error message
 */
class ArgumentParseException(msg: String) : Exception(msg)