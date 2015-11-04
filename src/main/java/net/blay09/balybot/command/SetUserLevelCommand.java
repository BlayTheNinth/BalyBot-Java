package net.blay09.balybot.command;

import net.blay09.balybot.CommandHandler;
import net.blay09.balybot.UserLevel;
import net.blay09.balybot.irc.IRCChannel;
import net.blay09.balybot.irc.IRCUser;

import java.util.Arrays;

public class SetUserLevelCommand extends BotCommand {

    public SetUserLevelCommand() {
        super("setul", "^!setul(?:\\s+(.*)|$)", UserLevel.MODERATOR);
    }

    @Override
    public void execute(IRCChannel channel, IRCUser sender, String[] args) {
        if(args.length < 1) {
            channel.message("Not enough parameters for setul command. Syntax: !setul <name|id> <userlevel>");
            return;
        }

        String name = args[0];
        for(BotCommand botCommand : CommandHandler.getGlobalCommands()) {
            if (botCommand.name.equals(name)) {
                channel.message("Command '" + botCommand.name + "' can not be edited.");
                return;
            }
        }
        BotCommand foundCommand = null;
        if(name.matches("[0-9]+")) {
            int id = Integer.parseInt(name);
            for(BotCommand botCommand : CommandHandler.getChannelCommands(channel)) {
                if(botCommand.id == id) {
                    foundCommand = botCommand;
                    break;
                }
            }
        }

        if(foundCommand == null) {
            for (BotCommand botCommand : CommandHandler.getChannelCommands(channel)) {
                if (botCommand.name.equals(name)) {
                    foundCommand = botCommand;
                    break;
                }
            }
        }

        UserLevel userLevel = UserLevel.fromName(args[1]);
        if(userLevel == null) {
            channel.message("Invalid user level '" + args[1] + "'. Valid are: all, turbo, reg, sub, mod, broadcaster, owner");
            return;
        }

        if(foundCommand != null && foundCommand instanceof MessageBotCommand) {
            if(!CommandHandler.unregisterCommand(channel, foundCommand)) {
                channel.message("Unexpected error, could not edit command!");
            }
            foundCommand.setUserLevel(userLevel);
            if(CommandHandler.registerMessageCommand(channel, (MessageBotCommand) foundCommand)) {
                channel.message("Command successfully edited: " + name);
            } else {
                channel.message("Unexpected error, could not save command!");
            }
        } else {
            channel.message("Command not found: " + name);
        }
    }

}
