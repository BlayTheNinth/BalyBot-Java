package net.blay09.balybot.module.manager;

import net.blay09.balybot.Config;
import net.blay09.balybot.UserLevel;
import net.blay09.balybot.command.BotCommand;
import net.blay09.balybot.irc.IRCChannel;
import net.blay09.balybot.irc.IRCUser;
import net.blay09.balybot.CommandHandler;

import java.util.Arrays;

public class ConfigBotCommand extends BotCommand {

    private final String prefix;

    public ConfigBotCommand(String prefix) {
        super("cfg", "^" + prefix + "cfg(?:\\s+(.*)|$)", UserLevel.BROADCASTER);
        this.prefix = prefix;
    }

    @Override
    public String getCommandSyntax() {
        return prefix + "cfg [channel] <option> <value>";
    }

    @Override
    public String execute(IRCChannel channel, IRCUser sender, String message, String[] args, int depth) {
        if(args.length < 2) {
            return "Not enough parameters for cfg command. Syntax: " + getCommandSyntax();
        }
        String channelName, option, value;
        if(args.length >= 3) {
            channelName = args[0];
            if(!channelName.startsWith("#")) {
                channelName = "#" + channelName;
            }
            if(!channelName.equalsIgnoreCase(channel.getName()) && !CommandHandler.passesUserLevel(sender, channel, UserLevel.OWNER)) {
                return "Global options and configurations of other channels can only be changed as bot owner.";
            }
            option = args[1];
            value = String.join(" ", Arrays.copyOfRange(args, 2, args.length));
        } else {
            channelName = channel.getName();
            option = args[0];
            value = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
        }

        Config.setConfigOption(channelName, option, value);
        return "Config option '" + option + "' updated: " + value;
    }

}
