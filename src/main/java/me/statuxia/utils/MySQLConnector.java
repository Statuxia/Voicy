package me.statuxia.utils;


import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;


public class MySQLConnector {
    public static final String UPDATE = "UPDATE channels SET ID = (?) WHERE GUILD = (?)";
    public static final String INSERT = "INSERT INTO channels (ID, GUILD) VALUES (?, ?)";
    public static final String[] COMMANDS = {UPDATE, INSERT};

    public static void newChannelInserter(long GuildID, long ChannelID) throws SQLException {
        Connection connection = DriverManager.getConnection(Config.DATABASE_HOST,
                Config.DATABASE_USER, Config.DATABASE_PASS);

        for (String command : COMMANDS) {
            PreparedStatement preparedStmt = connection.prepareStatement(command);
            preparedStmt.setLong(1, ChannelID);
            preparedStmt.setLong(2, GuildID);
            if (preparedStmt.executeUpdate() != 0) break;
        }

        connection.close();
    }

    // TO DO
    //  Make method for getting settings for users.
    //  Make method for editing setting by users.
    //  Make method for editing setting by admins.
    //  Make method for editing settings of generator channel.
    //  Make method for deleting generator channel.
}

