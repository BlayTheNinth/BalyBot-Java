package net.blay09.balybot.module.manager;

import net.blay09.balybot.UserLevel;
import net.blay09.balybot.command.BotCommand;
import net.blay09.balybot.command.MessageBotCommand;
import net.blay09.balybot.expr.ExpressionLibrary;
import net.blay09.balybot.irc.IRCChannel;
import net.blay09.balybot.irc.IRCUser;
import net.blay09.balybot.CommandHandler;
import org.apache.commons.lang3.StringUtils;

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class SetRegexBotCommand extends BotCommand {

    private final char prefix;

    public SetRegexBotCommand(char prefix) {
        super("setregex", "^" + prefix + "setregex(?:\\s+(.*)|$)", UserLevel.MODERATOR);
        this.prefix = prefix;
    }

    private String getCommandSyntax() {
        return prefix + "setrgx [-ul userLevel] [-if condition] [-whisperto receiver] <pattern> <commandMessage>";
    }

    @Override
    public String execute(IRCChannel channel, IRCUser sender, String message, String[] args, int depth) {
        if(args.length < 2) {
            return "Not enough parameters for setrgx command. Syntax: " + getCommandSyntax();
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
                    Object obj = ExpressionLibrary.eval(channel, condition);
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
            Pattern.compile(pattern);
        } catch (PatternSyntaxException e) {
            return "Regex Syntax Error: " + e.getMessage();
        }

        boolean overwrite = false;
        String name = args[startIdx];
        for(BotCommand botCommand : CommandHandler.getGlobalCommands()) {
            if (botCommand.name.equals(name)) {
                return "Command '" + botCommand.name + "' can not be edited.";
            }
        }
        for(BotCommand botCommand : CommandHandler.getChannelCommands(channel)) {
            if(botCommand.name.equals(name)) {
                if(!CommandHandler.unregisterCommand(channel, botCommand)) {
                    return "Unexpected error, could not edit command!";
                }
                if(userLevel == null) {
                    userLevel = botCommand.minUserLevel;
                }
                overwrite = true;
                break;
            }
        }

        if(userLevel == null) {
            userLevel = UserLevel.ALL;
        }

        String commandMessage = StringUtils.join(args, ' ', startIdx + 1, args.length);
        if(CommandHandler.registerMessageCommand(channel, new MessageBotCommand(pattern, pattern, commandMessage, userLevel, condition, whisperTo))) {
            return "Command successfully " + (overwrite ? "edited" : "registered") + ": " + pattern;
        } else {
            return "Unexpected error, could not save command!";
        }
    }

}
