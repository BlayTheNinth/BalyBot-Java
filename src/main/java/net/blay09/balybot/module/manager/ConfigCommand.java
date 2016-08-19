package net.blay09.balybot.module.manager;

import net.blay09.balybot.ChannelManager;
import net.blay09.balybot.impl.api.Channel;
import net.blay09.balybot.impl.api.User;
import net.blay09.balybot.impl.base.DefaultUserLevels;
import net.blay09.balybot.command.BotCommand;
import net.blay09.balybot.module.Module;
import org.apache.commons.lang3.StringUtils;

public class ConfigCommand extends BotCommand {

    private final Module module;

    public ConfigCommand(Module module) {
        super("cfg", "^" + module.getPrefix() + "cfg(?:\\s+(.*)|$)", DefaultUserLevels.CHANNEL_OWNER.getLevel());
        this.module = module;
    }

    @Override
    public String getCommandSyntax() {
        return module.getPrefix() + name + " [channel] <option> <value>";
    }

    @Override
    public String execute(Channel channel, User sender, String name, String[] args, int depth) {
        if(args.length < 2) {
            return "Not enough parameters for cfg command. Syntax: " + getCommandSyntax();
        }
        String option = args[0];
        String value = StringUtils.join(args, ' ', 1, args.length);
        ChannelManager.setChannelString(channel, option, value);
        return "Channel option '" + option + "' updated: " + value;
    }

    @Override
    public boolean ignoresCommandCooldown() {
        return true;
    }

}
