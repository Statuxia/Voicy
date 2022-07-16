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
import java.util.EnumSet;
import java.util.Objects;

public class Admin extends ListenerAdapter {
    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        Member member = event.getMember();
        EmbedBuilder embed = new EmbedBuilder();
        Message message = event.getMessage();

        if (!event.isFromGuild() || event.getAuthor().isBot() || member == null || !member.hasPermission(Permission.ADMINISTRATOR) ||
                !Objects.requireNonNull(event.getGuild().getMember(Main.bot)).hasPermission(Permission.ADMINISTRATOR)) {
            return;
        }

        if (message.getContentRaw().toLowerCase().startsWith(Config.PREFIX_VOICE + " create")) {
            message.getGuild().createVoiceChannel("[ + ]").setParent(message.getCategory()).queue(channel -> {
                MySQLConnector.newGenerator(channel.getGuild().getIdLong(), channel.getIdLong());
                embed.setTitle("The generator channel has been created!");
                embed.setDescription("Join <#" + channel.getId() + "> to create a room!\nType >help to get help");
                embed.setColor(Color.GREEN);
                message.replyEmbeds(embed.build()).queue();
            });
            return;
        }

        if (message.getContentRaw().toLowerCase().startsWith(Config.PREFIX_VOICE + " block")) {
            String[] content = message.getContentRaw().split(" ");
            String userString = content[content.length - 1].replaceAll("[<@!>]+", "").strip();
            Member user = message.getGuild().getMemberById(userString);

            if (user == null) {
                embed.setTitle("Block!");
                embed.setDescription("There is no user with id " + userString);
                embed.setColor(Color.GREEN);
                message.replyEmbeds(embed.build()).queue();
                return;
            }

            long channelID = MySQLConnector.getGenerator(event.getGuild().getIdLong());
            if (channelID == 0L)
                return;

            GuildChannel channel = event.getGuild().getGuildChannelById(channelID);
            if (channel == null)
                return;

            channel.getManager().putPermissionOverride(user, null, EnumSet.of(Permission.VOICE_CONNECT)).queue();
            embed.setTitle("Block!");
            embed.setDescription(user.getAsMention() + " now can't join to the " + channel.getAsMention());
            embed.setColor(Color.RED);
            message.replyEmbeds(embed.build()).queue();
            return;
        }

        if (message.getContentRaw().toLowerCase().startsWith(Config.PREFIX + "voice unblock")) {
            String[] content = message.getContentRaw().split(" ");
            String userString = content[content.length - 1].replaceAll("[<@!>]+", "").strip();
            Member user = message.getGuild().getMemberById(userString);

            if (user == null) {
                embed.setTitle("Block!");
                embed.setDescription("There is no user with id " + userString);
                embed.setColor(Color.GREEN);
                message.replyEmbeds(embed.build()).queue();
                return;
            }


            long channelID = MySQLConnector.getGenerator(event.getGuild().getIdLong());
            if (channelID == 0L)
                return;

            GuildChannel channel = event.getGuild().getGuildChannelById(channelID);
            if (channel == null)
                return;

            channel.getManager().putPermissionOverride(user, EnumSet.of(Permission.VOICE_CONNECT), null).queue();
            embed.setTitle("Unblock!");
            embed.setDescription(user.getAsMention() + " now can join to the " + channel.getAsMention());
            embed.setColor(Color.GREEN);
            message.replyEmbeds(embed.build()).queue();
        }
    }
}