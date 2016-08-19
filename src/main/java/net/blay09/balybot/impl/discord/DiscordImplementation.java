package net.blay09.balybot.impl.discord;

import com.google.common.collect.Maps;
import lombok.extern.log4j.Log4j2;
import net.blay09.balybot.BotProperties;
import net.blay09.balybot.ChannelManager;
import net.blay09.balybot.Database;
import net.blay09.balybot.ServerManager;
import net.blay09.balybot.impl.api.BotImplementation;
import net.blay09.balybot.impl.api.Channel;
import net.blay09.balybot.impl.api.ChatProvider;
import net.blay09.balybot.impl.api.Server;
import net.blay09.balybot.impl.api.User;
import net.dv8tion.jda.JDA;
import net.dv8tion.jda.JDABuilder;
import net.dv8tion.jda.entities.Guild;
import net.dv8tion.jda.entities.TextChannel;
import net.dv8tion.jda.events.message.guild.GuildMessageReceivedEvent;

import javax.security.auth.login.LoginException;
import java.sql.SQLException;
import java.util.Map;

@Log4j2
public class DiscordImplementation implements BotImplementation {

	private static final Map<String, Server> servers = Maps.newHashMap();
	private static final Map<String, Channel> channels = Maps.newHashMap();

	private boolean isEnabled;
	private String token;

	private JDA jda;
	private DiscordChatProvider chatProvider;

	@Override
	public String getId() {
		return "discord";
	}

	@Override
	public void registerProperties(BotProperties properties) {
		properties.setProperty("discord-enabled", "false");
		properties.setProperty("discord-token", "");
	}

	@Override
	public void loadProperties(BotProperties properties) {
		isEnabled = properties.getProperty("discord-enabled", false);
		token = properties.getProperty("discord-token", "");
	}

	@Override
	public void start() {
		if(isEnabled) {
			if (token.trim().isEmpty()) {
				log.error("Discord implementation is enabled, but no token was provided; unable to start");
				return;
			}
			ServerManager.getServers().stream()
					.filter(server -> server.getImplementation() == this)
					.forEach(server -> servers.put(server.getServerHost(), server));
			ChannelManager.getChannels().stream()
					.filter(channel -> channel.getImplementation() == this)
					.forEach(channel -> channels.put(channel.getServer().getServerHost() + "/" + channel.getName(), channel));
			try {
				jda = new JDABuilder()
						.setBotToken(token)
						.addListener(new DiscordBotListener())
						.buildBlocking();
			} catch (LoginException | InterruptedException e) {
				log.error("Discord implementation failed to login", e);
				return;
			}
			chatProvider = new DiscordChatProvider(jda);
		}
	}

	@Override
	public boolean handleCommandLine(String cmd) {
		if(cmd.equals("discord.invite")) {
			log.info(jda.getSelfInfo().getAuthUrl());
		} else if(cmd.startsWith("discord.join ")) {
			joinChannel(cmd.substring("discord.join ".length()));
		}
		return false;
	}

	public void joinServer(String serverId) {
		Server server = servers.get(serverId);
		if(server == null) {
			try {
				int id = Database.addNewServer(serverId, getId());
				server = new Server(id, this, serverId);
				servers.put(server.getServerHost(), server);
				ServerManager.addServer(server);
			} catch (SQLException e) {
				throw new RuntimeException(e);
			}
		}
	}

	public void joinChannel(String args) {
		String serverId = null;
		String channelName;
		int sepIdx = args.indexOf('/');
		if(sepIdx != -1 && sepIdx + 1 < args.length()) {
			serverId = args.substring(0, sepIdx);
			channelName = args.substring(sepIdx + 1);
		} else {
			channelName = args;
			for(Guild guild : jda.getGuilds()) {
				if(guild.getTextChannels().stream().anyMatch(channel -> channel.getName().equals(args))) {
					if(serverId != null) {
						log.error("Channel '{}' is not unique among all connected servers; prefix with server id! (e.g. 123456/anime)", args);
						return;
					}
					serverId = guild.getId();
				}
			}
			if(serverId == null) {
				log.error("Channel {} was not found in any connected server", args);
				return;
			}
		}
		String fullName = serverId + "/" + channelName;
		if(!channels.containsKey(fullName)) {
			log.info("Joining channel {}...", fullName);
			Server server = servers.get(serverId);
			if(server == null) {
				log.error("Server {} is not registered, that's kinda bad", serverId);
				return;
			}
			try {
				int id = Database.addNewChannel(channelName, server.getId());
				Channel channel = new Channel(id, server, channelName);
				channels.put(fullName, channel);
				ChannelManager.addChannel(channel);

				ServerManager.activateModule(server, "manager");
				ServerManager.activateModule(server, "commands");
				ServerManager.activateModule(server, "test");
			} catch (SQLException e) {
				log.error("Failed to join channel: {}", e);
			}
		} else {
			log.info("Channel {} is already registered.", fullName);
		}
	}

	@Override
	public void stop() {
		jda.shutdown(true);
	}

	@Override
	public ChatProvider getChatProvider() {
		return chatProvider;
	}

	public static User createUserFrom(GuildMessageReceivedEvent event) {
		return new User(event.getAuthor().getUsername(), event.getAuthorNick(), event.getAuthor());
	}

	public static Channel getChannel(Guild guild, TextChannel channel) {
		return channels.get(guild.getId() + "/" + channel.getName());
	}

	public static Server getServer(String id) {
		return servers.get(id);
	}

	@Override
	public boolean areChannelsShared() {
		return true;
	}
}
