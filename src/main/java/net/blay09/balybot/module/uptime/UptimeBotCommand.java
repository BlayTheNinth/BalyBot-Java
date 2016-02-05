package net.blay09.balybot.module.uptime;

import net.blay09.balybot.UserLevel;
import net.blay09.balybot.command.BotCommand;
import net.blay09.balybot.irc.IRCChannel;
import net.blay09.balybot.irc.IRCUser;
import net.blay09.balybot.twitch.TwitchAPI;

public class UptimeBotCommand extends BotCommand {

    private final String prefix;

    public UptimeBotCommand(String prefix, UserLevel userLevel) {
        super("uptime", "^" + prefix + "uptime(?:\\s+(.*)|$)", userLevel);
        this.prefix = prefix;
    }

    @Override
    public String getCommandSyntax() {
        return prefix + "uptime";
    }

    @Override
    public String execute(IRCChannel channel, IRCUser sender, String message, String[] args, int depth) {
        if(!TwitchAPI.getStreamData(channel.getName()).isLive()) {
            return "This channel is not live.";
        }
        StringBuilder sb = new StringBuilder();
        long uptime = TwitchAPI.getStreamData(channel.getName()).getUptime() / 1000;
        int days = (int) (uptime / (60*60*24));
        uptime -= days * 60*60*24;
        int hours = (int) (uptime / (60*60));
        uptime -= hours * 60*60;
        int minutes = (int) (uptime / 60);
        uptime -= minutes * 60;
        int seconds = (int) uptime;
        if(days > 0) {
            sb.append(days).append(" day");
            if(days > 1) {
                sb.append("s");
            }
        }
        if(hours > 0) {
            if(sb.length() > 0) {
                sb.append(", ");
            }
            sb.append(hours).append(" hour");
            if(hours > 1) {
                sb.append("s");
            }
        }
        if(minutes > 0) {
            if(sb.length() > 0) {
                sb.append(", ");
            }
            sb.append(minutes).append(" minute");
            if(minutes > 1) {
                sb.append("s");
            }
        }
        if(seconds > 0) {
            if(sb.length() > 0) {
                sb.append(", ");
            }
            sb.append(seconds).append(" second");
            if(seconds > 1) {
                sb.append("s");
            }
        }
        return "Stream uptime: " + sb.toString();
    }

}
