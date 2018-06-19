package com.mrkirby153.botcore.shard

import net.dv8tion.jda.core.JDA

class Shard(jda: JDA, val shardId: Int) : JDA by jda {

    override fun toString(): String {
        return "Shard[$shardId]"
    }

}