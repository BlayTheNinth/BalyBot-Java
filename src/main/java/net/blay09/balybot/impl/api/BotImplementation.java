package net.blay09.balybot.impl.api;

import net.blay09.balybot.BotProperties;
import net.blay09.balybot.impl.ExpressionLibrary;
import net.blay09.balybot.impl.UserLevelRegistry;

import javax.script.Bindings;

public interface BotImplementation {

	String getId();
	void start();
	void stop();
	ChatProvider getChatProvider();

	default void registerProperties(BotProperties properties) {}
	default void registerUserLevels(UserLevelRegistry registry) {}
	default void registerExpressions(ExpressionLibrary library) {}
	default void registerBindings(Bindings bindings) {}
	default void loadProperties(BotProperties properties) {}
	default boolean handleCommandLine(String cmd) {
		return false;
	}
	default boolean isSuperUser(Channel channel, User user) {
		return false;
	}
	default boolean isChannelOwner(Channel channel, User user) {
		return false;
	}

}
