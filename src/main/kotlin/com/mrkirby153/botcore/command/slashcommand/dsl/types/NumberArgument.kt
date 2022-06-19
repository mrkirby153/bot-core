package com.mrkirby153.botcore.command.slashcommand.dsl.types

import net.dv8tion.jda.api.interactions.commands.build.OptionData

interface HasMinAndMax<T: Number> : ModifiesOption {
    var min: T?
    var max: T?

    override fun modify(option: OptionData) {
        val min = min
        val max = max
        when (min) {
            is Double -> option.setMinValue(min)
            is Int, is Long -> option.setMinValue(min.toLong())
        }
        when (max) {
            is Double -> option.setMaxValue(max)
            is Int, is Long -> option.setMaxValue(max.toLong())
        }
    }
}