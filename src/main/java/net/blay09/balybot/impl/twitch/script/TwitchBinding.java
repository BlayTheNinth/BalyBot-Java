package net.blay09.balybot.impl.twitch.script;

import net.blay09.balybot.impl.api.Channel;
import net.blay09.balybot.impl.twitch.kraken.StreamData;
import net.blay09.balybot.impl.twitch.kraken.TwitchAPI;

public class TwitchBinding {

    public void timeout(Channel channel, String username, int time) {
        channel.getChatProvider().sendMessage(channel, "/timeout " + username + " " + time);
    }

    public StreamData getStreamData(Channel channel) {
        return TwitchAPI.getStreamData(channel);
    }

}
