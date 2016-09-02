package net.blay09.balybot.impl.base.script;

import net.blay09.balybot.BalyBot;
import net.blay09.balybot.impl.api.Channel;
import net.blay09.balybot.impl.api.User;
import net.blay09.balybot.impl.api.UserLevel;

public class BalyBotBinding {

    public void message(Channel channel, String message) {
        channel.getChatProvider().sendMessage(channel, message);
    }

    public void whisper(Channel channel, String targetUser, String message) {
        channel.getChatProvider().sendDirectMessage(targetUser, message);
    }

    public int getUserLevel(Channel channel, User user) {
        return BalyBot.getUserLevelRegistry(channel.getImplementation()).getUserLevel(channel, user).getLevel();
    }

    public boolean passesUserLevel(Channel channel, User user, String userLevel) {
        UserLevel other = BalyBot.getUserLevelRegistry(channel.getImplementation()).fromName(userLevel);
        return other != null && BalyBot.getUserLevelRegistry(channel.getImplementation()).getUserLevel(channel, user).getLevel() >= other.getLevel();
    }
}
