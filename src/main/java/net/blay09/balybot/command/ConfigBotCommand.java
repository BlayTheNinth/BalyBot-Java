package net.blay09.balybot.command;

import net.blay09.balybot.Config;
import net.blay09.balybot.UserLevel;
import net.blay09.balybot.irc.IRCChannel;
import net.blay09.balybot.irc.IRCUser;

import java.util.Arrays;

public class ConfigBotCommand extends BotCommand {

    public ConfigBotCommand() {
        super("cfg", "^!cfg\\s(.+)", UserLevel.BROADCASTER);
    }

    @Override
    public void execute(IRCChannel channel, IRCUser sender, String[] args) {
        if(args.length < 2) {
            channel.message("Not enough parameters for cfg command. Syntax: !cfg [channel] <option> <value>");
            return;
        }

        String channelName, option, value;
        if(args.length >= 3) {
            if(!CommandHandler.passesUserLevel(sender, channel, UserLevel.OWNER)) {
                channel.message("Global options and configurations of other channels can only be changed as bot owner.");
                return;
            }
            channelName = args[0];
            option = args[1];
            value = String.join(" ", Arrays.copyOfRange(args, 2, args.length));
        } else {
            channelName = channel.getName();
            option = args[0];
            value = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
        }

        Config.setConfigOption(channelName, option, value);
        channel.message("Config option '" + option + "' updated: " + value);
    }

}
