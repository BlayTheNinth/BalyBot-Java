package net.blay09.balybot.module.manager;

import net.blay09.balybot.UserLevel;
import net.blay09.balybot.command.BotCommand;
import net.blay09.balybot.irc.IRCChannel;
import net.blay09.balybot.irc.IRCUser;
import net.blay09.balybot.CommandHandler;

public class UnsetBotCommand extends BotCommand {

    private final String prefix;

    public UnsetBotCommand(String prefix) {
        super("unset", "^" + prefix + "unset(?:\\s+(.*)|$)", UserLevel.MODERATOR);
        this.prefix = prefix;
    }

    @Override
    public String getCommandSyntax() {
        return prefix + "unset <name|id>";
    }

    @Override
    public String execute(IRCChannel channel, IRCUser sender, String message, String[] args, int depth) {
        if(args.length < 1) {
            return "Not enough parameters for unset command. Syntax: " + getCommandSyntax();
        }

        String name = args[0];
        for(BotCommand botCommand : CommandHandler.getGlobalCommands()) {
            if (botCommand.name.equals(name)) {
                return "Command '" + botCommand.name + "' can not be removed.";
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

        if(foundCommand != null) {
            if(CommandHandler.unregisterCommand(channel, foundCommand)) {
                return "Command successfully removed: " + foundCommand.name;
            } else {
                return "Unexpected error, could not remove command!";
            }
        } else {
            return "Command not found: " + name;
        }
    }

}
