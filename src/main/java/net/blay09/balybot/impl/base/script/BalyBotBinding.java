package net.blay09.balybot.impl.base.script;

import net.blay09.balybot.BalyBot;
import net.blay09.balybot.impl.api.Channel;
import net.blay09.balybot.impl.api.User;
import net.blay09.balybot.impl.api.UserLevel;

public class BalyBotBinding {

    public void message(Channel channel, String message) {
        channel.getChatProvider().sendMessage(channel, message);
    }

    public int getUserLevel(Channel channel, User user) {
        return BalyBot.getUserLevelRegistry().getUserLevel(channel, user).getLevel();
    }

    public boolean passesUserLevel(Channel channel, User user, String userLevel) {
        UserLevel other = BalyBot.getUserLevelRegistry().fromName(userLevel);
        return other != null && BalyBot.getUserLevelRegistry().getUserLevel(channel, user).getLevel() >= other.getLevel();
    }
}
