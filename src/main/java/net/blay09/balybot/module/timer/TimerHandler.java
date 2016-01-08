package net.blay09.balybot.module.timer;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import net.blay09.balybot.BalyBot;
import net.blay09.balybot.CommandHandler;
import net.blay09.balybot.Database;
import net.blay09.balybot.irc.IRCConnection;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class TimerHandler implements Runnable {

    private static final Multimap<String, TimedCommand> timedCommands = ArrayListMultimap.create();
    private static boolean running;

    public static void load(Database database) {
        try {
            Statement stmt = database.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM timed_commands");
            while(rs.next()) {
                timedCommands.put(rs.getString("channel_name"), new TimedCommand(rs.getString("channel_name"), rs.getString("command"), rs.getInt("time_interval")));
            }
            rs.close();
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        Thread thread = new Thread(new TimerHandler());
        running = true;
        thread.start();
    }

    @Override
    public void run() {
        long lastTime = System.currentTimeMillis();
        while(running) {
            long nowTime = System.currentTimeMillis();
            long elapsedTime = nowTime - lastTime;
            for(TimedCommand command : timedCommands.values()) {
                command.timeSinceLast += elapsedTime;
                if(command.timeSinceLast > command.interval) {
                    IRCConnection connection = BalyBot.instance.getConnection();
                    CommandHandler.get(connection.getChannel(command.channelName)).handleCommand(connection.getChannel(command.channelName), connection.getBotUser(), command.command);
                    command.timeSinceLast = 0;
                }
            }
        }
    }

    public static void stop() {
        running = false;
    }
}
