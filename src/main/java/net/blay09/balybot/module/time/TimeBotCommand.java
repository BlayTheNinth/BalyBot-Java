package net.blay09.balybot.module.time;

import net.blay09.balybot.Config;
import net.blay09.balybot.UserLevel;
import net.blay09.balybot.command.BotCommand;
import net.blay09.balybot.irc.IRCChannel;
import net.blay09.balybot.irc.IRCUser;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class TimeBotCommand extends BotCommand {

    private final String prefix;

    public TimeBotCommand(String prefix) {
        super("time", "^" + prefix + "time(?:\\s+(.*)|$)", UserLevel.ALL);
        this.prefix = prefix;
    }

    @Override
    public String getCommandSyntax() {
        return prefix + "time <timezone>";
    }

    @Override
    public String execute(IRCChannel channel, IRCUser sender, String message, String[] args, int depth) {
        String timeZoneID;
        if(args.length > 0) {
            timeZoneID = String.join(" ", args);
        } else if(Config.hasOption(channel.getName(), "time_timezone")){
            timeZoneID = Config.getValue(channel.getName(), "time_timezone");
        } else {
            return "Not enough parameters for time command. Syntax: !time <timezone>";
        }
        String[] availableIDs = TimeZone.getAvailableIDs();
        for(String s : availableIDs) {
            if(timeZoneID.startsWith(s + "+") || timeZoneID.startsWith(s + "-")) {
                TimeZone timeZone = TimeZone.getTimeZone(timeZoneID);
                DateFormat dateFormat = new SimpleDateFormat("h:m a (H:m)");
                dateFormat.setTimeZone(timeZone);
                return "The time in " + timeZone.getDisplayName(Locale.ENGLISH) + " is currently " + dateFormat.format(new Date(System.currentTimeMillis())) + ".";
            }
        }
        return "Invalid timezone '" + timeZoneID + "'.";
    }

}
