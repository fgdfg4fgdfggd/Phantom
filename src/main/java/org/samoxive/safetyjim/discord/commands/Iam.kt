package org.samoxive.safetyjim.discord.commands

import net.dv8tion.jda.core.EmbedBuilder
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent
import org.samoxive.safetyjim.database.RolesTable
import org.samoxive.safetyjim.database.SettingsEntity
import org.samoxive.safetyjim.discord.*
import java.awt.Color
import java.util.*

class Iam : Command() {
    override val usages = arrayOf("iam <roleName> - self assigns specified role, removes role if it is already assigned", "iam list - lists available self-assignable roles")

    override suspend fun run(bot: DiscordBot, event: GuildMessageReceivedEvent, settings: SettingsEntity, args: String): Boolean {
        val messageIterator = Scanner(args)

        val member = event.member
        val message = event.message
        val guild = event.guild
        val channel = event.channel

        val roleName = messageIterator.seekToEnd()
                .toLowerCase()

        if (roleName == "") {
            return true
        }

        if (roleName == "list") {
            val roles = RolesTable.fetchGuildRoles(guild)
            if (roles.isEmpty()) {
                message.successReact(bot)
                channel.trySendMessage("No self-assignable roles have been added yet!")
                return false
            }

            val rolesText = roles.mapNotNull { guild.getRoleById(it.roleId) }
                    .joinToString("\n") { "\u2022 `" + it.name + "`" }

            val embed = EmbedBuilder()
            embed.setAuthor("Safety Jim", null, event.jda.selfUser.avatarUrl)
            embed.addField("List of self-assignable roles", truncateForEmbed(rolesText), false)
            embed.setColor(Color(0x4286F4))

            message.successReact(bot)
            channel.trySendMessage(embed.build())
            return false
        }

        val matchingRoles = guild.roles
                .filter { role -> role.name.toLowerCase() == roleName }

        if (matchingRoles.isEmpty()) {
            message.failMessage(bot, "Could not find a role with specified name!")
            return false
        }

        val matchedRole = matchingRoles[0]
        if (!RolesTable.isSelfAssignable(guild, matchedRole)) {
            message.failMessage(bot, "This role is not self-assignable!")
            return false
        }

        val controller = guild.controller
        if (member.roles.find { it == matchedRole } != null) {
            try {
                controller.removeSingleRoleFromMember(member, matchedRole).await()
                message.successReact(bot)
            } catch (e: Exception) {
                message.failMessage(bot, "Could not remove specified role. Do I have enough permissions?")
            }
        } else {
            try {
                controller.addSingleRoleToMember(member, matchedRole).await()
                message.successReact(bot)
            } catch (e: Exception) {
                message.failMessage(bot, "Could not assign specified role. Do I have enough permissions?")
            }
        }

        return false
    }
}
