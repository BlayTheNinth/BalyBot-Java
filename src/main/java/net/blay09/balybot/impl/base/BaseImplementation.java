package net.blay09.balybot.impl.base;

import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import net.blay09.balybot.BalyBot;
import net.blay09.balybot.BotProperties;
import net.blay09.balybot.ChannelManager;
import net.blay09.balybot.Database;
import net.blay09.balybot.ServerManager;
import net.blay09.balybot.impl.ExpressionLibrary;
import net.blay09.balybot.impl.UserLevelRegistry;
import net.blay09.balybot.impl.api.ChatProvider;
import net.blay09.balybot.impl.api.BotImplementation;
import net.blay09.balybot.impl.base.script.BalyBotBinding;
import net.blay09.balybot.impl.base.script.DateBinding;
import net.blay09.balybot.impl.base.script.ErrorBinding;
import net.blay09.balybot.impl.base.script.StringBinding;
import net.blay09.balybot.impl.base.script.StringExpressions;
import net.blay09.balybot.impl.base.script.SystemBinding;

import javax.script.Bindings;

@Log4j2
public class BaseImplementation implements BotImplementation {

	public static final int COMMAND_COOLDOWN_DEFAULT = 30;

	@Getter private static Database.Type databaseType;
	@Getter private static int commandCooldown;
	@Getter private static String databaseHost;
	@Getter private static String databaseName;
	@Getter private static String databaseUser;
	@Getter private static String databasePassword;

	@Override
	public String getId() {
		return "base";
	}

	@Override
	public void registerProperties(BotProperties properties) {
		properties.setProperty("database-type", "SQLITE");
		properties.setProperty("database-host", "");
		properties.setProperty("database-name", "balybot.db");
		properties.setProperty("database-user", "");
		properties.setProperty("database-password", "");

		properties.setProperty("command-cooldown", String.valueOf(COMMAND_COOLDOWN_DEFAULT));
	}

	@Override
	public void loadProperties(BotProperties properties) {
		try {
			databaseType = Database.Type.valueOf(properties.getProperty("database-type", "SQLITE"));
		} catch (IllegalArgumentException e) {
			throw new RuntimeException(e);
		}

		databaseHost = properties.getProperty("database-host", "");
		databaseName = properties.getProperty("database-name", "balybot.db");
		databaseUser = properties.getProperty("database-user", "");
		databasePassword = properties.getProperty("database-password", "");

		commandCooldown = properties.getProperty("command-cooldown", COMMAND_COOLDOWN_DEFAULT);
	}

	@Override
	public void registerUserLevels(UserLevelRegistry registry) {
		registry.register(DefaultUserLevels.SUPER_USER);
		registry.register(DefaultUserLevels.ALL);
	}

	@Override
	public void registerExpressions(ExpressionLibrary library) {
		library.registerGlobalStaticClass(Math.class);
		library.registerGlobalStaticClass(StringExpressions.class);
		library.markStateDependent("random");
	}

	@Override
	public void registerBindings(Bindings bindings) {
		bindings.put("JSystem", new SystemBinding());
		bindings.put("JString", new StringBinding());
		bindings.put("JDate", new DateBinding());
		bindings.put("JError", new ErrorBinding());
		bindings.put("JBalyBot", new BalyBotBinding());
	}

	@Override
	public ChatProvider getChatProvider() {
		return null;
	}

	@Override
	public boolean handleCommandLine(String cmd) {
		if(cmd.equals("reload-scripts")) {
			BalyBot.getInstance().loadModules();
			ServerManager.loadModules();
			ChannelManager.loadModules();
			log.info("Scripts reloaded.");
		}
		return false;
	}

	@Override
	public void start() {
	}

	@Override
	public void stop() {
	}

}
