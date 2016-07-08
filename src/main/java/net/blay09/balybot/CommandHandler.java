package net.blay09.balybot;

import net.blay09.balybot.command.BotCommand;
import net.blay09.balybot.expr.ExpressionLibrary;
import net.blay09.balybot.module.Module;
import net.blay09.balybot.twitch.TwitchAPI;
import net.blay09.javairc.snapshot.ChannelSnapshot;
import net.blay09.javatmi.TwitchUser;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CommandHandler {

    private static final Pattern varPattern = Pattern.compile("\\{(?:([^\\?]+)(\\?))?([^\\}\\?]+)(\\?)?([^\\}]+)?\\}");
    private static final int MAX_CMD_DEPTH = 3;

    public static String resolveVariables(String variables, BotCommand botCommand, String channelName, TwitchUser sender, String message, String[] args, int depth) {
        StringBuffer sb = new StringBuffer();
        Matcher varMatcher = varPattern.matcher(variables);
        while(varMatcher.find()) {
            String prefix = varMatcher.group(1);
            if(prefix == null) {
                prefix = "";
            }
            String varName = varMatcher.group(3);
            boolean isOptional = varMatcher.group(2) != null || varMatcher.group(4) != null;
            String suffix = varMatcher.group(5);
            if(suffix == null) {
                suffix = "";
            }
            String varValue = null;
            if(varName.equals("SENDER")) {
                varValue = sender.getDisplayName();
            } else if(varName.equals("TITLE")) {
                varValue = TwitchAPI.getChannelData(channelName).getTitle();
            } else if(varName.equals("GAME")) {
                varValue = TwitchAPI.getChannelData(channelName).getGame();
            } else if(varName.equals("VIEWERS")) {
                varValue = String.valueOf(TwitchAPI.getStreamData(channelName).getViewers());
            } else if(varName.equals("CHATTERS")) {
                ChannelSnapshot channelSnapshot = BalyBot.getInstance().getClient().getIRCConnection().getChannelSnapshot(channelName);
                varValue = String.valueOf(channelSnapshot.getUsers().size());
            } else if(varName.startsWith("EXPR:") && varName.length() > 5) {
                try {
                    varValue = String.valueOf(ExpressionLibrary.eval(channelName, varName.substring(5)));
                } catch (Throwable e) {
                    varValue = e.getMessage();
                }
            } else if(varName.startsWith("CMD:") && varName.length() > 4) {
                if(depth <= MAX_CMD_DEPTH) {
                    BotCommand command = findCommand(channelName, sender, varName.substring(4));
                    if (command != null) {
                        varValue = command.execute(channelName, sender, message, args, depth + 1, true);
                    }
                }
            } else if(varName.startsWith("REG:")) {
                Matcher matcher = botCommand.getPattern().matcher(message);
                if(matcher.find()) {
                    try {
                        int i = Integer.parseInt(varName.substring(4));
                        if(i >= 0 && i <= matcher.groupCount()) {
                            varValue = matcher.group(i);
                        }
                    } catch (NumberFormatException ignored) {}
                }
            } else if(varName.matches("[0-9]+")) {
                int index = Integer.parseInt(varName);
                if(index >= 0 && index < args.length) {
                    varValue = args[index];
                }
            }
            if(varValue == null) {
                if(isOptional) {
                    varMatcher.appendReplacement(sb, "");
                } else {
                    varMatcher.appendReplacement(sb, prefix + "{" + varName + "}" + suffix);
                }
            } else {
                varMatcher.appendReplacement(sb, prefix + varValue + suffix);
            }
        }
        varMatcher.appendTail(sb);
        return sb.toString();
    }

    public static BotCommand findCommand(String channel, TwitchUser sender, String message) {
        Matcher matcher = null;
        for (Module module : ChannelManager.getModules(channel)) {
            for (BotCommand command : module.getCommands()) {
                if (matcher == null) {
                    matcher = command.getPattern().matcher(message);
                } else {
                    matcher.usePattern(command.getPattern());
                    matcher.reset(message);
                }
                if(matcher.find()) {
                    if (command.getCondition() != null) {
                        try {
                            Boolean obj = (Boolean) ExpressionLibrary.eval(channel, command.getCondition());
                            if (obj == null || !obj) {
                                continue;
                            }
                        } catch (Throwable e) {
                            System.err.println("Condition failed at command " + command.getName() + ": " + command.getCondition() + " (" + e.getMessage() + ")");
                            continue;
                        }
                    }
                    return command;
                }
            }
        }
        return null;
    }

}
