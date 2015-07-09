package net.blay09.balybot.module.regulars;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import net.blay09.balybot.BalyBot;
import net.blay09.balybot.Database;
import net.blay09.balybot.irc.IRCChannel;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class Regulars {

    private static final Multimap<String, String> regulars = ArrayListMultimap.create();

    public static void load(Database database) {
        try {
            Statement stmt = database.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM regulars");
            while(rs.next()) {
                regulars.put(rs.getString("channel_name"), rs.getString("username"));
            }
            rs.close();
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static boolean registerRegular(IRCChannel channel, String username) {
        try {
            PreparedStatement stmtRegisterRegular = BalyBot.instance.getDatabase().stmtRegisterRegular;
            stmtRegisterRegular.setString(1, channel.getName());
            stmtRegisterRegular.setString(2, username);
            stmtRegisterRegular.executeUpdate();
            regulars.put(channel.getName(), username);
            return true;
        } catch (SQLException e) {
            return false;
        }
    }

    public static boolean unregisterRegular(IRCChannel channel, String username) {
        try {
            PreparedStatement stmtUnregisterRegular = BalyBot.instance.getDatabase().stmtUnregisterRegular;
            stmtUnregisterRegular.setString(1, channel.getName());
            stmtUnregisterRegular.setString(2, username);
            stmtUnregisterRegular.executeUpdate();
            regulars.remove(channel.getName(), username);
            return true;
        } catch (SQLException e) {
            return false;
        }
    }

    public static boolean isRegular(IRCChannel channel, String username) {
        return regulars.containsEntry(channel.getName(), username);
    }
}
