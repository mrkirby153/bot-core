package com.mrkirby153.botcore.command

import net.dv8tion.jda.api.entities.Message


class Context(message: Message) : Message by message {
    var clearance: Int = 0
    var commandName: String = ""
    var commandPrefix: String = ""
}