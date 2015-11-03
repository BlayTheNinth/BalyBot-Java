package net.blay09.balybot.command;

import net.blay09.balybot.UserLevel;
import net.blay09.balybot.irc.IRCChannel;
import net.blay09.balybot.irc.IRCUser;
import net.blay09.balybot.CommandHandler;

import java.util.Arrays;

public class SetBotCommand extends BotCommand {

    public SetBotCommand() {
        super("set", "^!set(?:\\s+(.*)|$)", UserLevel.MODERATOR);
    }

    @Override
    public void execute(IRCChannel channel, IRCUser sender, String[] args) {
        if(args.length < 3) {
            channel.message("Not enough parameters for set command. Syntax: !set <name> <userLevel> <message>");
            return;
        }

        boolean overwrite = false;
        String name = args[0];
        for(BotCommand botCommand : CommandHandler.getGlobalCommands()) {
            if (botCommand.name.equals(name)) {
                channel.message("Command '" + botCommand.name + "' can not be edited.");
                return;
            }
        }
        for(BotCommand botCommand : CommandHandler.getChannelCommands(channel)) {
            if(botCommand.name.equals(name)) {
                if(!CommandHandler.unregisterCommand(channel, botCommand)) {
                    channel.message("Unexpected error, could not edit command!");
                    return;
                }
                overwrite = true;
                break;
            }
        }

        UserLevel userLevel = UserLevel.fromName(args[1]);
        if(userLevel == null) {
            channel.message("Invalid user level '" + args[1] + "'. Valid are: all, turbo, reg, sub, mod, broadcaster, owner");
            return;
        }

        String message = String.join(" ", Arrays.copyOfRange(args, 2, args.length));
        if(CommandHandler.registerMessageCommand(channel, new SimpleMessageBotCommand(name, message, userLevel))) {
            channel.message("Command successfully " + (overwrite ? "edited" : "registered") + ": " + name);
        } else {
            channel.message("Unexpected error, could not save command!");
        }
    }

}
