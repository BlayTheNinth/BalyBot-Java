package net.blay09.balybot.module.manager;

import net.blay09.balybot.UserLevel;
import net.blay09.balybot.command.BotCommand;
import net.blay09.balybot.irc.IRCChannel;
import net.blay09.balybot.irc.IRCUser;

public class ModuleCommand extends BotCommand {

    private final String prefix;

    public ModuleCommand(String prefix) {
        super("module", "^" + prefix + "module(?:\\s+(.*)|$)", UserLevel.BROADCASTER);
        this.prefix = prefix;
    }

    @Override
    public String getCommandSyntax() {
        return prefix + "module <name> (on|off) [prefix]";
    }

    @Override
    public String execute(IRCChannel channel, IRCUser sender, String message, String[] args, int depth) {
        if(args.length < 2) {
            return "Not enough parameters for module command. Syntax: " + getCommandSyntax();
        }

        String moduleName = args[0];
        String state = args[1].toLowerCase();
        String prefix = "!";
        if(args.length > 2) {
            prefix = args[2];
        }
        if(state.equals("on") || state.equals("true") || state.equals("1")) {
            ModuleManager.activateModule(channel, moduleName, prefix);
            return "Module activated: " + moduleName;
        } else if(state.equals("off") || state.equals("false") || state.equals("0")) {
            ModuleManager.deactivateModule(channel, moduleName);
            return "Module deactivated: " + moduleName;
        } else {
            return "Invalid parameters for module command. Syntax: " + getCommandSyntax();
        }
    }

    @Override
    public boolean ignoresCommandCooldown() {
        return true;
    }

}
