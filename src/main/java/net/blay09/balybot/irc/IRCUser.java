// Copyright (c) 2014, Christopher "blay09" Baker
// All rights reserved.

package net.blay09.balybot.irc;

import java.util.*;

public class IRCUser {

	private final IRCConnection connection;
	private final Map<String, IRCChannel> channels = new HashMap<>();
	private final Map<String, IRCChannelUserMode> channelModes = new HashMap<>();
	private String name;
	private String nameColor;
	private boolean isTwitchSubscriber;
	private boolean isTwitchTurbo;
	private String accountName;
	private String displayName;

	public IRCUser(IRCConnection connection, String name) {
		this.connection = connection;
		this.name = name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}

	public IRCContext.ContextType getContextType() {
		return IRCContext.ContextType.IRCUser;
	}

	public boolean isOperator(IRCChannel channel) {
		IRCChannelUserMode mode = channelModes.get(channel.getName().toLowerCase());
		return mode != null && mode != IRCChannelUserMode.VOICE;
	}
	
	public boolean hasVoice(IRCChannel channel) {
		IRCChannelUserMode mode = channelModes.get(channel.getName().toLowerCase());
		return mode == IRCChannelUserMode.VOICE;
	}

	public String getChannelModePrefix(IRCChannel channel) {
		IRCChannelUserMode mode = channelModes.get(channel.getName().toLowerCase());
		if(mode != null) {
			int idx = channel.getConnection().getChannelUserModes().indexOf(mode.modeChar);
			if(idx != -1) {
				return String.valueOf(channel.getConnection().getChannelUserModePrefixes().charAt(idx));
			}
			return "";
		}
		return "";
	}

	public void setChannelUserMode(IRCChannel channel, IRCChannelUserMode mode) {
		if(mode == null) {
			channelModes.remove(channel.getName().toLowerCase());
		} else {
			channelModes.put(channel.getName().toLowerCase(), mode);
		}
	}

	public IRCChannelUserMode getChannelUserMode(IRCChannel channel) {
		return channelModes.get(channel.getName().toLowerCase());
	}
	
	public void addChannel(IRCChannel channel) {
		channels.put(channel.getName(), channel);
	}
	
	public void removeChannel(IRCChannel channel) {
		channels.remove(channel.getName());
	}
	
	public Collection<IRCChannel> getChannels() {
		return channels.values();
	}

	public String getIdentifier() {
		return connection.getConfig().host + "/" + name;
	}
	
	public String getUsername() {
		// TODO return nick!username@hostname instead
		return name;
	}

	public IRCConnection getConnection() {
		return connection;
	}

	public void whois() {
		connection.whois(name);
	}

	public void notice(String message) {
		connection.notice(name, message);
	}

	public void message(String message) {
		connection.message(name, message);
	}

	public boolean isTwitchSubscriber(IRCChannel channel) {
		return isTwitchSubscriber;
	}

	public void setTwitchSubscriber(boolean isSubscriber) {
		this.isTwitchSubscriber = isSubscriber;
	}

	public boolean isTwitchTurbo() {
		return isTwitchTurbo;
	}

	public void setTwitchTurbo(boolean twitchTurbo) {
		this.isTwitchTurbo = twitchTurbo;
	}

	public void setNameColor(String nameColor) {
		this.nameColor = nameColor;
	}

	public String getNameColor() {
		return nameColor;
	}

	public void setAccountName(String accountName) {
		this.accountName = accountName;
	}

	public String getAccountName() {
		return accountName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	public String getDisplayName() {
		return displayName;
	}
}
