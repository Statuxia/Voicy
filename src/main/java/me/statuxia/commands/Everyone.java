package me.statuxia.commands;

import me.statuxia.Main;
import me.statuxia.utils.Config;
import me.statuxia.utils.MySQLConnector;
import me.statuxia.utils.UserInfo;
import me.statuxia.voice.Voice;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.awt.*;
import java.util.EnumSet;
import java.util.Objects;

public class Everyone extends ListenerAdapter {

    private static final String HELP_TEXT = """
            :wrench: **BASIC**
            > `>help` - help command.
            > `>ping` - pong.
            :star: **PERSONAL SETTINGS**
            > `voice settings` - shows settings of your voice.
            > `>voice name {name}` - edits voice name.
            > `>voice limit {0 to 99}` - sets limit on voice.
            > `>voice reset` - reset all settings.
            > `>voice clear` - clear all allows and denys.
            > `>voice private` - make channel visible only for you.
            > `>voice lock` - make channel visible for you and allowed users.
            > `>voice unlock` - make channel visible for you and allowed users.
            > `>voice allow {@user/id}` - allows the selected user to join.
            > `>voice deny {@user/id}` - prohibits the selected user from joining.
            :crown: **ADMIN COMMANDS**
            > `>voice create` - creates generator channel.
            > `>voice block {@user/id}` - blocks user to join channel.
            > `>voice unblock {@user/id}` - unblocks user to join channel.
                        
            `* -> send again for off`.
            """;

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        Member member = event.getMember();
        EmbedBuilder embed = new EmbedBuilder();

        if (event.getAuthor().isBot() || member == null || !event.isFromGuild())
            return;

        Message msg = event.getMessage();
        if (!Objects.requireNonNull(event.getGuild().getMember(Main.bot)).hasPermission(Permission.ADMINISTRATOR)) {
            embed.setTitle("Something's wrong...");
            embed.setDescription("I don't have [ADMINISTRATOR] permissions.");
            embed.setColor(Color.YELLOW);
            msg.replyEmbeds(embed.build()).queue();
            return;
        }

        if (msg.getContentRaw().toLowerCase().startsWith(Config.PREFIX + "help")) {
            embed.setTitle("Voicy help commands");
            embed.setDescription(HELP_TEXT);
            embed.setColor(Color.GREEN);
            msg.replyEmbeds(embed.build()).queue();
            return;
        }

        if (msg.getContentRaw().toLowerCase().startsWith(Config.PREFIX + "ping")) {
            long start = System.currentTimeMillis();
            embed.setTitle("Pong");
            embed.setDescription("");
            embed.setColor(Color.GREEN);
            msg.replyEmbeds(embed.build()).queue(m -> {
                long end = (System.currentTimeMillis() - start);
                m.editMessageEmbeds(embed.setDescription(end + " ms...").build()).queue();
            });
            return;
        }

        if (msg.getContentRaw().toLowerCase().startsWith(Config.PREFIX_VOICE + " settings")) {
            MySQLConnector.getUserSettings(member.getIdLong());
            UserInfo settings = UserInfo.get(member.getIdLong());
            if (settings == null) {
                embed.setTitle("Voice Settings");
                embed.setDescription("I'm sorry, but you've never created a channel");
                embed.setColor(Color.GREEN);
                msg.replyEmbeds(embed.build()).queue();
                return;
            }

            embed.setTitle("Voice Settings");
            embed.setDescription("**Voice User:** " + msg.getAuthor().getAsMention() + "\n" +
                    "**Voice Name:** " + settings.getVoiceName() + "\n" +
                    "**Voice Max Entry:** " + settings.getMaxEntry() + "\n" +
                    "**Voice Alloweds (IDS):** " + settings.getAlloweds() + "\n" +
                    "**Voice Denieds (IDS):** " + settings.getDenieds()
            );
            embed.setColor(Color.GREEN);
            msg.replyEmbeds(embed.build()).queue();
            return;
        }

        if (msg.getContentRaw().toLowerCase().startsWith(Config.PREFIX_VOICE + " name")) {
            if (MySQLConnector.setVoiceName(msg.getAuthor().getIdLong(), msg.getContentRaw().substring(11))) {
                embed.setTitle("Voice Settings");
                embed.setDescription("Voice name successfully changed.");
                embed.setColor(Color.GREEN);
                msg.replyEmbeds(embed.build()).queue();
            } else {
                embed.setTitle("Voice Settings");
                embed.setDescription("Sorry, but your voice name is wrong.");
                embed.setColor(Color.RED);
                msg.replyEmbeds(embed.build()).queue();
            }
            return;
        }

        if (msg.getContentRaw().toLowerCase().startsWith(Config.PREFIX_VOICE + " limit")) {
            int limit = Integer.parseInt(msg.getContentRaw().substring(13).strip());
            if (MySQLConnector.setMaxEntry(msg.getAuthor().getIdLong(), limit)) {
                embed.setTitle("Voice Settings");
                embed.setDescription("Voice limit successfully changed.");
                embed.setColor(Color.GREEN);
                msg.replyEmbeds(embed.build()).queue();
            } else {
                embed.setTitle("Voice Settings");
                embed.setDescription("Sorry, but your limit input is wrong.");
                embed.setColor(Color.RED);
                msg.replyEmbeds(embed.build()).queue();
            }
            return;
        }

        if (msg.getContentRaw().toLowerCase().startsWith(Config.PREFIX_VOICE + " reset")) {
            Voice.defaultSettings(member);
            embed.setTitle("Voice Settings");
            embed.setDescription("Voice settings dropped to default.");
            embed.setColor(Color.GREEN);
            msg.replyEmbeds(embed.build()).queue();
        }

        if (msg.getContentRaw().toLowerCase().startsWith(Config.PREFIX_VOICE + " clear")) {
            MySQLConnector.getUserSettings(member.getIdLong());
            UserInfo info = UserInfo.get(member.getIdLong());
            if (info == null)
                return;

            Voice.defaultSettings(member, info.getVoiceName(), info.getMaxEntry());
            embed.setTitle("Voice Settings");
            embed.setDescription("Alloweds/Denieds users dropped to default.");
            embed.setColor(Color.GREEN);
            msg.replyEmbeds(embed.build()).queue();
        }

        if (msg.getContentRaw().toLowerCase().startsWith(Config.PREFIX_VOICE + " private")) {
            GuildVoiceState a = member.getVoiceState();
            if (a == null)
                return;

            VoiceChannel channel = a.getChannel();
            if (channel == null)
                return;

            Role everyone = msg.getGuild().getPublicRole();
            channel.getManager().clearOverridesAdded()
                    .clearOverridesRemoved()
                    .putRolePermissionOverride(everyone.getIdLong(), null, EnumSet.of(Permission.VIEW_CHANNEL))
                    .putPermissionOverride(member, EnumSet.of(Permission.VIEW_CHANNEL), null)
                    .queue();
        }

        if (msg.getContentRaw().toLowerCase().startsWith(Config.PREFIX_VOICE + " lock")) {
            GuildVoiceState a = member.getVoiceState();
            if (a == null)
                return;

            VoiceChannel channel = a.getChannel();
            if (channel == null)
                return;

            Role everyone = msg.getGuild().getPublicRole();
            channel.getManager().clearOverridesAdded()
                    .putRolePermissionOverride(everyone.getIdLong(), null, EnumSet.of(Permission.VIEW_CHANNEL))
                    .putPermissionOverride(member, EnumSet.of(Permission.VIEW_CHANNEL), null)
                    .queue();
        }

        if (msg.getContentRaw().toLowerCase().startsWith(Config.PREFIX_VOICE + " unlock")) {
            GuildVoiceState a = member.getVoiceState();
            if (a == null)
                return;

            VoiceChannel channel = a.getChannel();
            if (channel == null)
                return;

            Role everyone = msg.getGuild().getPublicRole();
            channel.getManager().clearOverridesAdded()
                    .putRolePermissionOverride(everyone.getIdLong(), EnumSet.of(Permission.VIEW_CHANNEL), null)
                    .putPermissionOverride(member, EnumSet.of(Permission.VIEW_CHANNEL), null)
                    .queue();
            return;
        }

        if (msg.getContentRaw().toLowerCase().startsWith(Config.PREFIX_VOICE + " allow")) {
            if (MySQLConnector.addAllowed(msg.getAuthor().getIdLong(), msg.getContentRaw().strip().
                    substring(13).replaceAll("[<@!>]", ""))) {
                embed.setTitle("Voice Settings");
                embed.setDescription("User allowed to your voice channel.");
                embed.setColor(Color.GREEN);
                msg.replyEmbeds(embed.build()).queue();
            } else {
                embed.setTitle("Voice Settings");
                embed.setDescription("Sorry, but user ID is wrong or you already allowed it.");
                embed.setColor(Color.RED);
                msg.replyEmbeds(embed.build()).queue();
            }
            return;
        }

        if (msg.getContentRaw().toLowerCase().startsWith(Config.PREFIX_VOICE + " deny")) {
            if (MySQLConnector.addDenied(msg.getAuthor().getIdLong(), msg.getContentRaw().strip()
                    .substring(12).replaceAll("[<@!>]", ""))) {
                embed.setTitle("Voice Settings");
                embed.setDescription("User denied to your voice channel.");
                embed.setColor(Color.GREEN);
                msg.replyEmbeds(embed.build()).queue();
            } else {
                embed.setTitle("Voice Settings");
                embed.setDescription("Sorry, but user ID is wrong or you already denied it.");
                embed.setColor(Color.RED);
                msg.replyEmbeds(embed.build()).queue();
            }
        }
    }
}