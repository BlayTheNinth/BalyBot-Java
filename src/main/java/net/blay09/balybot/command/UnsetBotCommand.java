package net.blay09.balybot.command;

import net.blay09.balybot.UserLevel;
import net.blay09.balybot.irc.IRCChannel;
import net.blay09.balybot.irc.IRCUser;

public class UnsetBotCommand extends BotCommand {

    public UnsetBotCommand() {
        super("unset", "^!unset\\s?(.*)", UserLevel.MODERATOR);
    }

    @Override
    public void execute(IRCChannel channel, IRCUser sender, String[] args) {
        if(args.length < 1) {
            channel.message("Not enough parameters for unset command. Syntax: !unset <name>");
            return;
        }

        String name = args[0];
        for(BotCommand botCommand : CommandHandler.getGlobalCommands()) {
            if (botCommand.name.equals(name)) {
                channel.message("Command '" + botCommand.name + "' can not be removed.");
                return;
            }
        }
        for(BotCommand botCommand : CommandHandler.getChannelCommands(channel)) {
            if(botCommand.name.equals(name)) {
                if(CommandHandler.unregisterCommand(channel, botCommand)) {
                    channel.message("Command successfully removed: " + name);
                } else {
                    channel.message("Unexpected error, could not remove command!");
                }
                return;
            }
        }

        channel.message("Command not found: " + name);
    }

}
