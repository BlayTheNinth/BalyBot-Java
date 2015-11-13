package net.blay09.balybot.module.manager;

import net.blay09.balybot.CommandHandler;
import net.blay09.balybot.UserLevel;
import net.blay09.balybot.command.BotCommand;
import net.blay09.balybot.command.MessageBotCommand;
import net.blay09.balybot.irc.IRCChannel;
import net.blay09.balybot.irc.IRCUser;

public class SetUserLevelCommand extends BotCommand {

    private final char prefix;

    public SetUserLevelCommand(char prefix) {
        super("setul", "^" + prefix + "setul(?:\\s+(.*)|$)", UserLevel.MODERATOR);
        this.prefix = prefix;
    }

    private String getCommandSyntax() {
        return prefix + "setul <name|id> <userlevel>";
    }

    @Override
    public String execute(IRCChannel channel, IRCUser sender, String message, String[] args, int depth) {
        if(args.length < 1) {
            return "Not enough parameters for setul command. Syntax: " + getCommandSyntax();
        }

        String name = args[0];
        for(BotCommand botCommand : CommandHandler.getGlobalCommands()) {
            if (botCommand.name.equals(name)) {
                return "Command '" + botCommand.name + "' can not be edited.";
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
            return "Invalid user level '" + args[1] + "'. Valid are: all, turbo, reg, sub, mod, broadcaster, owner";
        }

        if(foundCommand != null && foundCommand instanceof MessageBotCommand) {
            if(!CommandHandler.unregisterCommand(channel, foundCommand)) {
                return "Unexpected error, could not edit command!";
            }
            foundCommand.setUserLevel(userLevel);
            if(CommandHandler.registerMessageCommand(channel, (MessageBotCommand) foundCommand)) {
                return "Command successfully edited: " + name;
            } else {
                return "Unexpected error, could not save command!";
            }
        } else {
            return "Command not found: " + name;

        }
    }

}
