package net.blay09.balybot.module.commands;

import lombok.Data;
import lombok.extern.log4j.Log4j2;
import net.blay09.balybot.command.UserLevel;
import net.blay09.balybot.command.BotCommand;
import net.blay09.balybot.expr.ExpressionLibrary;
import net.blay09.balybot.module.Module;
import net.blay09.javatmi.TwitchUser;
import org.apache.commons.lang3.StringUtils;

import java.sql.SQLException;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

@Log4j2
public class SetRegexCommand extends BotCommand {

    private final Module module;

    public SetRegexCommand(Module module) {
        super("setregex", "^" + module.getPrefix() + "setregex(?:\\s+(.*)|$)", UserLevel.MOD.getLevel());
        this.module = module;
    }

    @Override
    public String getCommandSyntax() {
        return module.getPrefix() + name + " [-ul userLevel] [-if condition] [-whisperto receiver] <pattern> <commandMessage>";
    }

    @Override
    public String execute(String channelName, TwitchUser sender, String message, String[] args, int depth) {
        if(args.length < 2) {
            return "Not enough parameters for setregex command. Syntax: " + getCommandSyntax();
        }

        UserLevel userLevel = null;
        String condition = null;
        String whisperTo = null;
        int startIdx = 0;
        for(int i = 0; i < args.length; i++) {
            if (args[i].equals("-ul")) {
                i++;
                if(i >= args.length) {
                    return "Not enough parameters for setrgx command. Syntax: " + getCommandSyntax();
                }
                userLevel = UserLevel.fromName(args[i]);
                if (userLevel == null) {
                    return "Invalid user level '" + args[1] + "'. Valid are: all, turbo, reg, sub, mod, broadcaster, owner";
                }
            } else if (args[i].equals("-whisperto")) {
                i++;
                if(i >= args.length) {
                    return "Not enough parameters for setrgx command. Syntax: " + getCommandSyntax();
                }
                whisperTo = args[i];
            } else if (args[i].equals("-if")) {
                i++;
                if(i >= args.length) {
                    return "Not enough parameters for setrgx command. Syntax: " + getCommandSyntax();
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
                    Object obj = ExpressionLibrary.eval(channelName, condition);
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

        String pattern = args[startIdx];
        try {
			//noinspection ResultOfMethodCallIgnored
			Pattern.compile(pattern);
        } catch (PatternSyntaxException e) {
            return "Regex Syntax Error: " + e.getMessage();
        }

        if(userLevel == null) {
            userLevel = UserLevel.ALL;
        }

		String commandMessage = StringUtils.join(args, ' ', startIdx + 1, args.length);
		CustomBotRegexCommand newCommand = new CustomBotRegexCommand(pattern, pattern, commandMessage, userLevel.getLevel(), condition, whisperTo);
		module.registerCommand(newCommand);
		try {
			((CommandsModule) module.getDefinition()).dbInsertCommand(newCommand, channelName);
		} catch (SQLException e) {
			log.error("Could not save command to database: " + e.getMessage());
			log.error("Changes will be lost upon reload.");
		}
		return "Command successfully registered: " + pattern;
    }

    @Override
    public boolean ignoresCommandCooldown() {
        return true;
    }

}
