package net.blay09.balybot.command;

import net.blay09.balybot.UserLevel;
import net.blay09.balybot.irc.IRCChannel;
import net.blay09.balybot.irc.IRCUser;
import net.blay09.balybot.CommandHandler;

public class UnsetBotCommand extends BotCommand {

    public UnsetBotCommand() {
        super("unset", "^!unset(?:\\s+(.*)|$)", UserLevel.MODERATOR);
    }

    @Override
    public void execute(IRCChannel channel, IRCUser sender, String[] args) {
        if(args.length < 1) {
            channel.message("Not enough parameters for unset command. Syntax: !unset <name|id>");
            return;
        }

        String name = args[0];
        for(BotCommand botCommand : CommandHandler.getGlobalCommands()) {
            if (botCommand.name.equals(name)) {
                channel.message("Command '" + botCommand.name + "' can not be removed.");
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

        if(foundCommand != null) {
            if(CommandHandler.unregisterCommand(channel, foundCommand)) {
                channel.message("Command successfully removed: " + name);
            } else {
                channel.message("Unexpected error, could not remove command!");
            }
        } else {
            channel.message("Command not found: " + name);
        }
    }

}
