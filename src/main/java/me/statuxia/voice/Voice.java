package me.statuxia.voice;

import me.statuxia.utils.Funcs;
import me.statuxia.utils.MySQLConnector;
import me.statuxia.utils.UserInfo;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Category;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceJoinEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceLeaveEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceMoveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;

public class Voice extends ListenerAdapter {

    public static ArrayList<Long> createdChannels = new ArrayList<>();

    public boolean onVoiceJoin(boolean isBot, long guildID, long channelID) {
        if (isBot) return true;
        try {
            return MySQLConnector.isVoiceGenerator(guildID, channelID);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean onVoiceLeave(boolean isBot, boolean isEmpty, long guildID, long channelID) {
        if (isBot) return false;
        if (!isEmpty) return false;
        if (!createdChannels.contains(channelID)) return false;
        try {
            return MySQLConnector.isVoiceGenerator(guildID, channelID);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void createVoice(Guild guild, Member member, Category category) throws SQLException {
        String voiceName;
        int voiceLimit;
        ArrayList<String> voiceAllowedsIDS;
        ArrayList<String> voiceDeniedsIDS;
        ArrayList<Member> voiceAlloweds;
        ArrayList<Member> voiceDenieds;

        UserInfo settings = MySQLConnector.getUserSettings(member.getIdLong());

        if (settings == null) {
            voiceName = member.getUser().getName();
            voiceLimit = 0;
            voiceAllowedsIDS = null;
            voiceDeniedsIDS = null;
            defaultSettings(member);
        } else {
            voiceName = settings.NAME;
            voiceLimit = settings.MAX_ENTRY;
            voiceAllowedsIDS = new ArrayList<>(Arrays.asList(settings.ALLOWEDS.split(" ")));
            voiceDeniedsIDS = new ArrayList<>(Arrays.asList(settings.DENIEDS.split(" ")));
        }
        voiceAlloweds = Funcs.getMembers(guild, voiceAllowedsIDS);
        voiceDenieds = Funcs.getMembers(guild, voiceDeniedsIDS);

        try {
            guild.createVoiceChannel(voiceName)
                    .setParent(category)
                    .addPermissionOverride(member, EnumSet.of(Permission.VIEW_CHANNEL,
                            Permission.MANAGE_CHANNEL, Permission.VOICE_CONNECT), null)
                    .setUserlimit(voiceLimit).queue(voice -> {
                        for (Member allowed : voiceAlloweds) {
                            voice.getManager().putPermissionOverride(allowed,
                                    EnumSet.of(Permission.VIEW_CHANNEL, Permission.VOICE_CONNECT), null).queue();
                        }
                        for (Member denied : voiceDenieds) {
                            voice.getManager().putPermissionOverride(denied, null,
                                    EnumSet.of(Permission.VIEW_CHANNEL, Permission.VOICE_CONNECT)).queue();
                        }
                        guild.moveVoiceMember(member, voice).queue();
                        createdChannels.add(voice.getIdLong());
                    });
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onGuildVoiceJoin(@NotNull GuildVoiceJoinEvent event){
        boolean isBot = event.getMember().getUser().isBot();
        long guildID = event.getGuild().getIdLong();
        long channelID = event.getChannelJoined().getIdLong();
        if (onVoiceJoin(isBot, guildID, channelID)) return;

        Category category = event.getChannelJoined().getParent();
        Guild guild = event.getGuild();
        Member member = event.getMember();
        try {
            createVoice(guild, member, category);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onGuildVoiceLeave(@NotNull GuildVoiceLeaveEvent event) {
        boolean isBot = event.getMember().getUser().isBot();
        boolean isEmpty = event.getChannelLeft().getMembers().isEmpty();
        long guildID = event.getGuild().getIdLong();
        long channelID = event.getChannelLeft().getIdLong();
        if (onVoiceLeave(isBot, isEmpty, guildID, channelID)) event.getChannelLeft().delete().queue();
    }

    @Override
    public void onGuildVoiceMove(@NotNull GuildVoiceMoveEvent event) {
        boolean isBot = event.getMember().getUser().isBot();
        boolean isEmpty = event.getChannelLeft().getMembers().isEmpty();
        long guildID = event.getGuild().getIdLong();
        long leftID = event.getChannelLeft().getIdLong();
        long joinID = event.getChannelJoined().getIdLong();
        if (onVoiceLeave(isBot, isEmpty, guildID, leftID)) event.getChannelLeft().delete().queue();
        if (onVoiceJoin(isBot, guildID, joinID)) return;

        Category category = event.getChannelJoined().getParent();
        Guild guild = event.getGuild();
        Member member = event.getMember();
        try {
            createVoice(guild, member, category);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static void defaultSettings(Member member) throws SQLException{
        MySQLConnector.setUserSettings(member.getIdLong(), member.getUser().getName(), 0, "", "");
    }

    public static void defaultSettings(Member member, String name, int maxEntry) throws SQLException{
        MySQLConnector.setUserSettings(member.getIdLong(), name, maxEntry, "", "");
    }

    public static void defaultSettings(Member member, String name, int maxEntry, String alloweds, String denieds) throws SQLException{
        MySQLConnector.setUserSettings(member.getIdLong(), name, maxEntry, alloweds, denieds);
    }
}
