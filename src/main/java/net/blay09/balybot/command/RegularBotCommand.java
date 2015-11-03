package net.blay09.balybot.command;

import net.blay09.balybot.Regulars;
import net.blay09.balybot.command.BotCommand;
import net.blay09.balybot.UserLevel;
import net.blay09.balybot.irc.IRCChannel;
import net.blay09.balybot.irc.IRCUser;

public class RegularBotCommand extends BotCommand {

    public RegularBotCommand() {
        super("reg", "^!reg\\s?(.*)", UserLevel.MODERATOR);
    }

    @Override
    public void execute(IRCChannel channel, IRCUser sender, String[] args) {
        if(args.length < 2) {
            channel.message("Not enough parameters for reg command. Syntax: !reg <add|remove> <username>");
            return;
        }

        boolean isRemove;
        String mode = args[0];
        switch(mode) {
            case "add": isRemove = false; break;
            case "remove": isRemove = true; break;
            default:
                channel.message("Invalid mode '" + args[0] + "'. Valid are: add, remove");
                return;
        }

        if(!isRemove) {
            if(Regulars.registerRegular(channel, args[1])) {
                channel.message("Successfully marked " + args[1] + " as a regular.");
            } else {
                channel.message("Unexpected error, could not make regular!");
            }
        } else {
            if(Regulars.unregisterRegular(channel, args[1])) {
                channel.message("Successfully removed " + args[1] + " as a regular.");
            } else {
                channel.message("Unexpected error, could not remove regular!");
            }
        }
    }

}
