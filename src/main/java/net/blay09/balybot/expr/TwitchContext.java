package net.blay09.balybot.expr;

import net.blay09.balybot.BalyBot;
import net.blay09.balybot.twitch.TwitchAPI;

@SuppressWarnings("unused")
public class TwitchContext {

	private final String channelName;

	public TwitchContext(String channelName) {
		this.channelName = channelName;
	}

	public String getChannelName() {
		return channelName;
	}

	public boolean IS_LIVE() {
		return TwitchAPI.getStreamData(channelName).isLive();
	}

	public int VIEWERS() {
		return TwitchAPI.getStreamData(channelName).getViewers();
	}

	public int CHATTERS() {
		return BalyBot.getInstance().getClient().getIRCConnection().getChannelSnapshot(channelName).getUsers().size();
	}

	public String TITLE() {
		return TwitchAPI.getChannelData(channelName).getTitle();
	}

	public String GAME() {
		return TwitchAPI.getChannelData(channelName).getGame();
	}
}
