package net.blay09.balybot.module.commands;

import com.google.common.base.Objects;
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
public class SetCommand extends BotCommand {

	private final Module module;

	public SetCommand(Module module) {
		super("set", "^" + module.getPrefix() + "set(?:\\s+(.*)|$)", DefaultUserLevels.CHANNEL_OWNER.getLevel());
		this.module = module;
	}

	@Override
	public String getCommandSyntax() {
		return module.getPrefix() + name + " [-ul userLevel] [-if condition] [-whisperto receiver] <name> <commandMessage>";
	}

    @Override
    public String execute(Channel channel, User sender, String message, String[] args, int depth) {
        if(args.length < 2) {
            return "Not enough parameters for set command. Syntax: " + getCommandSyntax();
        }

        UserLevel userLevel = null;
        String condition = null;
        String whisperTo = null;
        int startIdx = 0;
        for(int i = 0; i < args.length; i++) {
            if (args[i].equals("-ul")) {
                i++;
                if (i >= args.length) {
                    return "Not enough parameters for set command. Syntax: " + getCommandSyntax();
                }
                userLevel = BalyBot.getUserLevelRegistry().fromName(args[i]);
                if (userLevel == null) {
                    return "Invalid user level '" + args[1] + "'. Valid are: " + StringUtils.join(BalyBot.getUserLevelRegistry().getValidLevels(), ", ");
                }
            } else if(args[i].equals("-whisperto")) {
                i++;
                if (i >= args.length) {
                    return "Not enough parameters for set command. Syntax: " + getCommandSyntax();
                }
                whisperTo = args[i];
            } else if (args[i].equals("-if")) {
                i++;
                if(i >= args.length) {
                    return "Not enough parameters for set command. Syntax: " + getCommandSyntax();
                }
                if(args[i].startsWith("[")) {
                    StringBuilder sb = new StringBuilder();
                    if(args[i].length() > 1) {
                        if(args[i].endsWith("]")) {
                            condition = args[i].substring(1, args[i].length() - 1);
                            continue;
                        } else {
                            sb.append(args[i].substring(1));
                        }
                    }
                    i++;
                    while(i < args.length) {
                        if(args[i].endsWith("]")) {
                            if(args[i].length() > 1) {
                                sb.append(' ').append(args[i].substring(0, args[i].length() - 1));
                            }
                            break;
                        } else {
                            sb.append(' ').append(args[i]);
                        }
                        i++;
                    }
                    condition = sb.toString();
                } else {
                    condition = args[i];
                }
                try {
                    Object obj = BalyBot.getExpressionLibrary().eval(channel, condition);
                    if(!(obj instanceof Boolean)) {
                        throw new RuntimeException("Return value is not a boolean.");
                    }
                } catch (Throwable e) {
                    return "The supplied condition is invalid: " + e.getMessage();
                }
            } else {
                startIdx = i;
                break;
            }
        }

		String name = args[startIdx];
		if(name.startsWith("!")) {
			if(name.length() == 1) {
				return "Invalid command " + name;
			}
			name = name.substring(1);
		}
		CustomBotRegexCommand editCommand = null;
		for(BotCommand command : module.getCommands()) {
			if(command.getName().equals(name)) {
				if(!(command instanceof CustomBotRegexCommand)) {
					return "Command " + command.getName() + " can not be edited.";
				}
				if(userLevel == null) {
					userLevel = command.getUserLevel();
				}
				if(Objects.equal(condition, command.getCondition())) {
					editCommand = (CustomBotRegexCommand) command;
				}
			}
		}
		if(userLevel == null) {
			userLevel = DefaultUserLevels.ALL;
		}

		String commandMessage = StringUtils.join(args, ' ', startIdx + 1, args.length);
		CustomBotCommand newCommand = new CustomBotCommand(name, commandMessage, userLevel.getLevel(), condition, whisperTo);
		if(editCommand != null) {
			newCommand.setId(editCommand.getId());
			module.unregisterCommand(editCommand);
		}
		module.registerCommand(newCommand);
		if(editCommand != null) {
			try {
				((CommandsModule) module.getDefinition()).dbReplaceCommand(newCommand, channel.getId());
			} catch (SQLException e) {
				log.error("Could not save command to database: " + e.getMessage());
				log.error("Changes will be lost upon reload.");
			}
		} else {
			try {
				((CommandsModule) module.getDefinition()).dbInsertCommand(newCommand, channel.getId());
			} catch (SQLException e) {
				log.error("Could not save command to database: " + e.getMessage());
				log.error("Changes will be lost upon reload.");
			}
		}
		return "Command successfully " + (editCommand != null ? "edited" : "registered") + ": " + name;
    }

    @Override
    public boolean ignoresCommandCooldown() {
        return true;
    }
}
