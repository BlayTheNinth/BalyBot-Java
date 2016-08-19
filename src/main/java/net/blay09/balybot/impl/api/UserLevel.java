package net.blay09.balybot.impl.api;

import lombok.Getter;

public abstract class UserLevel {

	@Getter private final String name;
	@Getter private final int level;

	public UserLevel(String name, int level) {
		this.name = name;
		this.level = level;
	}

	public abstract boolean passes(Channel channel, User user);

}
