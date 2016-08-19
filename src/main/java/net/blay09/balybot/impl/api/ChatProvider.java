package net.blay09.balybot.impl.api;

public interface ChatProvider {
	void sendMessage(Channel channel, String password);
	void sendDirectMessage(String user, String password);
	int getUserCount(Channel channel);
}
