package net.blay09.balybot;

import com.google.common.collect.*;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import net.blay09.balybot.impl.ExpressionLibrary;
import net.blay09.balybot.impl.api.BotImplementation;
import net.blay09.balybot.impl.base.BaseImplementation;
import net.blay09.balybot.impl.UserLevelRegistry;
import net.blay09.balybot.impl.twitch.TwitchImplementation;
import net.blay09.balybot.module.ModuleDef;
import net.blay09.balybot.module.commands.CommandsModule;
import net.blay09.balybot.module.manager.ManagerModule;
import net.blay09.balybot.script.ScriptManager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Map;

@Log4j2
public class BalyBot {

    private static final String VERSION = "3.0.0";
	public static boolean SIMULATED = false;

	@Getter private static BalyBot instance;
	public static void main(String[] args) {
		instance = new BalyBot();
		instance.start();
		BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
		while(true) {
			try {
				String s = reader.readLine();
				if(s != null) {
					if (s.equals("stop")) {
						BalyBot.getInstance().stop();
						break;
					} else {
						instance.handleCommandLine(s);
					}
				}
			} catch (IOException e) {
				log.error(e);
			}
			try {
				Thread.sleep(10);
			} catch (InterruptedException ignored) {}
		}
	}

	@Getter private static final BotProperties botProperties = new BotProperties();
	@Getter private static final ExpressionLibrary expressionLibrary = new ExpressionLibrary();
	@Getter private static final UserLevelRegistry userLevelRegistry = new UserLevelRegistry();

	private final Map<String, BotImplementation> implementations = Maps.newHashMap();
    private final Map<String, ModuleDef> availableModules = Maps.newHashMap();

    public void start() {
        log.info("Loading BalyBot {}...", VERSION);
        preInit();
        init();
        postInit();
    }

    private void preInit() {
		log.info("Registering bot implementations...");
		registerImplementation(new BaseImplementation());
		registerImplementation(new TwitchImplementation());
//		implementations.put("discord", new DiscordServerType());

		log.info("Loading properties...");
		if(botProperties.loadFromFile()) {
			for(BotImplementation impl : implementations.values()) {
				impl.loadProperties(botProperties);
			}
		} else {
			for(BotImplementation impl : implementations.values()) {
				impl.registerProperties(botProperties);
				impl.loadProperties(botProperties);
			}
		}
		botProperties.saveToFile();

		log.info("Initializing expression library...");
		for(BotImplementation impl : implementations.values()) {
			impl.registerExpressions(expressionLibrary);
		}

        log.info("Connecting to database...");
		try {
			Database.connect();
		} catch (SQLException | ClassNotFoundException e) {
			throw new RuntimeException(e);
		}

		loadModules();

		log.info("Loading servers...");
		ServerManager.loadServers();

		log.info("Loading channels...");
		ChannelManager.loadChannels();
    }

	private void init() {
        log.info("Loading channel configurations...");
        ChannelManager.loadConfig();

        log.info("Initializing modules...");
		ChannelManager.loadModules();
	}

    private void postInit() {
		log.info("Initializing bot implementations...");

		for(BotImplementation impl : implementations.values()) {
			impl.registerUserLevels(userLevelRegistry);
			impl.start();
		}

		log.info("BalyBot is now running.");
    }

	private void handleCommandLine(String cmd) {
		for(BotImplementation impl : implementations.values()) {
			impl.handleCommandLine(cmd);
		}
	}

	public void stop() {
		implementations.values().forEach(BotImplementation::stop);
	}

	private void registerImplementation(BotImplementation impl) {
		implementations.put(impl.getId(), impl);
	}

	public void loadModules() {
		availableModules.clear();
		log.info("Loading manager module...");
		ManagerModule managerModule = new ManagerModule();
		availableModules.put(managerModule.getId(), managerModule);
		CommandsModule commandsModule = new CommandsModule();
		availableModules.put(commandsModule.getId(), commandsModule);

		log.info("Scanning for script modules...");
		for(ModuleDef moduleDef : ScriptManager.getInstance().loadModules()) {
			availableModules.put(moduleDef.getId(), moduleDef);
		}
	}

    public Collection<ModuleDef> getAvailableModules() {
        return availableModules.values();
    }

	public ModuleDef getModuleDef(String moduleId) {
		return availableModules.get(moduleId);
	}

	public BotImplementation getImplementation(String id) {
		return implementations.get(id);
	}

	public Collection<BotImplementation> getImplementations() {
		return implementations.values();
	}
}
