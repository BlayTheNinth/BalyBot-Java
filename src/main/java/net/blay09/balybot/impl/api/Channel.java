package net.blay09.balybot.impl.api;

import lombok.Value;

@Value
public class Channel {
	private final int id;
	private final Server server;
	private final String name;

	@Override
	public String toString() {
		return server.toString() + "/" + name;
	}

	public BotImplementation getImplementation() {
		return server.getImplementation();
	}

	public ChatProvider getChatProvider() {
		return server.getImplementation().getChatProvider();
	}
}
