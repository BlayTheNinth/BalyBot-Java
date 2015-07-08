package net.blay09.balybot.command;

import net.blay09.balybot.UserLevel;
import net.blay09.balybot.irc.IRCChannel;
import net.blay09.balybot.irc.IRCUser;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class TimeBotCommand extends BotCommand {

    public TimeBotCommand() {
        super("time", "^!time\\s(.+)", UserLevel.ALL);
    }

    @Override
    public void execute(IRCChannel channel, IRCUser sender, String[] args) {
        if(args.length < 1) {
            channel.message("Not enough parameters for time command. Syntax: !time <timezone>");
            return;
        }
        String timeZoneID = String.join(" ", args);
        String[] availableIDs = TimeZone.getAvailableIDs();
        for(String s : availableIDs) {
            if(timeZoneID.startsWith(s + "+") || timeZoneID.startsWith(s + "-")) {
                TimeZone timeZone = TimeZone.getTimeZone(args[0]);
                DateFormat dateFormat = new SimpleDateFormat("h:m a (H:m)");
                dateFormat.setTimeZone(timeZone);
                channel.message("The time in " + timeZone.getDisplayName(Locale.ENGLISH) + " is currently " + dateFormat.format(new Date(System.currentTimeMillis())) + ".");
                return;
            }
        }
        channel.message("Invalid timezone '" + timeZoneID + "'.");
    }

}
