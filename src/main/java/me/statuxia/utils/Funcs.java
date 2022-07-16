package me.statuxia.utils;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;

import java.util.ArrayList;

public class Funcs {

    public static ArrayList<Member> getMembers(Guild guild, ArrayList<String> IDS) {
        ArrayList<Member> members = new ArrayList<>();
        if (IDS == null)
            return members;

        for (String id : IDS) {
            if (id.equals(" ") || id.equals(""))
                continue;
            try {
                Member member = guild.getMemberById(id);
                if (member == null)
                    continue;
                members.add(members.size(), member);
            } catch (Exception ignored) {
            }
        }
        return members;
    }
}
