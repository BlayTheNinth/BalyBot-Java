package net.blay09.balybot.impl.twitch.script;

import net.blay09.balybot.impl.api.Channel;
import net.blay09.balybot.impl.twitch.kraken.StreamData;
import net.blay09.balybot.impl.twitch.kraken.TwitchAPI;

public class TwitchBinding {

    public void timeout(Channel channel, String username, int time, String reason) {
        channel.getChatProvider().sendMessage(channel, "/timeout " + username + " " + time + " " + reason);
    }

    public StreamData getStreamData(Channel channel) {
        return TwitchAPI.getStreamData(channel);
    }

}
