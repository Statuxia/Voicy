package me.statuxia.utils;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.requests.RestAction;

import java.util.ArrayList;

public class Funcs {

    public static ArrayList<Member> getMembers(Guild guild, ArrayList<String> IDS) {
        ArrayList<Member> members = new ArrayList<>();
        if (IDS == null)
            return members;

        for (String id : IDS) {
            if (id.equals(" ") || id.equals(""))
                continue;

            RestAction<Member> member = guild.retrieveMemberById(id.strip());
            member.queue(member1 -> members.add(members.size(), member1), member2 -> {
            });
        }
        return members;
    }

    public static boolean isNotLong(String content) {

        String trash = content.replaceAll("\\D", "");
        return !trash.equals(content);
    }
}
