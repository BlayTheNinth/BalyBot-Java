package net.blay09.balybot;

import java.sql.*;

public class Database {

    private Connection connection;
    public PreparedStatement stmtRegisterCommand;
    public PreparedStatement stmtUnregisterCommand;
    public PreparedStatement stmtRegisterRegular;
    public PreparedStatement stmtUnregisterRegular;
    private PreparedStatement stmtSetConfigOption;
    private PreparedStatement stmtAddToChannel;
    private PreparedStatement stmtRemoveFromChannel;

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
        stmt.executeUpdate("CREATE TABLE IF NOT EXISTS config (channel_name VARCHAR(64) NOT NULL, config_name VARCHAR(32) NOT NULL, config_value TEXT NOT NULL, PRIMARY KEY (channel_name, config_name))");
        stmt.close();

        stmt = connection.createStatement();
        stmt.executeUpdate("CREATE TABLE IF NOT EXISTS channels (channel_name VARCHAR(64) PRIMARY KEY NOT NULL)");
        stmt.close();

        stmt = connection.createStatement();
        stmt.executeUpdate("CREATE TABLE IF NOT EXISTS timed_commands (channel_name VARCHAR(64) NOT NULL, command VARCHAR(32) NOT NULL, time_interval INTEGER(4), PRIMARY KEY(channel_name, command))");
        stmt.close();

        stmt = connection.createStatement();
        stmt.executeUpdate("CREATE TABLE IF NOT EXISTS modules (channel_name VARCHAR(64) NOT NULL, module_name VARCHAR(32) NOT NULL, PRIMARY KEY(channel_name, module_name))");
        stmt.close();
    }

    public void prepareStatements() throws SQLException {
        stmtRegisterCommand = connection.prepareStatement("INSERT INTO commands (channel_name, command_name, regex, message, userLevel) VALUES (?, ?, ?, ?, ?)", PreparedStatement.RETURN_GENERATED_KEYS);
        stmtUnregisterCommand = connection.prepareStatement("DELETE FROM commands WHERE channel_name = ? AND command_name = ?");

        stmtRegisterRegular = connection.prepareStatement("INSERT INTO regulars (channel_name, username) VALUES (?, ?)");
        stmtUnregisterRegular = connection.prepareStatement("DELETE FROM regulars WHERE channel_name = ? AND username = ?");

        stmtSetConfigOption = connection.prepareStatement("INSERT OR REPLACE INTO config (channel_name, config_name, config_value) VALUES (?, ?, ?)");

        stmtAddToChannel = connection.prepareStatement("INSERT OR REPLACE INTO channels (channel_name) VALUES (?)");
        stmtRemoveFromChannel = connection.prepareStatement("DELETE FROM channels WHERE channel_name = ?");
    }

    public Statement createStatement() throws SQLException {
        return connection.createStatement();
    }

    public void setConfigOption(String channel, String name, String value) {
        try {
            stmtSetConfigOption.setString(1, channel);
            stmtSetConfigOption.setString(2, name);
            stmtSetConfigOption.setString(3, value);
            stmtSetConfigOption.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void addToChannel(String channelName) {
        try {
            stmtAddToChannel.setString(1, channelName);
            stmtAddToChannel.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void removeFromChannel(String channelName) {
        try {
            stmtRemoveFromChannel.setString(1, channelName);
            stmtRemoveFromChannel.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
