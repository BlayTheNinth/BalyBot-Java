package net.blay09.balybot.module.raffle;

import net.blay09.balybot.UserLevel;
import net.blay09.balybot.command.BotCommand;
import net.blay09.balybot.irc.IRCChannel;
import net.blay09.balybot.irc.IRCUser;
import net.blay09.balybot.module.ccpoll.ModuleCountedChatPoll;
import org.apache.commons.lang3.StringUtils;

public class RaffleBotCommand extends BotCommand {

    private final ModuleRaffle module;
    private final String prefix;

    public RaffleBotCommand(ModuleRaffle module, String prefix) {
        super("raffle", "^" + prefix + "raffle(?:\\s+(.*)|$)", UserLevel.MODERATOR);
        this.module = module;
        this.prefix = prefix;
    }

    @Override
    public String getCommandSyntax() {
        return prefix + "raffle (start|draw|stop) <keyword> [description]";
    }

    @Override
    public String execute(IRCChannel channel, IRCUser sender, String message, String[] args, int depth) {
        if(args.length < 1) {
            return "Not enough parameters for raffle command. Syntax: " + getCommandSyntax();
        }
        switch (args[0]) {
            case "start":
                if (args.length < 2) {
                    return "Not enough parameters for raffle command. Syntax: " + getCommandSyntax();
                }
                return module.startRaffle(channel, args[1], args.length > 2 ? String.join(" ", StringUtils.join(args, ' ', 2, args.length)) : null);
            case "draw":
                return module.draw(channel);
            case "stop":
                return module.stop(channel);
            default:
                return "Invalid parameters for raffle command. Syntax: " + getCommandSyntax();
        }
    }

}
