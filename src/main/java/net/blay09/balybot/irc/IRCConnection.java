// Copyright (c) 2014, Christopher "blay09" Baker

package net.blay09.balybot.irc;

import com.google.common.eventbus.EventBus;
import net.blay09.balybot.irc.event.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.net.*;
import java.util.*;

public class IRCConnection implements Runnable {

	public static class ProxyAuthenticator extends Authenticator {
		private PasswordAuthentication auth;

		public ProxyAuthenticator(String username, String password) {
			auth = new PasswordAuthentication(username, password.toCharArray());
		}

		@Override
		protected PasswordAuthentication getPasswordAuthentication() {
			return auth;
		}
	}

	private static final Logger logger = LogManager.getLogger();
	public static final int DEFAULT_PORT = 6667;

	public static final String EMOTE_START = "\u0001ACTION ";
	public static final String EMOTE_END = "\u0001";
	private static final int DEFAULT_PROXY_PORT = 1080;
	private final IRCParser parser = new IRCParser();

	private final IRCSender sender = new IRCSender(this);
	private final Map<String, IRCChannel> channels = new HashMap<>();
	private final Map<String, IRCUser> users = new HashMap<>();

	private final IRCConfig config;
	private final EventBus eventBus;
	private String nick;
	private boolean connected;
	private int waitingReconnect;

	private String channelTypes = "#&";
	private String channelUserModes = "ov";
	private String channelUserModePrefixes = "@+";

	private Socket socket;
	protected BufferedWriter writer;
	protected BufferedReader reader;

	public IRCConnection(IRCConfig config, String nick, EventBus eventBus) {
		this.config = config;
		this.nick = nick;
		this.eventBus = eventBus;
	}

	public String getNick() {
		return nick;
	}

	public IRCUser getBotUser() {
		return getOrCreateUser(nick);
	}

	public IRCChannel getChannel(String channelName) {
		return channels.get(channelName.toLowerCase());
	}

	public IRCChannel getOrCreateChannel(String channelName) {
		IRCChannel channel = getChannel(channelName);
		if(channel == null) {
			channel = new IRCChannel(this, channelName);
			channels.put(channelName.toLowerCase(), channel);
		}
		return channel;
	}

	public IRCUser getUser(String nick) {
		return users.get(nick.toLowerCase());
	}

	public IRCUser getOrCreateUser(String nick) {
		IRCUser user = getUser(nick);
		if(user == null) {
			user = new IRCUser(this, nick);
			users.put(nick.toLowerCase(), user);
		}
		return user;
	}

	public Collection<IRCChannel> getChannels() {
		return channels.values();
	}

	public boolean start() {
		eventBus.post(new IRCConnectingEvent(this));
		Thread thread = new Thread(this, "BalyBot Reader");
		thread.start();
		return true;
	}

	protected Proxy createProxy() {
		if(!config.proxyHost.isEmpty()) {
			if(!config.proxyUsername.isEmpty() || !config.proxyPassword.isEmpty()) {
				Authenticator.setDefault(new ProxyAuthenticator(config.proxyUsername, config.proxyPassword));
			}
			SocketAddress proxyAddr = new InetSocketAddress(config.proxyHost, config.proxyPort);
			return new Proxy(Proxy.Type.SOCKS, proxyAddr);
		}
		return null;
	}

	protected Socket connect() throws Exception {
		for(int i = 0; i < config.ports.length; i++) {
			try {
				SocketAddress targetAddr = new InetSocketAddress(config.host, config.ports[i]);
				Socket newSocket;
				Proxy proxy = createProxy();
				if (proxy != null) {
					newSocket = new Socket(proxy);
				} else {
					newSocket = new Socket();
				}

				if (!config.bindIP.isEmpty()) {
					newSocket.bind(new InetSocketAddress(config.bindIP, config.ports[i]));
				}
				newSocket.connect(targetAddr);
				writer = new BufferedWriter(new OutputStreamWriter(newSocket.getOutputStream(), config.charset));
				reader = new BufferedReader(new InputStreamReader(newSocket.getInputStream(), config.charset));
				sender.setWriter(writer);
				return newSocket;
			} catch (UnknownHostException e) {
				throw e;
			} catch (IOException e) {
				if(i == config.ports.length - 1) {
					throw e;
				}
			}
		}
		return null;
	}

	@Override
	public void run() {
		try {
			try {
				socket = connect();
			} catch (Exception e) {
				logger.error("Connection failed: " + e.getMessage());
				e.printStackTrace();
				eventBus.post(new IRCConnectionFailedEvent(this, e));
				return;
			}
			register();
			sender.start();
			String line;
			while ((line = reader.readLine()) != null && sender.isRunning()) {
				logger.debug("> " + line);
				if (!line.isEmpty()) {
					IRCMessage msg = parser.parse(line);
					if (!handleNumericMessage(msg)) {
						handleMessage(msg);
					}
				}
			}
		} catch (IOException e) {
			if(!e.getMessage().equals("Socket closed")) {
				e.printStackTrace();
			} else {
				closeSocket();
			}
		} catch (Exception e) {
			eventBus.post(new IRCExceptionEvent(this, e));
			closeSocket();
		}
		eventBus.post(new IRCDisconnectEvent(this));
		if(connected) {
			tryReconnect();
		}
	}

	public void tryReconnect() {
		closeSocket();
		if(waitingReconnect == 0) {
			waitingReconnect = 15000;
		} else {
			waitingReconnect *= 2;
		}
		eventBus.post(new IRCReconnectEvent(this, waitingReconnect));
		try {
			Thread.sleep(waitingReconnect);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		start();
	}

	public void disconnect(String quitMessage) {
		connected = false;
		try {
			if(writer != null) {
				writer.write("QUIT :" + quitMessage + "\r\n");
				writer.flush();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		closeSocket();
	}

	private void closeSocket() {
		try {
			if (socket != null) {
				socket.close();
			}
		} catch(IOException e) {
			e.printStackTrace();
		}
	}

	private void register() {
		try {
			if(config.serverPassword != null && !config.serverPassword.isEmpty()) {
				writer.write("PASS " + config.serverPassword + "\r\n");
			}
			writer.write("NICK " + nick + "\r\n");
			writer.write("USER " + config.ident + " \"\" \"\" :" + config.realName + "\r\n");
			writer.write("CAP REQ :twitch.tv/tags");
			writer.write("CAP REQ :twitch.tv/commands");
			writer.flush();
		} catch (IOException e) {
			e.printStackTrace();
			eventBus.post(new IRCConnectionFailedEvent(this, e));
			if(connected) {
				tryReconnect();
			}
		}
	}

	public void nick(String nick) {
		if(irc("NICK " + nick)) {
			this.nick = nick;
		}
	}

	public void join(String channelName, String channelKey) {
		irc("JOIN " + channelName + (channelKey != null ? (" " + channelKey) : ""));
	}

	public void part(String channelName) {
		if(irc("PART " + channelName)) {
			IRCChannel channel = channels.remove(channelName.toLowerCase());
			if(channel != null) {
				eventBus.post(new IRCChannelLeftEvent(this, channel));
			}
		}
	}

	public void mode(String targetName, String flags) {
		irc("MODE " + targetName + " " + flags);
	}

	public void mode(String targetName, String flags, String nick) {
		irc("MODE " + targetName + " " + flags + " " + nick);
	}

	public void topic(String channelName, String topic) {
		irc("TOPIC " + channelName + " :" + topic);
	}

	private boolean handleNumericMessage(IRCMessage msg) {
		int numeric = msg.getNumericCommand();
		if(numeric == -1) {
			return false;
		}
		if(numeric == IRCReplyCodes.RPL_NAMREPLY) {
			IRCChannel channel = getChannel(msg.arg(2));
			String[] names = msg.arg(3).split(" ");
			for (String name : names) {
				char firstChar = name.charAt(0);
				int idx = channelUserModePrefixes.indexOf(firstChar);
				IRCChannelUserMode mode = null;
				if(idx != -1) {
					mode = IRCChannelUserMode.fromChar(channelUserModes.charAt(idx));
					name = name.substring(1);
				}
				IRCUser user = getOrCreateUser(name);
				if(mode != null) {
					user.setChannelUserMode(channel, mode);
				}
				user.addChannel(channel);
				channel.addUser(user);
			}
			eventBus.post(new IRCChannelJoinedEvent(this, channel));
		} else if(numeric == IRCReplyCodes.RPL_WELCOME) {
			connected = true;
			waitingReconnect = 0;
			eventBus.post(new IRCConnectEvent(this));
		} else if(numeric == IRCReplyCodes.RPL_TOPIC) {
			IRCChannel channel = getChannel(msg.arg(1));
			if(channel != null) {
				channel.setTopic(msg.arg(2));
				eventBus.post(new IRCChannelTopicEvent(this, channel, null, channel.getTopic()));
			}
		} else if(numeric == IRCReplyCodes.RPL_WHOISLOGIN) {
			IRCUser user = getOrCreateUser(msg.arg(1));

			user.setAccountName(msg.arg(2));
		} else if(numeric == IRCReplyCodes.RPL_IDENTIFIED || numeric == IRCReplyCodes.RPL_WHOISLOGIN2) {
			IRCUser user = getOrCreateUser(msg.arg(1));
			user.setAccountName(msg.arg(1));
		} else if(numeric == IRCReplyCodes.RPL_ENDOFWHOIS) {
			IRCUser user = getOrCreateUser(msg.arg(1));
			if(user.getAccountName() == null || user.getAccountName().isEmpty()) {
				user.setAccountName(null);
			}
		} else if(numeric == IRCReplyCodes.ERR_NICKNAMEINUSE || numeric == IRCReplyCodes.ERR_ERRONEUSNICKNAME || numeric == IRCReplyCodes.ERR_PASSWDMISMATCH) {
			eventBus.post(new IRCErrorEvent(this, msg.getNumericCommand(), msg.args()));
		} else if(numeric == IRCReplyCodes.RPL_ISUPPORT) {
			for(int i = 0; i < msg.argcount(); i++) {
				if(msg.arg(i).startsWith("CHANTYPES=")) {
					channelTypes = msg.arg(i).substring(10);
				} else if(msg.arg(i).startsWith("PREFIX=")) {
					String value = msg.arg(i).substring(7);
					StringBuilder sb = new StringBuilder();
					for(int j = 0; j < value.length(); j++) {
						char c = value.charAt(j);
						if(c == ')') {
							channelUserModes = sb.toString();
							sb = new StringBuilder();
						} else if(c != '(') {
							sb.append(c);
						}
					}
					channelUserModePrefixes = sb.toString();
				}
			}
		} else if(numeric == IRCReplyCodes.RPL_MOTD || numeric <= 4 || numeric == 251 || numeric == 252 || numeric == 254 || numeric == 255 || numeric == 265 || numeric == 266 || numeric == 250 || numeric == 375) {
			logger.debug("Ignoring message code: " + msg.getCommand() + " (" + msg.argcount() + " arguments)");
		} else {
			logger.warn("Unhandled message code: " + msg.getCommand() + " (" + msg.argcount() + " arguments)");
		}
		return true;
	}

	private boolean handleMessage(IRCMessage msg) {
		String cmd = msg.getCommand();
		if(cmd.equals("PING")) {
			irc("PONG " + msg.arg(0));
		} else if(cmd.equals("PRIVMSG")) {
			IRCUser user = null;
			if(msg.getNick() != null) {
				user = getOrCreateUser(msg.getNick());
			}
			String target = msg.arg(0);
			String message = msg.arg(1);
			boolean isEmote = false;
			if(message.startsWith(EMOTE_START)) {
				message = message.substring(EMOTE_START.length(), message.length() - EMOTE_END.length());
				isEmote = true;
			}
			if(channelTypes.indexOf(target.charAt(0)) != -1) {
				IRCChannel channel = getChannel(target);
				if(user != null) {
					user.setNameColor(msg.getTagByKey("color"));
					user.setDisplayName(msg.getTagByKey("display-name"));
					user.setTwitchSubscriber(msg.getTagByKey("subscriber").equals("1"));
					user.setTwitchTurbo(msg.getTagByKey("turbo").equals("1"));
					String userType = msg.getTagByKey("user-type");
					switch(userType) {
						case "mod":
						case "global_mod":
							user.setChannelUserMode(channel, IRCChannelUserMode.OPER);
							break;
						default:
							user.setChannelUserMode(channel, null);
					}
				}
				eventBus.post(new IRCChannelChatEvent(this, channel, user, msg, message, isEmote, false));
			} else if(target.equals(this.nick)) {
				eventBus.post(new IRCPrivateChatEvent(this, user, msg, message, isEmote, false));
			}
		} else if(cmd.equals("NOTICE")) {
			IRCUser user = null;
			if(msg.getNick() != null) {
				user = getOrCreateUser(msg.getNick());
			}
			String target = msg.arg(0);
			String message = msg.arg(1);
			if(channelTypes.indexOf(target.charAt(0)) != -1) {
				eventBus.post(new IRCChannelChatEvent(this, getChannel(target), user, msg, message, false, true));
			} else if(target.equals(this.nick) || target.equals("*")) {
				eventBus.post(new IRCPrivateChatEvent(this, user, msg, message, false, true));
			}
		} else if(cmd.equals("HOSTTARGET")) {
			String source = msg.arg(0);
			String target = msg.arg(1);
			if(target.startsWith("-")) {
				eventBus.post(new TwitchHostStopEvent(this, source));
			} else {
				int viewerCount = 0;
				int lastSpace = target.lastIndexOf(' ');
				if(lastSpace != -1) {
					try {
						viewerCount = Integer.parseInt(target.substring(lastSpace + 1));
						target = target.substring(0, lastSpace);
					} catch (NumberFormatException ignored) {}
				}
				eventBus.post(new TwitchHostStartEvent(this, getChannel(target), source, viewerCount));
			}
		} else if(cmd.equals("JOIN")) {
			IRCUser user = getOrCreateUser(msg.getNick());
			IRCChannel channel = getOrCreateChannel(msg.arg(0));
			channel.addUser(user);
			user.addChannel(channel);
			eventBus.post(new IRCUserJoinEvent(this, channel, user));
		} else if(cmd.equals("PART")) {
			IRCUser user = getOrCreateUser(msg.getNick());
			IRCChannel channel = getChannel(msg.arg(0));
			if(channel != null) {
				channel.removeUser(user);
				user.removeChannel(channel);
				eventBus.post(new IRCUserLeaveEvent(this, channel, user, msg.arg(1)));
			}
		} else if(cmd.equals("TOPIC")) {
			IRCUser user = getOrCreateUser(msg.getNick());
			IRCChannel channel = getChannel(msg.arg(0));
			if(channel != null) {
				channel.setTopic(msg.arg(1));
				eventBus.post(new IRCChannelTopicEvent(this, channel, user, channel.getTopic()));
			}
		} else if(cmd.equals("NICK")) {
			String newNick = msg.arg(0);
			IRCUser user = getOrCreateUser(msg.getNick());
			users.remove(user.getName().toLowerCase());
			String oldNick = user.getName();
			user.setName(newNick);
			users.put(user.getName().toLowerCase(), user);
			eventBus.post(new IRCUserNickChangeEvent(this, user, oldNick, newNick));
		} else if(cmd.equals("MODE")) {
			if(channelTypes.indexOf(msg.arg(0).charAt(0)) == -1 || msg.argcount() < 3) {
				return false;
			}
			IRCChannel channel = getOrCreateChannel(msg.arg(0));
			String mode = msg.arg(1);
			String param = msg.arg(2);
			boolean set = false;
			List<Character> setList = new ArrayList<>();
			List<Character> unsetList = new ArrayList<>();
			for(int i = 0; i < mode.length(); i++) {
				char c = mode.charAt(i);
				if(c == '+') {
					set = true;
				} else if(c == '-') {
					set = false;
				} else if(set) {
					setList.add(c);
				} else {
					unsetList.add(c);
				}
			}
			IRCUser user = getOrCreateUser(param);
			IRCChannelUserMode currentMode = user.getChannelUserMode(channel);
			for(char c : setList) {
				int idx = channelUserModes.indexOf(c);
				if(idx != -1) {
					user.setChannelUserMode(channel, IRCChannelUserMode.fromChar(c));
				}
			}
			for(char c : unsetList) {
				if(c == currentMode.modeChar) {
					user.setChannelUserMode(channel, null);
				}
			}
		} else if(cmd.equals("QUIT")) {
			IRCUser user = getOrCreateUser(msg.getNick());
			eventBus.post(new IRCUserQuitEvent(this, user, msg.arg(0)));
			for(IRCChannel channel : user.getChannels()) {
				channel.removeUser(user);
			}
			users.remove(user.getName().toLowerCase());
		}
		return false;
	}

	public void whois(String nick) {
		irc("WHOIS " + nick);
	}

	public void message(String target, String message) {
		irc("PRIVMSG " + target + " :" + message);
	}

	public void notice(String target, String message) {
		irc("NOTICE " + target + " :" + message);
	}

	public void kick(String channelName, String nick, String reason) {
		irc("KICK " + channelName + " " + nick + (reason != null ? (" :" + reason) : ""));
	}

	public boolean irc(String message) {
		return sender.addToSendQueue(message);
	}

	public String getChannelTypes() {
		return channelTypes;
	}

	public String getChannelUserModes() {
		return channelUserModes;
	}

	public String getChannelUserModePrefixes() {
		return channelUserModePrefixes;
	}

	public IRCContext.ContextType getContextType() {
		return IRCContext.ContextType.IRCConnection;
	}

	public void message(String message) {}

	public void notice(String message) {}

	public IRCConfig getConfig() {
		return config;
	}

	public EventBus getEventBus() {
		return eventBus;
	}

	public boolean isConnected() {
		return connected;
	}

}
