package net.blay09.balybot.impl.twitch;

import net.blay09.balybot.impl.api.Channel;
import net.blay09.balybot.impl.api.ChatProvider;
import net.blay09.javatmi.TMIClient;

public class TwitchChatProvider implements ChatProvider {

	private final TMIClient tmiClient;

	public TwitchChatProvider(TMIClient tmiClient) {
		this.tmiClient = tmiClient;
	}

	@Override
	public void sendMessage(Channel channel, String message) {
		tmiClient.send(channel.getName(), message);
	}

	@Override
	public void sendDirectMessage(String user, String message) {
		tmiClient.getTwitchCommands().whisper(user, message);
	}

	@Override
	public int getUserCount(Channel channel) {
		return tmiClient.getIRCConnection().getChannelSnapshot(channel.getName()).getUsers().size();
	}

}
