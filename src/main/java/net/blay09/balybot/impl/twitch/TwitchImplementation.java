package net.blay09.balybot.impl.twitch;

import com.google.common.collect.Maps;
import lombok.extern.log4j.Log4j2;
import net.blay09.balybot.BalyBot;
import net.blay09.balybot.BotProperties;
import net.blay09.balybot.ChannelManager;
import net.blay09.balybot.Database;
import net.blay09.balybot.ServerManager;
import net.blay09.balybot.impl.ExpressionLibrary;
import net.blay09.balybot.impl.UserLevelRegistry;
import net.blay09.balybot.impl.api.Channel;
import net.blay09.balybot.impl.api.ChatProvider;
import net.blay09.balybot.impl.api.BotImplementation;
import net.blay09.balybot.impl.api.Server;
import net.blay09.balybot.impl.api.User;
import net.blay09.balybot.impl.twitch.kraken.TwitchAPI;
import net.blay09.balybot.impl.twitch.script.TwitchContext;
import net.blay09.balybot.impl.twitch.script.TwitchExpressions;
import net.blay09.javatmi.TMIClient;
import net.blay09.javatmi.TwitchUser;

import java.sql.SQLException;
import java.util.Collections;
import java.util.Map;

@Log4j2
public class TwitchImplementation implements BotImplementation {

	public static final String TWITCH_HOST = "irc.twitch.tv";

	private static final Map<String, Channel> channels = Maps.newHashMap();
	private final TwitchBotListener listener = new TwitchBotListener();

	private boolean isEnabled;
	private String username;
	private String token;

	private TMIClient client;
	private TwitchChatProvider chatProvider;

	@Override
	public String getId() {
		return "twitch";
	}

	@Override
	public void registerProperties(BotProperties properties) {
		properties.setProperty("twitch-enabled", "true");
		properties.setProperty("twitch-token", "");
	}

	@Override
	public void loadProperties(BotProperties properties) {
		isEnabled = properties.getProperty("twitch-enabled", true);
		username = properties.getProperty("twitch-user", "");
		token = properties.getProperty("twitch-token", "");
	}

	@Override
	public void registerUserLevels(UserLevelRegistry registry) {
		registry.register(TwitchUserLevels.BROADCASTER);
		registry.register(TwitchUserLevels.MOD);
		registry.register(TwitchUserLevels.SUB);
		registry.register(TwitchUserLevels.TURBO);
	}

	@Override
	public void registerExpressions(ExpressionLibrary library) {
		library.registerStaticClass(this, TwitchExpressions.class);
		library.registerDynamicClass(this, TwitchContext.class);
	}

	@Override
	public ChatProvider getChatProvider() {
		return chatProvider;
	}

	@Override
	public void start() {
		if(isEnabled) {
			if(token.trim().isEmpty()) {
				log.error("Twitch implementation is enabled, but no token was provided; unable to start");
				return;
			}
			if(username.trim().isEmpty()) {
				username = TwitchAPI.getUsername(token);
				BalyBot.getBotProperties().setProperty("twitch-user", username);
				BalyBot.getBotProperties().saveToFile();
			}

			if(ServerManager.findServer(TWITCH_HOST) == null) {
				try {
					int id = Database.addNewServer(TWITCH_HOST, getId());
					Server server = new Server(id, this, TWITCH_HOST);
					ServerManager.addServer(server);
				} catch (SQLException e) {
					log.error("Failed to store Twitch server: {}; unable to start", e.getMessage());
					return;
				}
			}

			client = new TMIClient(username, token, Collections.emptyList(), listener);
			if(!BalyBot.SIMULATED) {
				client.connect();
			}
			chatProvider = new TwitchChatProvider(client);
		}
	}

	@Override
	public void stop() {
		client.disconnect();
	}

	public static User createUserFrom(TwitchUser twitchUser) {
		return new User(twitchUser.getNick(), twitchUser.getDisplayName(), twitchUser);
	}

	public static Channel getChannel(String channelName) {
		return channels.get(channelName);
	}

	@Override
	public boolean handleCommandLine(String cmd) {
		if (cmd.startsWith("join ")) {
			joinChannel(cmd.substring(6));
			return true;
		} else if(cmd.startsWith("part ")) {
			partChannel(cmd.substring(6));
			return true;
		}
		return false;
	}

	public void joinChannel(String channelName) {
		if(!channels.containsKey(channelName)) {
			log.info("Joining channel " + channelName + "...");
			try {
				int id = Database.addNewChannel(channelName);
				Channel channel = new Channel(id, ServerManager.findServer(TWITCH_HOST), channelName);
				channels.put(channelName, channel);
				ChannelManager.addChannel(channel);

				ChannelManager.activateModule(channel, "manager");
				ChannelManager.activateModule(channel, "commands");
				ChannelManager.activateModule(channel, "test");

				client.join(channelName);
			} catch (SQLException e) {
				log.error("Failed to join channel: " + e.getMessage());
			}
		} else {
			log.info("Channel " + channelName + " is already joined.");
		}
	}

	public void partChannel(String channelName) {
		log.info("Leaving channel " + channelName + "...");
		client.part(channelName);
		Channel channel = channels.get(channelName);
		if(channel != null) {
			try {
				Database.setChannelActive(channel.getId(), false);
			} catch (SQLException e) {
				log.error("Failed to update channel status in database: " + e.getMessage());
				log.error("The bot will rejoin the channel upon reload.");
			}
		}
	}
}
