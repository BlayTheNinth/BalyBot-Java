package net.blay09.balybot.module.timer;

public class TimedCommand {

    public final String channelName;
    public final String command;
    public final int interval;
    public int timeSinceLast;

    public TimedCommand(String channelName, String command, int interval) {
        this.channelName = channelName;
        this.interval = interval;
        this.command = command;
    }

}
