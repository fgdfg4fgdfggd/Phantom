package org.samoxive.safetyjim.discord.commands

import java.awt.Color
import java.util.*
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import org.ocpsoft.prettytime.PrettyTime
import org.samoxive.safetyjim.database.BansTable
import org.samoxive.safetyjim.database.SettingsEntity
import org.samoxive.safetyjim.discord.*

class Info : Command() {
    override val usages = arrayOf("info - displays some information about the bot")
    private val supportServer = "https://www.roblox.com/groups/3326755/ROBLOX-Community-Developers#!/about"
    private val githubLink = "https://rcdforum.com/"
    private val botInviteLink = "https://discord.gg/7RuJrUT"
    private val patreonLink = "https://rcdforum.com/u/dis_chat"
    private val prettyTime = PrettyTime()

    override suspend fun run(bot: DiscordBot, event: GuildMessageReceivedEvent, settings: SettingsEntity, args: String): Boolean {
        val config = bot.config
        val currentShard = event.jda
        val shards = bot.shards.map { shard -> shard.jda }
        val guild = event.guild
        val selfUser = currentShard.selfUser
        val message = event.message
        val channel = event.channel

        val shardCount = shards.size
        val shardId = getShardIdFromGuildId(guild.idLong, shardCount)
        val shardString = getShardString(shardId, shardCount)

        val uptimeString = prettyTime.format(bot.startTime)

        val guildCount = bot.guildCount
        val userCount = shards
                .asSequence()
                .map { shard -> shard.users.size }
                .sum()
        val pingShard = currentShard.gatewayPing
        val pingAverage = shards
                .asSequence()
                .map { shard -> shard.gatewayPing }
                .sum() / shardCount

        val runtime = Runtime.getRuntime()
        val ramTotal = runtime.totalMemory() / (1024 * 1024)
        val ramUsed = ramTotal - runtime.freeMemory() / (1024 * 1024)

        val lastBanRecord = BansTable.fetchGuildLastBan(guild)

        var daysSince = "\u221E" // Infinity symbol

        if (lastBanRecord != null) {
            val now = Date()
            val dayCount = (now.time / 1000 - lastBanRecord.banTime) / (60 * 60 * 24)
            daysSince = dayCount.toString()
        }

        val embed = EmbedBuilder()
        embed.setAuthor("Phantom - v${config.jim.version} - Shard $shardString", null, selfUser.avatarUrl)
        embed.setDescription("Lifting the :hammer: since $uptimeString")
        embed.addField("Server Count", guildCount.toString(), true)
        embed.addField("User Count", userCount.toString(), true)
        embed.addBlankField(true)
        embed.addField("Websocket Ping", "Shard $shardString: ${pingShard}ms\nAverage: ${pingAverage}ms", false)
        embed.addField("RAM usage", "${ramUsed}MB / ${ramTotal}MB", false)
        embed.addBlankField(true)
        embed.addField("Links", "[RCD Group]($supportServer) | [RCDForum]($githubLink) | [RCD Invite]($botInviteLink) | [RCDForum Founder]($patreonLink)", true)
        embed.setFooter("Made by RCDForum Team. | Days since last incident: $daysSince", null)
        embed.setColor(Color(0x00FF00))

        message.successReact()
        channel.trySendMessage(embed.build())

        return false
    }
}
