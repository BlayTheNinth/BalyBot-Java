package net.blay09.balybot.module.manager;

import net.blay09.balybot.Regulars;
import net.blay09.balybot.UserLevel;
import net.blay09.balybot.command.BotCommand;
import net.blay09.balybot.irc.IRCChannel;
import net.blay09.balybot.irc.IRCUser;

public class RegularBotCommand extends BotCommand {

    private final char prefix;

    public RegularBotCommand(char prefix) {
        super("reg", "^" + prefix + "reg\\s?(.*)", UserLevel.MODERATOR);
        this.prefix = prefix;
    }

    private String getCommandSyntax() {
        return prefix + "reg <add|remove> <username>";
    }

    @Override
    public String execute(IRCChannel channel, IRCUser sender, String message, String[] args, int depth) {
        if(args.length < 2) {
            return "Not enough parameters for reg command. Syntax: " + getCommandSyntax();
        }

        boolean isRemove;
        String mode = args[0];
        switch(mode) {
            case "add": isRemove = false; break;
            case "remove": isRemove = true; break;
            default:
                return "Invalid mode '" + args[0] + "'. Valid are: add, remove";
        }

        if(!isRemove) {
            if(Regulars.registerRegular(channel, args[1])) {
                return "Successfully marked " + args[1] + " as a regular.";
            } else {
                return "Unexpected error, could not make regular!";
            }
        } else {
            if(Regulars.unregisterRegular(channel, args[1])) {
                return "Successfully removed " + args[1] + " as a regular.";
            } else {
                return "Unexpected error, could not remove regular!";
            }
        }
    }

}
