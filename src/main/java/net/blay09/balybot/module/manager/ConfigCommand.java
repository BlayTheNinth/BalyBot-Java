package net.blay09.balybot.module.manager;

import net.blay09.balybot.Config;
import net.blay09.balybot.command.UserLevel;
import net.blay09.balybot.command.BotCommand;
import net.blay09.balybot.module.Module;
import net.blay09.javatmi.TwitchUser;
import org.apache.commons.lang3.StringUtils;

public class ConfigCommand extends BotCommand {

    private final Module module;

    public ConfigCommand(Module module) {
        super("cfg", "^" + module.getPrefix() + "cfg(?:\\s+(.*)|$)", UserLevel.BROADCASTER.getLevel());
        this.module = module;
    }

    @Override
    public String getCommandSyntax() {
        return module.getPrefix() + name + " [channel] <option> <value>";
    }

    @Override
    public String execute(String channelName, TwitchUser sender, String name, String[] args, int depth) {
        if(args.length < 2) {
            return "Not enough parameters for cfg command. Syntax: " + getCommandSyntax();
        }
        String targetChannel, option, value;
        if(args.length >= 3) {
            targetChannel = args[0];
            if(!targetChannel.startsWith("#")) {
                targetChannel = "#" + targetChannel;
            }
            if(!targetChannel.equalsIgnoreCase(channelName) && UserLevel.getUserLevel(channelName, sender).getLevel() < UserLevel.OWNER.getLevel()) {
                return "Global options and configurations of other channels can only be changed by the bot owner.";
            }
            option = args[1];
            value = StringUtils.join(args, ' ', 3, args.length);
        } else {
            targetChannel = channelName;
            option = args[0];
            value = StringUtils.join(args, ' ', 1, args.length);
        }
        Config.setChannelString(targetChannel, option, value);
        return "Config option '" + option + "' updated: " + value;
    }

    @Override
    public boolean ignoresCommandCooldown() {
        return true;
    }

}
