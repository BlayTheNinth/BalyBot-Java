package net.blay09.balybot.module.commands;

import lombok.extern.log4j.Log4j2;
import net.blay09.balybot.BalyBot;
import net.blay09.balybot.impl.api.Channel;
import net.blay09.balybot.impl.api.User;
import net.blay09.balybot.impl.api.UserLevel;
import net.blay09.balybot.impl.base.DefaultUserLevels;
import net.blay09.balybot.command.BotCommand;
import net.blay09.balybot.module.Module;
import org.apache.commons.lang3.StringUtils;

import java.sql.SQLException;

@Log4j2
public class SetUserLevelCommand extends BotCommand {

    private final Module module;

    public SetUserLevelCommand(Module module) {
        super("setul", "^" + BalyBot.PREFIX + "setul(?:\\s+(.*)|$)", DefaultUserLevels.CHANNEL_OWNER.getLevel());
        this.module = module;
    }

	@Override
	public String getCommandSyntax() {
		return BalyBot.PREFIX + name + " <name|id> <userlevel>";
	}

    @Override
    public String execute(Channel channel, User sender, String message, String[] args, int depth) {
        if(args.length < 1) {
            return "Not enough parameters for setul command. Syntax: " + getCommandSyntax();
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

        UserLevel userLevel = BalyBot.getUserLevelRegistry(channel.getImplementation()).fromName(args[1]);
        if(userLevel == null) {
            return "Invalid user level '" + args[1] + "'. Valid are: " + StringUtils.join(BalyBot.getUserLevelRegistry(channel.getImplementation()).getValidLevels(), ", ");
        }

        if(foundCommand != null) {
            foundCommand.setUserLevel(userLevel);
			try {
				((CommandsModule) module.getDefinition()).setCommand(foundCommand, channel);
			} catch (SQLException e) {
				log.error("Could not save command to database: " + e.getMessage());
				log.error("Changes will be lost upon reload.");
			}
			return "Command successfully edited: " + name;
        } else {
            return "Command not found: " + name;
        }
    }

    @Override
    public boolean ignoresCommandCooldown() {
        return true;
    }

}
