package net.blay09.balybot.impl.api;

import lombok.NonNull;
import lombok.Value;

@Value
public class Server {
	private final int id;
	@NonNull
	private final BotImplementation implementation;
	@NonNull
	private final String serverHost;

	@Override
	public String toString() {
		return implementation.getId() + ":" + serverHost;
	}
}
