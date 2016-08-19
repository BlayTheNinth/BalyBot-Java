package net.blay09.balybot.impl.api;

public interface ChatProvider {
	void sendMessage(Channel channel, String message);
	void sendDirectMessage(String user, String message);
	int getUserCount(Channel channel);
}
