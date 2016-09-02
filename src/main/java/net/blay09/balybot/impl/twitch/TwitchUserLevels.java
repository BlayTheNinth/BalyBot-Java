package net.blay09.balybot.impl.twitch;

import net.blay09.balybot.impl.api.Channel;
import net.blay09.balybot.impl.api.User;
import net.blay09.balybot.impl.api.UserLevel;
import net.blay09.javatmi.TwitchUser;

public class TwitchUserLevels {

	public static final UserLevel SUB = new UserLevel("sub", 200) {
		@Override
		public boolean passes(Channel channel, User user) {
			return ((TwitchUser) user.getBackend()).isSubscriber();
		}
	};

	public static final UserLevel TURBO = new UserLevel("turbo", 100) {
		@Override
		public boolean passes(Channel channel, User user) {
			return ((TwitchUser) user.getBackend()).isTurbo();
		}
	};

}
