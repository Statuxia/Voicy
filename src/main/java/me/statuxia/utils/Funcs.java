package me.statuxia.utils;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;

import java.sql.*;
import java.util.ArrayList;

public class Funcs {

    public static ArrayList<Member> getMembers(Guild guild, ArrayList<String> IDS) {
        ArrayList<Member> members = new ArrayList<>();
        if (IDS == null) return members;
        for (String id : IDS) {
            if (id.equals(" ") || id.equals("")) continue;
            try {
                Member member = guild.getMemberById(id);
                if (member == null) continue;
                members.add(members.size(), member);
            } catch (Exception ignored) {
            }
        }
        return members;
    }

    public static PreparedStatement makeUserStatement(ResultSet line) throws SQLException {
        Connection connection = DriverManager.getConnection(Config.DATABASE_HOST,
                Config.DATABASE_USER, Config.DATABASE_PASS);

        PreparedStatement preparedStmt = connection.prepareStatement(MySQLConnector.SELECT_USERS);
        preparedStmt.setLong(1, line.getLong(1));
        preparedStmt.setString(2, line.getString(2));
        preparedStmt.setInt(1, line.getInt(3));
        preparedStmt.setString(2, line.getString(4));
        preparedStmt.setString(2, line.getString(5));
        return preparedStmt;
    }
}
