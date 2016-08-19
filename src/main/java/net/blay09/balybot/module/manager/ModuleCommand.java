package net.blay09.balybot.module.manager;

import net.blay09.balybot.BalyBot;
import net.blay09.balybot.ChannelManager;
import net.blay09.balybot.impl.api.Channel;
import net.blay09.balybot.impl.api.User;
import net.blay09.balybot.impl.base.DefaultUserLevels;
import net.blay09.balybot.command.BotCommand;
import net.blay09.balybot.module.Module;
import net.blay09.balybot.module.ModuleDef;

public class ModuleCommand extends BotCommand {

    private final Module module;

    public ModuleCommand(Module module) {
        super("module", "^" + BalyBot.PREFIX + "module(?:\\s+(.*)|$)", DefaultUserLevels.CHANNEL_OWNER.getLevel());
        this.module = module;
    }

    @Override
    public String getCommandSyntax() {
        return BalyBot.PREFIX + name + " <name> (on|off) [prefix] OR " + BalyBot.PREFIX + name + " list [active]";
    }

    @Override
    public String execute(Channel channel, User sender, String message, String[] args, int depth) {
        if(args.length < 1) {
            return "Not enough parameters for module command. Syntax: " + getCommandSyntax();
        }
        String moduleId = args[0];
		if(moduleId.equals("list")) {
			if(args.length > 1) {
				if(args[1].equals("active")) {
					StringBuilder sb = new StringBuilder();
					for(Module module : ChannelManager.getModules(channel)) {
						if(sb.length() > 0) {
							sb.append(", ");
						}
						sb.append(module.getId());
					}
					return "Active Modules: " + sb.toString();
				}
			}
			StringBuilder sb = new StringBuilder();
			for(ModuleDef module : BalyBot.getInstance().getAvailableModules()) {
				if(sb.length() > 0) {
					sb.append(", ");
				}
				sb.append(module.getId());
			}
			return "Available Modules: " + sb.toString();
		}
		if(args.length < 2) {
			return "Not enough parameters for module command. Syntax: " + getCommandSyntax();
		}
        String state = args[1].toLowerCase();
		String prefix = "!";
		if(args.length > 2) {
			prefix = args[2];
		}
		switch (state) {
			case "on":
				ChannelManager.setChannelString(channel, moduleId + ".prefix", prefix);
				ChannelManager.activateModule(channel, moduleId);
				return "Module activated: " + moduleId;
			case "off":
				ChannelManager.deactivateModule(channel, moduleId);
				return "Module deactivated: " + moduleId;
			default:
				return "Invalid parameters for module command. Syntax: " + getCommandSyntax();
		}
    }

    @Override
    public boolean ignoresCommandCooldown() {
        return true;
    }

}
