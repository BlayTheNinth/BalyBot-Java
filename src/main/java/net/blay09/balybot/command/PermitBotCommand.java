package net.blay09.balybot.command;

import net.blay09.balybot.LinkFilter;
import net.blay09.balybot.UserLevel;
import net.blay09.balybot.irc.IRCChannel;
import net.blay09.balybot.irc.IRCUser;

public class PermitBotCommand extends BotCommand {

    public PermitBotCommand() {
        super("permit", "^!permit\\s?(.*)", UserLevel.MODERATOR);
    }

    @Override
    public void execute(IRCChannel channel, IRCUser sender, String[] args) {
        if(args.length < 1) {
            channel.message("Not enough parameters for permit command. Syntax: !permit <username>");
            return;
        }

        LinkFilter.permit(channel.getName(), args[0]);
        channel.message(sender.getName() + ", you may now post one link. Yay!!");
    }

}
