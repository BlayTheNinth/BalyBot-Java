package net.blay09.balybot.command;

import lombok.extern.log4j.Log4j2;
import net.blay09.balybot.BalyBot;
import net.blay09.balybot.ChannelManager;
import net.blay09.balybot.ServerManager;
import net.blay09.balybot.impl.UserLevelRegistry;
import net.blay09.balybot.impl.api.Channel;
import net.blay09.balybot.impl.api.User;
import net.blay09.balybot.module.Module;
import net.blay09.balybot.impl.twitch.kraken.TwitchAPI;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Log4j2
public class CommandHandler {

    private static final Pattern varPattern = Pattern.compile("\\{(?:([^\\?]+)(\\?))?([^\\}\\?]+)(\\?)?([^\\}]+)?\\}");
    private static final int MAX_CMD_DEPTH = 3;

    public static String resolveVariables(String variables, BotCommand botCommand, Channel channel, User sender, String message, String[] args, int depth) {
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
                varValue = TwitchAPI.getChannelData(channel).getTitle();
            } else if(varName.equals("GAME")) {
                varValue = TwitchAPI.getChannelData(channel).getGame();
            } else if(varName.equals("VIEWERS")) {
                varValue = String.valueOf(TwitchAPI.getStreamData(channel).getViewers());
            } else if(varName.equals("CHATTERS")) {
                varValue = String.valueOf(channel.getChatProvider().getUserCount(channel));
            } else if(varName.startsWith("EXPR:") && varName.length() > 5) {
                try {
                    varValue = String.valueOf(BalyBot.getExpressionLibrary().eval(channel, varName.substring(5)));
                } catch (Throwable e) {
                    varValue = e.getMessage();
                }
            } else if(varName.startsWith("CMD:") && varName.length() > 4) {
                if(depth <= MAX_CMD_DEPTH) {
                    BotCommand command = findCommand(channel, sender, varName.substring(4));
                    if (command != null) {
                        varValue = command.execute(channel, sender, message, args, depth + 1, true);
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

    public static BotCommand findCommand(Channel channel, User sender, String message) {
        for (Module module : ChannelManager.getModules(channel)) {
            BotCommand command = findCommand(channel, sender, message, module);
            if(command != null) {
                return command;
            }
        }
        for (Module module : ServerManager.getModules(channel.getServer())) {
            BotCommand command = findCommand(channel, sender, message, module);
            if(command != null) {
                return command;
            }
        }
        return null;
    }

    private static BotCommand findCommand(Channel channel, User sender, String message, Module module) {
        Matcher matcher = null;
        for (BotCommand command : module.getCommands()) {
            if(command.getUserLevelValue() < BalyBot.getUserLevelRegistry().getUserLevel(channel, sender).getLevel()) {
                continue;
            }
            if (matcher == null) {
                matcher = command.getPattern().matcher(message);
            } else {
                matcher.usePattern(command.getPattern());
                matcher.reset(message);
            }
            if(matcher.find()) {
                if (command.getCondition() != null) {
                    try {
                        Boolean obj = (Boolean) BalyBot.getExpressionLibrary().eval(channel, command.getCondition());
                        if (obj == null || !obj) {
                            continue;
                        }
                    } catch (Throwable e) {
                        log.error("Condition error-ed at command {}: {} ({})", command.getName(), command.getCondition(), e.getMessage());
                        continue;
                    }
                }
                return command;
            }
        }
        return null;
    }

}
