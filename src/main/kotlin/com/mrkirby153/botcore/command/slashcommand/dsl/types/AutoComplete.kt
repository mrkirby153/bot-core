package com.mrkirby153.botcore.command.slashcommand.dsl.types

import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent


typealias AutoCompleteCallback = (CommandAutoCompleteInteractionEvent) -> List<Pair<String, String>>