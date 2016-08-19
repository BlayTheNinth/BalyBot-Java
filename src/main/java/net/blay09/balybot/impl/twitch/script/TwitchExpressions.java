package net.blay09.balybot.impl.twitch.script;

import net.blay09.balybot.impl.api.Channel;
import net.blay09.balybot.impl.twitch.kraken.TwitchAPI;

public class TwitchExpressions {

	public static String title(Channel channel) {
		return TwitchAPI.getChannelData(channel).getTitle();
	}

	public static String game(Channel channel) {
		return TwitchAPI.getChannelData(channel).getGame();
	}

	public static int viewers(Channel channel) {
		return TwitchAPI.getStreamData(channel).getViewers();
	}

	public static boolean isLive(Channel channel) {
		return TwitchAPI.getStreamData(channel).isLive();
	}

}
