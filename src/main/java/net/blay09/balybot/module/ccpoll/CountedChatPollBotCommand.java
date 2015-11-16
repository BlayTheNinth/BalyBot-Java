package net.blay09.balybot.module.ccpoll;

import net.blay09.balybot.UserLevel;
import net.blay09.balybot.command.BotCommand;
import net.blay09.balybot.irc.IRCChannel;
import net.blay09.balybot.irc.IRCUser;
import org.apache.commons.lang3.StringUtils;

public class CountedChatPollBotCommand extends BotCommand {

    private final ModuleCountedChatPoll module;
    private final String prefix;

    public CountedChatPollBotCommand(ModuleCountedChatPoll module, String prefix) {
        super("ccp", "^" + prefix + "ccp(?:\\s+(.*)|$)", UserLevel.MODERATOR);
        this.module = module;
        this.prefix = prefix;
    }

    @Override
    public String getCommandSyntax() {
        return prefix + "ccp (start|stop) [maxCount] [keyword] [description]";
    }

    @Override
    public String execute(IRCChannel channel, IRCUser sender, String message, String[] args, int depth) {
        if(args.length < 1) {
            return "Not enough parameters for ccp command. Syntax: " + getCommandSyntax();
        }
        switch (args[0]) {
            case "start":
                if (args.length < 3) {
                    return "Not enough parameters for ccp command. Syntax: " + getCommandSyntax();
                }
                int maxCount;
                try {
                    maxCount = Integer.parseInt(args[1]);
                    if (maxCount < 1 || maxCount > 12) {
                        return "Parameter 'maxCount' must be within 1 and 12. Syntax: " + getCommandSyntax();
                    }
                } catch (NumberFormatException e) {
                    return "Expected numeric value for parameter 'maxCount'. Syntax: " + getCommandSyntax();
                }
                return module.startPoll(channel, args[2], maxCount, args.length > 3 ? String.join(" ", StringUtils.join(args, ' ', 3, args.length)) : null);
            case "stop":
                return module.stop(channel);
            default:
                return "Invalid parameters for ccp command. Syntax: " + getCommandSyntax();
        }
    }

}
