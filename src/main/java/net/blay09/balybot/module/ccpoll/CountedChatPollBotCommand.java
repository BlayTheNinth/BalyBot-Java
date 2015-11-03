package net.blay09.balybot.module.ccpoll;

import net.blay09.balybot.UserLevel;
import net.blay09.balybot.command.BotCommand;
import net.blay09.balybot.irc.IRCChannel;
import net.blay09.balybot.irc.IRCUser;
import org.apache.commons.lang3.StringUtils;

public class CountedChatPollBotCommand extends BotCommand {

    public CountedChatPollBotCommand() {
        super("ccp", "^!ccp\\s?(.*)", UserLevel.MODERATOR);
    }

    @Override
    public void execute(IRCChannel channel, IRCUser sender, String[] args) {
        if(args.length < 1) {
            channel.message("Not enough parameters for ccp command. Syntax: !ccp (start|stop) <text> <maxCount> [description]");
            return;
        }
        if(args[0].equals("start")) {
            if(args.length < 3) {
                channel.message("Not enough parameters for ccp command. Syntax: !ccp start <text> <maxCount> [description]");
                return;
            }
            int maxCount;
            try {
                maxCount = Integer.parseInt(args[2]);
                if(maxCount < 1 || maxCount > 12) {
                    channel.message("Parameter 'maxCount' must be within 1 and 12. Syntax: !ccp start <text> <maxCount>");
                }
            } catch (NumberFormatException e) {
                channel.message("Expected numeric value for parameter 'maxCount'. Syntax: !ccp start <text> <maxCount>");
                return;
            }
            CountedChatPoll.instance.startPoll(channel, args[1], maxCount, args.length > 3 ? String.join(" ", StringUtils.join(args, ' ', 3, args.length)) : null);
        } else if(args[0].equals("stop")) {
            CountedChatPoll.instance.stop(channel);
        } else {
            channel.message("Invalid parameters for ccp command. Syntax: !ccp (start|stop) [text] [maxCount]");
        }
    }

}
