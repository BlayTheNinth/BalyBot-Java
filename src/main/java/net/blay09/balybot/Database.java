package net.blay09.balybot;

import net.blay09.balybot.irc.IRCChannel;

import java.sql.*;

public class Database {

    private Connection connection;
    public PreparedStatement stmtRegisterCommand;
    public PreparedStatement stmtUnregisterCommand;
    public PreparedStatement stmtRegisterRegular;
    public PreparedStatement stmtUnregisterRegular;
    private PreparedStatement stmtSetConfigOption;

    public Database(String databasePath) {
        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:" + databasePath);
            initialSetup();
            prepareStatements();
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void initialSetup() throws SQLException {
        Statement stmt = connection.createStatement();
        stmt.executeUpdate("CREATE TABLE IF NOT EXISTS commands (id INTEGER PRIMARY KEY AUTOINCREMENT, channel_name VARCHAR(64) NOT NULL, command_name VARCHAR(32) NOT NULL, regex VARCHAR(128) NOT NULL, message TEXT, userLevel INTEGER(4))");
        stmt.close();

        stmt = connection.createStatement();
        stmt.executeUpdate("CREATE TABLE IF NOT EXISTS regulars (id INTEGER PRIMARY KEY AUTOINCREMENT, channel_name VARCHAR(64) NOT NULL, username VARCHAR(64) NOT NULL)");
        stmt.close();

        stmt = connection.createStatement();
        stmt.executeUpdate("CREATE TABLE IF NOT EXISTS config (channel_name VARCHAR(64)  PRIMARY KEY NOT NULL, config_name VARCHAR(32)  PRIMARY KEY NOT NULL, config_value VARCHAR(64)  PRIMARY KEY NOT NULL)");
        stmt.close();
    }

    public void prepareStatements() throws SQLException {
        stmtRegisterCommand = connection.prepareStatement("INSERT INTO commands (channel_name, command_name, regex, message, userLevel) VALUES (?, ?, ?, ?, ?)");
        stmtUnregisterCommand = connection.prepareStatement("DELETE FROM commands WHERE channel_name = ? AND command_name = ?");

        stmtRegisterRegular = connection.prepareStatement("INSERT INTO regulars (channel_name, username) VALUES (?, ?)");
        stmtUnregisterRegular = connection.prepareStatement("DELETE FROM regulars WHERE channel_name = ? AND username = ?");

        stmtSetConfigOption = connection.prepareStatement("INSERT OR REPLACE INTO config (channel_name, config_name, config_value) VALUES (?, ?, ?)");
    }

    public Statement createStatement() throws SQLException {
        return connection.createStatement();
    }

    public void setConfigOption(IRCChannel channel, String name, String value) {
        try {
            stmtSetConfigOption.setString(1, channel != null ? channel.getName() : "*");
            stmtSetConfigOption.setString(2, name);
            stmtSetConfigOption.setString(3, value);
            stmtSetConfigOption.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
