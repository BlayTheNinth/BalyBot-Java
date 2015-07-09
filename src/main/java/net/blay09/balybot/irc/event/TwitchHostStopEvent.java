package net.blay09.balybot.irc.event;

import net.blay09.balybot.irc.IRCConnection;

public class TwitchHostStopEvent extends IRCEvent {

    public final String channelName;

    public TwitchHostStopEvent(IRCConnection connection, String channelName) {
        super(connection);
        this.channelName = channelName;
    }

}
