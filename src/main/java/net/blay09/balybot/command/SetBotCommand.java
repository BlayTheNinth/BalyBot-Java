package net.blay09.balybot.command;

import net.blay09.balybot.UserLevel;
import net.blay09.balybot.expr.ExpressionLibrary;
import net.blay09.balybot.irc.IRCChannel;
import net.blay09.balybot.irc.IRCUser;
import net.blay09.balybot.CommandHandler;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;

public class SetBotCommand extends BotCommand {

    public SetBotCommand() {
        super("set", "^!set(?:\\s+(.*)|$)", UserLevel.MODERATOR);
    }

    @Override
    public void execute(IRCChannel channel, IRCUser sender, String[] args) {
        if(args.length < 2) {
            channel.message("Not enough parameters for set command. Syntax: !set [-ul userLevel] [-if conditions] <name> <message>");
            return;
        }

        UserLevel userLevel = UserLevel.ALL;
        String condition = null;
        int startIdx = 0;
        for(int i = 0; i < args.length; i++) {
            if (args[i].equals("-ul")) {
                i++;
                userLevel = UserLevel.fromName(args[i]);
                if(userLevel == null) {
                    channel.message("Invalid user level '" + args[1] + "'. Valid are: all, turbo, reg, sub, mod, broadcaster, owner");
                    return;
                }
            } else if (args[i].equals("-if")) {
                i++;
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
                    channel.message("The supplied condition is invalid: " + e.getMessage());
                    return;
                }
            } else {
                startIdx = i;
                break;
            }
        }

        boolean overwrite = false;
        String name = args[startIdx];
        for(BotCommand botCommand : CommandHandler.getGlobalCommands()) {
            if (botCommand.name.equals(name)) {
                channel.message("Command '" + botCommand.name + "' can not be edited.");
                return;
            }
        }
        for(BotCommand botCommand : CommandHandler.getChannelCommands(channel)) {
            if(botCommand.name.equals(name)) {
                if(!CommandHandler.unregisterCommand(channel, botCommand)) {
                    channel.message("Unexpected error, could not edit command!");
                    return;
                }
                overwrite = true;
                break;
            }
        }

        String message = StringUtils.join(args, ' ', startIdx + 1, args.length);
        if(CommandHandler.registerMessageCommand(channel, new SimpleMessageBotCommand(name, message, userLevel, condition))) {
            channel.message("Command successfully " + (overwrite ? "edited" : "registered") + ": " + name);
        } else {
            channel.message("Unexpected error, could not save command!");
        }
    }

}
