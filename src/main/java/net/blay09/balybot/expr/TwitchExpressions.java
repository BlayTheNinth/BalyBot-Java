package net.blay09.balybot.expr;

import net.blay09.balybot.twitch.TwitchAPI;

public class TwitchExpressions {

    public static String title(String channelName) {
        return TwitchAPI.getChannelData(channelName).getTitle();
    }

    public static String game(String channelName) {
        return TwitchAPI.getChannelData(channelName).getGame();
    }

    public static int viewers(String channelName) {
        return TwitchAPI.getStreamData(channelName).getViewers();
    }

    public static boolean isLive(String channelName) {
        return TwitchAPI.getStreamData(channelName).isLive();
    }

}
