package me.statuxia.utils;


import java.sql.*;

public class MySQLConnector {
    public static final String SELECT_USERS = "SELECT * FROM users WHERE USER = (?)";
    public static final String UPDATE_USERS = "UPDATE users SET USER = (?), NAME = (?), MAX_ENTRY = (?), ALLOWEDS = (?), DENIEDS = (?) WHERE USER = (?)";
    public static final String INSERT_USERS = "INSERT INTO users (USER, NAME, MAX_ENTRY, ALLOWEDS, DENIEDS) VALUES (?, ?, ?, ?, ?)";
    public static final String SELECT_CHANNELS = "SELECT * FROM channels WHERE GUILD = (?)";
    public static final String UPDATE_CHANNELS = "UPDATE channels SET ID = (?) WHERE GUILD = (?)";
    public static final String INSERT_CHANNELS = "INSERT INTO channels (ID, GUILD) VALUES (?, ?)";
    public static final String[] COMMANDS_CHANNELS = {UPDATE_CHANNELS, INSERT_CHANNELS};
    public static final String[] COMMANDS_USERS = {UPDATE_USERS, INSERT_USERS};

    public static void newChannelInserter(long guildID, long channelID) throws SQLException {
        Connection connection = DriverManager.getConnection(Config.DATABASE_HOST,
                Config.DATABASE_USER, Config.DATABASE_PASS);

        for (String command : COMMANDS_CHANNELS) {
            PreparedStatement preparedStmt = connection.prepareStatement(command);
            preparedStmt.setLong(1, channelID);
            preparedStmt.setLong(2, guildID);
            if (preparedStmt.executeUpdate() != 0)
                break;
        }

        connection.close();
    }

    public static boolean isVoiceGenerator(long guildID, long channelID) throws SQLException {
        Connection connection = DriverManager.getConnection(Config.DATABASE_HOST,
                Config.DATABASE_USER, Config.DATABASE_PASS);

        PreparedStatement preparedStmt = connection.prepareStatement(SELECT_CHANNELS);
        preparedStmt.setLong(1, guildID);
        ResultSet line = preparedStmt.executeQuery();

        while (line.next()) {
            if (line.getLong(2) == channelID) {
                connection.close();
                return false;
            }
        }
        connection.close();
        return true;
    }

    public static long getChannelByGuildID(long guildID) throws SQLException {
        Connection connection = DriverManager.getConnection(Config.DATABASE_HOST,
                Config.DATABASE_USER, Config.DATABASE_PASS);

        PreparedStatement preparedStmt = connection.prepareStatement(SELECT_CHANNELS);
        preparedStmt.setLong(1, guildID);
        ResultSet line = preparedStmt.executeQuery();

        if (line.next()) {
            long channelID = line.getLong(2);
            connection.close();
            return channelID;
        }
        return 0L;
    }

//    @SneakyThrows lombok
    public static UserInfo getUserSettings(long memberID) throws SQLException {
        Connection connection = DriverManager.getConnection(Config.DATABASE_HOST,
                Config.DATABASE_USER, Config.DATABASE_PASS);

        PreparedStatement preparedStmt = connection.prepareStatement(SELECT_USERS);
        preparedStmt.setLong(1, memberID);
        ResultSet line = preparedStmt.executeQuery();

        while (line.next()) {
            if (line.getLong(1) == memberID) {
                long user = line.getLong(1);
                String name = line.getString(2);
                int maxEntries = line.getInt(3);
                String alloweds = line.getString(4);
                String denieds = line.getString(5);
                UserInfo info = new UserInfo(user, name, maxEntries, alloweds, denieds);
                connection.close();
                return info;
            }
        }
        connection.close();
        return null;
    }

    public static void setUserSettings(long memberID, String name, int maxEntry, String alloweds, String denieds) throws
            SQLException {
        Connection connection = DriverManager.getConnection(Config.DATABASE_HOST,
                Config.DATABASE_USER, Config.DATABASE_PASS);

        for (String command : COMMANDS_USERS) {
            PreparedStatement preparedStmt = connection.prepareStatement(command);
            preparedStmt.setLong(1, memberID);
            preparedStmt.setString(2, name);
            preparedStmt.setInt(3, maxEntry);
            preparedStmt.setString(4, alloweds);
            preparedStmt.setString(5, denieds);
            if (command.equals(COMMANDS_USERS[0])) preparedStmt.setLong(6, memberID);
            if (preparedStmt.executeUpdate() != 0) break;
        }
        connection.close();
    }

    public static boolean setName(long memberID, String name) throws SQLException {
        if (!(name.length() > 0 && name.length() < 101)) return false;

        UserInfo info = getUserSettings(memberID);
        if (info == null) return false;

        setUserSettings(info.USER, name, info.MAX_ENTRY, info.ALLOWEDS, info.DENIEDS);
        return true;
    }

    public static boolean setMaxEntry(long memberID, int limit) throws SQLException {
        if (!(0 <= limit && limit <= 99)) return false;

        UserInfo info = getUserSettings(memberID);
        if (info == null) return false;

        setUserSettings(info.USER, info.NAME, limit, info.ALLOWEDS, info.DENIEDS);
        return true;
    }
}

