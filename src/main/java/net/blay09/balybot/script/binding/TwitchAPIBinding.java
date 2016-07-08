package net.blay09.balybot.script.binding;

import net.blay09.balybot.twitch.StreamData;
import net.blay09.balybot.twitch.TwitchAPI;

public class TwitchAPIBinding {

    public StreamData getStreamData(String channelName) {
        return TwitchAPI.getStreamData(channelName);
    }

}
