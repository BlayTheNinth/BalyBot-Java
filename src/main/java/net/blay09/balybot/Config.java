package net.blay09.balybot;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import net.blay09.balybot.irc.IRCChannel;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class Config {

    private static final Table<String, String, String> config = HashBasedTable.create();

    public static void load(Database database) {
        config.clear();
        try {
            Statement stmt = database.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM config");
            while(rs.next()) {
                config.put(rs.getString("channel_name"), rs.getString("config_name"), rs.getString("config_value"));
            }
            rs.close();
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static String getValue(IRCChannel channel, String name) {
        if(!config.contains(channel != null ? channel.getName() : "*", name)) {
            throw new RuntimeException("Required config option " + name + " but it's missing and has no default value.");
        }
        return config.get(channel != null ? channel.getName() : "*", name);
    }

    public static String getValue(IRCChannel channel, String name, String defaultVal) {
        String value = config.get(channel != null ? channel.getName() : "*", name);
        if(value == null) {
            return defaultVal;
        }
        return value;
    }

    public static boolean hasOption(IRCChannel channel, String name) {
        return config.contains(channel != null ? channel.getName() : "*", name);
    }
}
