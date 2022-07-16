package me.statuxia;

import me.statuxia.commands.Admin;
import me.statuxia.commands.Everyone;
import me.statuxia.utils.Config;
import me.statuxia.voice.Voice;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import javax.security.auth.login.LoginException;


public class Main extends ListenerAdapter {

    public static JDA jda;

    public static User bot;

    public static void main(String[] args)
            throws LoginException {
        jda = JDABuilder.createDefault(Config.TOKEN)
                .setActivity(Activity.watching(">help"))
                .setStatus(OnlineStatus.ONLINE)
                .addEventListeners(new Admin())
                .addEventListeners(new Voice())
                .addEventListeners(new Everyone())
                .build();
        bot = jda.getSelfUser();
    }

}