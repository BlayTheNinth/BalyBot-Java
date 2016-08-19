package net.blay09.balybot.impl.api;

import lombok.Value;

@Value
public class Server {
	private final int id;
	private final BotImplementation implementation;
	private final String serverHost;

	@Override
	public String toString() {
		return implementation.getId() + ":" + serverHost;
	}
}
