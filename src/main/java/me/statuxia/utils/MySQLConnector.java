package me.statuxia.utils;


import lombok.SneakyThrows;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class MySQLConnector {
    protected static final String SELECT_USERS = "SELECT * FROM users WHERE USER = (?)";
    private static final String UPDATE_USERS = "UPDATE users SET USER = (?), NAME = (?), MAX_ENTRY = (?), ALLOWEDS = (?), DENIEDS = (?) WHERE USER = (?)";
    private static final String INSERT_USERS = "INSERT INTO users (USER, NAME, MAX_ENTRY, ALLOWEDS, DENIEDS) VALUES (?, ?, ?, ?, ?)";
    private static final String SELECT_CHANNELS = "SELECT * FROM channels WHERE GUILD = (?)";
    private static final String UPDATE_CHANNELS = "UPDATE channels SET ID = (?) WHERE GUILD = (?)";
    private static final String INSERT_CHANNELS = "INSERT INTO channels (ID, GUILD) VALUES (?, ?)";
    private static final String[] COMMANDS_CHANNELS = {UPDATE_CHANNELS, INSERT_CHANNELS};
    private static final String[] COMMANDS_USERS = {UPDATE_USERS, INSERT_USERS};


    @SneakyThrows
    public static void newGenerator(long guildID, long channelID) {
        Connection connection = DriverManager.getConnection(Config.DATABASE_HOST,
                Config.DATABASE_USER, Config.DATABASE_PASS);

        for (String command : COMMANDS_CHANNELS) {
            PreparedStatement statement = connection.prepareStatement(command);
            statement.setLong(1, channelID);
            statement.setLong(2, guildID);
            if (statement.executeUpdate() != 0)
                break;
        }
        connection.close();
    }

    @SneakyThrows
    public static boolean isNotGenerator(long guildID, long channelID) {
        Connection connection = DriverManager.getConnection(Config.DATABASE_HOST,
                Config.DATABASE_USER, Config.DATABASE_PASS);

        PreparedStatement statement = connection.prepareStatement(SELECT_CHANNELS);
        statement.setLong(1, guildID);
        ResultSet line = statement.executeQuery();

        while (line.next()) {
            if (line.getLong(2) == channelID) {
                connection.close();
                return false;
            }
        }
        connection.close();
        return true;
    }

    @SneakyThrows
    public static long getGenerator(long guildID) {
        Connection connection = DriverManager.getConnection(Config.DATABASE_HOST,
                Config.DATABASE_USER, Config.DATABASE_PASS);

        PreparedStatement statement = connection.prepareStatement(SELECT_CHANNELS);
        statement.setLong(1, guildID);
        ResultSet line = statement.executeQuery();

        if (line.next()) {
            long channelID = line.getLong(2);
            connection.close();
            return channelID;
        }
        return 0L;
    }

    @SneakyThrows
    public static void getUserSettings(long memberID) {
        Connection connection = DriverManager.getConnection(Config.DATABASE_HOST,
                Config.DATABASE_USER, Config.DATABASE_PASS);

        PreparedStatement statement = connection.prepareStatement(SELECT_USERS);
        statement.setLong(1, memberID);
        ResultSet line = statement.executeQuery();

        while (line.next()) {
            if (line.getLong(1) == memberID) {
                long user = line.getLong(1);
                String name = line.getString(2);
                int maxEntries = line.getInt(3);
                String alloweds = line.getString(4);
                String denieds = line.getString(5);
                new UserInfo(user, name, maxEntries, alloweds, denieds);
                connection.close();
                break;
            }
        }
        connection.close();
    }

    @SneakyThrows
    public static void setUserSettings(long memberID, String name, int maxEntry, String alloweds, String denieds) {
        Connection connection = DriverManager.getConnection(Config.DATABASE_HOST,
                Config.DATABASE_USER, Config.DATABASE_PASS);

        for (String command : COMMANDS_USERS) {
            PreparedStatement statement = connection.prepareStatement(command);
            statement.setLong(1, memberID);
            statement.setString(2, name);
            statement.setInt(3, maxEntry);
            statement.setString(4, alloweds);
            statement.setString(5, denieds);
            if (command.equals(COMMANDS_USERS[0]))
                statement.setLong(6, memberID);
            if (statement.executeUpdate() != 0)
                break;
        }
        connection.close();
    }

    public static boolean setVoiceName(long memberID, String name) {
        if (!(name.length() > 0 && name.length() < 101))
            return false;

        getUserSettings(memberID);
        UserInfo info = UserInfo.get(memberID);
        if (info == null)
            return false;

        setUserSettings(memberID, name, info.getMaxEntry(), info.getAlloweds(), info.getDenieds());
        return true;
    }

    public static boolean setMaxEntry(long memberID, int limit) {
        if (!(0 <= limit && limit <= 99))
            return false;

        getUserSettings(memberID);
        UserInfo info = UserInfo.get(memberID);
        if (info == null) return false;

        setUserSettings(memberID, info.getVoiceName(), limit, info.getAlloweds(), info.getDenieds());
        return true;
    }
}

