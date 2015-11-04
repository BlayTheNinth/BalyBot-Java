package net.blay09.balybot.expr;

import net.blay09.balybot.irc.IRCChannel;
import net.blay09.balybot.twitch.TwitchAPI;

public class TwitchContext {

    private final IRCChannel channel;

    public TwitchContext(IRCChannel channel) {
        this.channel = channel;
    }

    public boolean IS_LIVE() {
        return TwitchAPI.getStreamData(channel.getName()).isLive();
    }

    public int VIEWERS() {
        return TwitchAPI.getStreamData(channel.getName()).getViewers();
    }

    public int CHATTERS() {
        return channel.getUserList().size();
    }

    public String TITLE() {
        return TwitchAPI.getChannelData(channel.getName()).getTitle();
    }

    public String GAME() {
        return TwitchAPI.getChannelData(channel.getName()).getGame();
    }

}
