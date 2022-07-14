package me.statuxia.commands;

import me.statuxia.Main;
import me.statuxia.utils.Config;
import me.statuxia.utils.MySQLConnector;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.awt.*;
import java.sql.SQLException;
import java.util.Objects;

public class Admin extends ListenerAdapter {


    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        Member member = event.getMember();
        EmbedBuilder embed = new EmbedBuilder();

        if (event.getAuthor().isBot() || member == null) return;

        Message msg = event.getMessage();
        if (!member.hasPermission(Permission.ADMINISTRATOR)) {
            embed.setTitle("Something's wrong...");
            embed.setDescription("You don't have permissions to do that.");
            embed.setColor(Color.RED);
            msg.replyEmbeds(embed.build()).queue();
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

        if (msg.getContentRaw().toLowerCase().startsWith(Config.PREFIX + "create")) {
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

            return;
        }
        msg.reply("I don't know command " + msg.getContentRaw()).queue();
    }

    // TO DO
    //  Make this comamnds:
    //   >voice block {@user/id} - blocks the possibility of creating a channel.
}
