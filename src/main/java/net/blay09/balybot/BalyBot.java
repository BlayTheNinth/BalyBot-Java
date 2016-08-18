package net.blay09.balybot;

import com.google.common.collect.*;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import net.blay09.balybot.module.ModuleDef;
import net.blay09.balybot.module.commands.CommandsModule;
import net.blay09.balybot.module.manager.ManagerModule;
import net.blay09.balybot.script.ScriptManager;
import net.blay09.javatmi.TMIClient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

@Log4j2
public class BalyBot {

    private static final String VERSION = "0.1.0";

    @Getter
    private static BalyBot instance;

	public static boolean SIMULATED = false;

    public static void main(String[] args) {
        instance = new BalyBot();
        instance.start();
        CommandLineHandler.handleCommands();
    }

    @Getter
    private TMIClient client;

    private final Map<String, ModuleDef> availableModules = Maps.newHashMap();
	@Getter
	private final BalyBotListener listener = new BalyBotListener();

    public void start() {
        log.info("Loading BalyBot {}...", VERSION);
        preInit();
        init();
        postInit();
    }

    private void preInit() {
		Config.loadFromFile();

        log.info("Connecting to database...");
		try {
			Database.setup();
		} catch (SQLException | ClassNotFoundException e) {
			throw new RuntimeException(e);
		}

		log.info("Loading manager module...");
        ManagerModule managerModule = new ManagerModule();
        availableModules.put(managerModule.getId(), managerModule);
		CommandsModule commandsModule = new CommandsModule();
		availableModules.put(commandsModule.getId(), commandsModule);

        log.info("Scanning for script modules...");
        for(ModuleDef moduleDef : ScriptManager.getInstance().loadModules()) {
            availableModules.put(moduleDef.getId(), moduleDef);
        }

		log.info("Loading channels...");
		ChannelManager.loadChannels();
    }

    private void init() {
        log.info("Loading configuration from database...");
        Config.loadFromDatabase();

        log.info("Initializing modules...");
		ChannelManager.loadModules();
	}

    private void postInit() {
        if(Config.hasGlobalValue("username") && Config.hasGlobalValue("oauth")) {
			log.info("BalyBot is now running.");
            connect();
        } else {
            setup();
        }

        DocBuilder.rebuildAllDocs();
    }

    private void setup() {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        try {
            log.info("BalyBot is not configured yet, but no worries, it's simple!");

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

			try {
				Database.dbReplaceConfig("*", "username", username);
				Database.dbReplaceConfig("*", "oauth", oauth);
				Database.dbReplaceConfig("*", "docs_dir", "docs");
			} catch (SQLException e) {
				e.printStackTrace();
			}
            Config.loadFromDatabase();

            log.info("Setup complete! Use /join <channel> to join a channel.");

            connect();
        } catch (IOException e) {
            log.error(e);
        }
    }

    private void connect() {
        client = new TMIClient(Config.getGlobalString("username", null), Config.getGlobalString("oauth", null), Collections.emptyList(), listener);
		if(!SIMULATED) {
			client.connect();
		}
    }

    public Collection<ModuleDef> getAvailableModules() {
        return availableModules.values();
    }

	public ModuleDef getModuleDef(String moduleId) {
		return availableModules.get(moduleId);
	}
}
