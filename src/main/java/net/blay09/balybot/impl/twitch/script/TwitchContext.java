package net.blay09.balybot.impl.twitch.script;

import net.blay09.balybot.impl.api.Channel;
import net.blay09.balybot.impl.twitch.kraken.TwitchAPI;

@SuppressWarnings("unused")
public class TwitchContext {

	private final Channel channel;

	public TwitchContext(Channel channel) {
		this.channel = channel;
	}

	public String getChannelName() {
		return channel.getName();
	}

	public boolean IS_LIVE() {
		return TwitchAPI.getStreamData(channel).isLive();
	}

	public int VIEWERS() {
		return TwitchAPI.getStreamData(channel).getViewers();
	}

	public int CHATTERS() {
		return channel.getChatProvider().getUserCount(channel);
	}

	public String TITLE() {
		return TwitchAPI.getChannelData(channel).getTitle();
	}

	public String GAME() {
		return TwitchAPI.getChannelData(channel).getGame();
	}
}
