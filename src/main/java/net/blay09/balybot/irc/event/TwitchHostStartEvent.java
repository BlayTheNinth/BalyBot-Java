package net.blay09.balybot.irc.event;

import net.blay09.balybot.irc.IRCChannel;
import net.blay09.balybot.irc.IRCConnection;

public class TwitchHostStartEvent extends IRCEvent {

    public final IRCChannel channel;
    public final String hostingChannel;
    public final int viewerCount;

    public TwitchHostStartEvent(IRCConnection connection, IRCChannel channel, String hostingChannel, int viewerCount) {
        super(connection);
        this.channel = channel;
        this.hostingChannel = hostingChannel;
        this.viewerCount = viewerCount;
    }
}
