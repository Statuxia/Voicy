package me.statuxia.commands;

import me.statuxia.Main;
import me.statuxia.utils.Config;
import me.statuxia.utils.MySQLConnector;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.GuildChannel;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.awt.*;
import java.sql.SQLException;
import java.util.EnumSet;
import java.util.Objects;

public class Admin extends ListenerAdapter {
    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        Member member = event.getMember();
        EmbedBuilder embed = new EmbedBuilder();

        if (event.getAuthor().isBot() || member == null) return;

        Message msg = event.getMessage();
        if (!member.hasPermission(Permission.ADMINISTRATOR)) {
            return;
        }
        if (!event.isFromGuild()) {
            embed.setTitle("Something's wrong...");
            embed.setDescription("The bot functions only on guilds.");
            embed.setColor(Color.YELLOW);
            msg.replyEmbeds(embed.build()).queue();
            return;
        }

        if (!Objects.requireNonNull(event.getGuild().getMember(Main.bot)).hasPermission(Permission.ADMINISTRATOR)) {
            embed.setTitle("Something's wrong...");
            embed.setDescription("I don't have [ADMINISTRATOR] permissions.");
            embed.setColor(Color.YELLOW);
            msg.replyEmbeds(embed.build()).queue();
            return;
        }

        if (msg.getContentRaw().toLowerCase().startsWith(Config.PREFIX + "voice create")) {
            msg.getGuild().createVoiceChannel("[ + ]").setParent(msg.getCategory()).queue(channel -> {
                try {
                    MySQLConnector.newChannelInserter(channel.getGuild().getIdLong(), channel.getIdLong());
                    embed.setTitle("The generator channel has been created!");
                    embed.setDescription("Join <#" + channel.getId() + "> to create a room!\nType >help to get help");
                    embed.setColor(Color.GREEN);
                    msg.replyEmbeds(embed.build()).queue();
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            });
        }

        if (msg.getContentRaw().toLowerCase().startsWith(Config.PREFIX + "voice block")) {
            String[] content = msg.getContentRaw().split(" ");
            String userString = content[content.length - 1].replaceAll("[<@!>]+", "");
            Member user = msg.getGuild().getMemberById(userString);

            if (user == null) {
                embed.setTitle("Block!");
                embed.setDescription("There is no user with id " + userString);
                embed.setColor(Color.GREEN);
                msg.replyEmbeds(embed.build()).queue();
                return;
            }

            try {
                long channelID = MySQLConnector.getChannelByGuildID(event.getGuild().getIdLong());
                if (channelID == 0L) return;

                GuildChannel channel = event.getGuild().getGuildChannelById(channelID);
                if (channel == null) return;

                channel.getManager().putPermissionOverride(user, null, EnumSet.of(Permission.VOICE_CONNECT)).queue();
                embed.setTitle("Block!");
                embed.setDescription(user.getAsMention() + " now can't join to the " + channel.getAsMention());
                embed.setColor(Color.RED);
                msg.replyEmbeds(embed.build()).queue();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }

        if (msg.getContentRaw().toLowerCase().startsWith(Config.PREFIX + "voice unblock")) {
            String[] content = msg.getContentRaw().split(" ");
            String userString = content[content.length - 1].replaceAll("[<@!>]+", "");
            Member user = msg.getGuild().getMemberById(userString);

            if (user == null) {
                embed.setTitle("Block!");
                embed.setDescription("There is no user with id " + userString);
                embed.setColor(Color.GREEN);
                msg.replyEmbeds(embed.build()).queue();
                return;
            }

            try {
                long channelID = MySQLConnector.getChannelByGuildID(event.getGuild().getIdLong());
                if (channelID == 0L) return;

                GuildChannel channel = event.getGuild().getGuildChannelById(channelID);
                if (channel == null) return;

                channel.getManager().putPermissionOverride(user, EnumSet.of(Permission.VOICE_CONNECT), null).queue();
                embed.setTitle("Unblock!");
                embed.setDescription(user.getAsMention() + " now can join to the " + channel.getAsMention());
                embed.setColor(Color.GREEN);
                msg.replyEmbeds(embed.build()).queue();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
    }
}