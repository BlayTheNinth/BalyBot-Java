package net.blay09.balybot.impl.twitch;

import net.blay09.balybot.impl.api.Channel;
import net.blay09.balybot.impl.api.User;
import net.blay09.balybot.impl.api.UserLevel;
import net.blay09.javatmi.TwitchUser;

public class TwitchUserLevels {

	public static final UserLevel BROADCASTER = new UserLevel("broadcaster", 400) {
		@Override
		public boolean passes(Channel channel, User user) {
			return user.getNick().toLowerCase().equals(channel.getName());
		}
	};

	public static final UserLevel MOD = new UserLevel("mod", 300) {
		@Override
		public boolean passes(Channel channel, User user) {
			return ((TwitchUser) user.getBackend()).isMod();
		}
	};

	public static final UserLevel SUB = new UserLevel("sub", 200) {
		@Override
		public boolean passes(Channel channel, User user) {
			return ((TwitchUser) user.getBackend()).isSubscriber();
		}
	};

	public static final UserLevel TURBO = new UserLevel("sub", 100) {
		@Override
		public boolean passes(Channel channel, User user) {
			return ((TwitchUser) user.getBackend()).isTurbo();
		}
	};

}
