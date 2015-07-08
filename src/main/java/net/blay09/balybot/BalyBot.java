package net.blay09.balybot;

import com.google.common.eventbus.EventBus;
import net.blay09.balybot.irc.IRCConfig;
import net.blay09.balybot.irc.IRCConnection;
import net.blay09.balybot.command.CommandHandler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class BalyBot {

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
                        instance.connection.join(s.substring(6), null);
                    } else if(s.startsWith("/part ")) {
                        instance.connection.part(s.substring(6));
                    } else if(s.startsWith("/quote ")) {
                        instance.connection.irc(s.substring(7));
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private final Database database;
    private final EventBus eventBus;
    private final CommandHandler commandHandler;
    private IRCConnection connection;


    public BalyBot() {
        database = new Database("balybot.db");
        eventBus = new EventBus();

        commandHandler = new CommandHandler(database);
        eventBus.register(commandHandler);

        load();

        if(Config.hasOption(null, "username") && Config.hasOption(null, "oauth")) {
            start();
        } else {
            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
            try {
                System.out.println("Initial BalyBot setup...");

                String username = null;
                while(username == null || username.isEmpty()) {
                    System.out.print("Enter username: ");
                    username = reader.readLine().trim();
                }

                String oauth = null;
                while(oauth == null || oauth.isEmpty()) {
                    System.out.print("Enter oauth token: ");
                    oauth = reader.readLine();
                    if(!oauth.startsWith("oauth:")) {
                        System.out.println("Invalid oauth token. It should start with 'oauth:'.");
                        oauth = null;
                    }
                }

                database.setConfigOption(null, "username", username);
                database.setConfigOption(null, "oauth", oauth);
                Config.load(database);

                System.out.println("Setup complete! Use /join <channel> to join a channel.");

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

    public void start() {
        IRCConfig config = new IRCConfig();
        config.host = "irc.twitch.tv";
        config.ident = Config.getValue(null, "username");
        config.realName = "BalyBot v0.1.0";
        config.serverPassword = Config.getValue(null, "oauth");
        connection = new IRCConnection(config, Config.getValue(null, "username"), eventBus);
        connection.start();
    }

    public Database getDatabase() {
        return database;
    }

    public IRCConnection getConnection() {
        return connection;
    }

    public CommandHandler getCommandHandler() {
        return commandHandler;
    }


}
