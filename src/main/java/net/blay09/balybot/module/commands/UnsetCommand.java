package net.blay09.balybot.module.commands;

import lombok.extern.log4j.Log4j2;
import net.blay09.balybot.command.UserLevel;
import net.blay09.balybot.command.BotCommand;
import net.blay09.balybot.module.Module;
import net.blay09.javatmi.TwitchUser;

import java.sql.SQLException;

@Log4j2
public class UnsetCommand extends BotCommand {

    private final Module module;

    public UnsetCommand(Module module) {
        super("unset", "^" + module.getPrefix() + "unset(?:\\s+(.*)|$)", UserLevel.MOD.getLevel());
        this.module = module;
    }

    @Override
    public String getCommandSyntax() {
        return module.getPrefix() + name + " <name|id>";
    }

    @Override
    public String execute(String channelName, TwitchUser sender, String message, String[] args, int depth) {
        if(args.length < 1) {
            return "Not enough parameters for unset command. Syntax: " + getCommandSyntax();
        }

        String name = args[0];
        int id = 0;
		try {
			id = Integer.parseInt(name);
		} catch (NumberFormatException ignored) {}
        CustomBotRegexCommand foundCommand = null;
        for(BotCommand command : module.getCommands()) {
            if(command.getName().equals(name) || (command instanceof CustomBotRegexCommand && ((CustomBotRegexCommand) command).getId() == id)) {
                if(!(command instanceof CustomBotRegexCommand)) {
                    return "Command " + command.getName() + " can not be edited.";
                }
                foundCommand = (CustomBotRegexCommand) command;
            }
        }
        if(foundCommand != null) {
			module.unregisterCommand(foundCommand);
			try {
				((CommandsModule) module.getDefinition()).dbDeleteCommand(foundCommand);
			} catch (SQLException e) {
				log.error("Could not delete command from database: " + e.getMessage());
				log.error("Changes will be lost upon reload.");
			}
			return "Command successfully removed: " + foundCommand.getName();
        } else {
            return "Command not found: " + name;
        }
    }

    @Override
    public boolean ignoresCommandCooldown() {
        return true;
    }

}