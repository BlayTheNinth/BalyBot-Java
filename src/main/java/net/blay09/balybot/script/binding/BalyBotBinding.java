package net.blay09.balybot.script.binding;

import net.blay09.balybot.BalyBot;
import net.blay09.balybot.command.UserLevel;
import net.blay09.javatmi.TwitchUser;

public class BalyBotBinding {

    public void timeout(String channelName, String username, int time) {
        BalyBot.getInstance().getClient().getTwitchCommands().timeout(channelName, username, time);
    }

    public void message(String channelName, String message) {
        BalyBot.getInstance().getClient().send(channelName, message);
    }

    public int getUserLevel(String channelName, TwitchUser user) {
        return UserLevel.getUserLevel(channelName, user).getLevel();
    }

    public boolean passesUserLevel(String channelName, TwitchUser user, String userLevel) {
        UserLevel other = UserLevel.fromName(userLevel);
        if(other == null) {
            return false;
        }
        return UserLevel.getUserLevel(channelName, user).getLevel() >= other.getLevel();
    }
}
