package net.blay09.balybot;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import net.blay09.balybot.irc.IRCConfig;
import net.blay09.balybot.irc.IRCConnection;
import net.blay09.balybot.irc.event.IRCConnectEvent;
import net.blay09.balybot.module.Module;
import net.blay09.balybot.module.calc.ModuleMath;
import net.blay09.balybot.module.ccpoll.CountedChatPollBotCommand;
import net.blay09.balybot.module.hostnotifier.ModuleHostNotifier;
import net.blay09.balybot.module.linkfilter.ModuleLinkFilter;
import net.blay09.balybot.module.ccpoll.ModuleCountedChatPoll;
import net.blay09.balybot.module.manager.ModuleManager;
import net.blay09.balybot.module.raffle.ModuleRaffle;
import net.blay09.balybot.module.song.ModuleSong;
import net.blay09.balybot.module.time.ModuleTime;
import net.blay09.balybot.module.timer.TimerHandler;
import net.blay09.balybot.module.uptime.ModuleUptime;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class BalyBot {

    private static final String VERSION = "0.1.0";
    private static final Logger logger = LogManager.getLogger();

    public static BalyBot instance;

    public static void main(String[] args) {
        instance = new BalyBot();
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        while(true) {
            try {
                String s = reader.readLine();
                if(s != null) {
                    if (s.equals("/quit")) {
                        instance.connection.disconnect("Bot stopped.");
                        break;
                    } else if (s.startsWith("/join ")) {
                        String channelName = s.substring(6);
                        instance.connection.join(channelName, null);
                        instance.database.addToChannel(channelName);
                    } else if(s.startsWith("/part ")) {
                        String channelName = s.substring(6);
                        instance.connection.part(channelName);
                        instance.database.removeFromChannel(channelName);
                    } else if(s.startsWith("/quote ")) {
                        instance.connection.irc(s.substring(7));
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
                logger.error(e);
            }
        }
        TimerHandler.stop();
    }

    private final Database database;
    private final EventBus eventBus;
    private IRCConnection connection;
    private IRCConnection groupConnection;

    public BalyBot() {
        logger.info("Loading BalyBot {0}...", VERSION);
        database = new Database("balybot.db");
        eventBus = new EventBus();

        Module.registerModule("manager", ModuleManager.class);
        Module.registerModule("linkfilter", ModuleLinkFilter.class);
        Module.registerModule("math", ModuleMath.class);
        Module.registerModule("ccp", ModuleCountedChatPoll.class);
        Module.registerModule("song", ModuleSong.class);
        Module.registerModule("time", ModuleTime.class);
        Module.registerModule("hostnotifier", ModuleHostNotifier.class);
        Module.registerModule("uptime", ModuleUptime.class);
        Module.registerModule("raffle", ModuleRaffle.class);

        Module.load(database, eventBus);
        CommandHandler.load(database, eventBus);
        TimerHandler.load(database, eventBus);
        eventBus.register(this);

        load();

        if(Config.hasOption("*", "username") && Config.hasOption("*", "oauth")) {
            start();
        } else {
            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
            try {
                logger.info("BalyBot is not configured yet, but no worries, it's simple!");

                String username = null;
                while(username == null || username.isEmpty()) {
                    System.out.print("Enter Twitch username: ");
                    username = reader.readLine().trim();
                }

                String oauth = null;
                while(oauth == null || oauth.isEmpty()) {
                    System.out.print("Enter Twitch oauth token (see http://twitchapps.com/tmi/): ");
                    oauth = reader.readLine();
                    if(!oauth.startsWith("oauth:")) {
                        System.out.println("Invalid oauth token. It should start with 'oauth:'.");
                        oauth = null;
                    }
                }

                database.setConfigOption("*", "username", username);
                database.setConfigOption("*", "oauth", oauth);
                Config.load(database);

                logger.info("Setup complete! Use /join <channel> to join a channel.");

                start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void load() {
        Config.load(database);
        Regulars.load(database);
    }

    @Subscribe
    @SuppressWarnings("unused")
    public void onConnected(IRCConnectEvent event) {
        event.connection.irc("CAP REQ :twitch.tv/membership");
        event.connection.irc("CAP REQ :twitch.tv/tags");
        event.connection.irc("CAP REQ :twitch.tv/commands");

        if(event.connection != connection) {
            return;
        }

        try {
            Statement stmt = database.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM channels");
            while(rs.next()) {
                event.connection.join(rs.getString("channel_name"), null);
            }
            rs.close();
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void start() {
        IRCConfig config = new IRCConfig();
        config.host = "irc.twitch.tv";
        config.ident = Config.getValue("*", "username");
        config.realName = "BalyBot v" + VERSION;
        config.serverPassword = Config.getValue("*", "oauth");
        connection = new IRCConnection(config, Config.getValue("*", "username"), eventBus);
        connection.start();

        IRCConfig groupConfig = new IRCConfig();
        groupConfig.host = Config.getValue("*", "groupServer");
        groupConfig.ident = Config.getValue("*", "username");
        groupConfig.realName = "BalyBot v" + VERSION;
        groupConfig.serverPassword = Config.getValue("*", "oauth");
        groupConnection = new IRCConnection(groupConfig, Config.getValue("*", "username"), eventBus);
        groupConnection.start();
    }

    public Database getDatabase() {
        return database;
    }

    public IRCConnection getConnection() {
        return connection;
    }

    public IRCConnection getGroupConnection() {
        return groupConnection;
    }
}
