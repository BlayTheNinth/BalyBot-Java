package net.blay09.balybot.module.poll;

import net.blay09.balybot.UserLevel;
import net.blay09.balybot.command.BotCommand;
import net.blay09.balybot.irc.IRCChannel;
import net.blay09.balybot.irc.IRCUser;
import net.blay09.balybot.module.ccpoll.ModuleCountedChatPoll;
import org.apache.commons.lang3.StringUtils;

public class PollBotCommand extends BotCommand {

    private final ModulePoll module;
    private final String prefix;

    public PollBotCommand(ModulePoll module, String prefix, UserLevel userLevel) {
        super("poll", "^" + prefix + "poll(?:\\s+(.*)|$)", userLevel);
        this.module = module;
        this.prefix = prefix;
    }

    @Override
    public String getCommandSyntax() {
        return prefix + "poll (start|stop) [keyword] [option] [keyword2] [option2] ...";
    }

    @Override
    public String execute(IRCChannel channel, IRCUser sender, String message, String[] args, int depth) {
        if(args.length < 1) {
            return "Not enough parameters for poll command. Syntax: " + getCommandSyntax();
        }
        switch (args[0]) {
            case "start":
                if (args.length < 5 || args.length % 2 == 0) {
                    return "Not enough parameters for poll command. Syntax: " + getCommandSyntax();
                }
                String[] triggers = new String[(args.length - 1) / 2];
                String[] options = new String[triggers.length];
                int idx = 0;
                for(int i = 1; i < args.length; i += 2) {
                    triggers[idx] = args[i];
                    options[idx] = args[i + 1];
                    idx++;
                }
                return module.startPoll(triggers, options);
            case "stop":
                return module.stop();
            default:
                return "Invalid parameters for poll command. Syntax: " + getCommandSyntax();
        }
    }

}
