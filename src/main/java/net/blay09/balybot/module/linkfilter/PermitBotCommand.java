package net.blay09.balybot.module.linkfilter;

import net.blay09.balybot.command.BotCommand;
import net.blay09.balybot.UserLevel;
import net.blay09.balybot.irc.IRCChannel;
import net.blay09.balybot.irc.IRCUser;

public class PermitBotCommand extends BotCommand {

    private final ModuleLinkFilter module;
    private final char prefix;

    public PermitBotCommand(ModuleLinkFilter module, char prefix) {
        super("permit", "^" + prefix + "permit\\s?(.*)", UserLevel.MODERATOR);
        this.module = module;
        this.prefix = prefix;
    }

    private String getCommandSyntax() {
        return prefix + "permit <username>";
    }

    @Override
    public String execute(IRCChannel channel, IRCUser sender, String message, String[] args, int depth) {
        if(args.length < 1) {
            return "Not enough parameters for permit command. Syntax: " + getCommandSyntax();
        }
        module.permit(args[0]);
        return sender.getName() + ", you may now post one link. Make it count!";
    }

}
