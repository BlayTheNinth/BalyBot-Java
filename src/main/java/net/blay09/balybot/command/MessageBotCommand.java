package net.blay09.balybot.command;

import net.blay09.balybot.twitch.TwitchAPI;
import net.blay09.balybot.UserLevel;
import net.blay09.balybot.irc.IRCChannel;
import net.blay09.balybot.irc.IRCUser;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MessageBotCommand extends BotCommand {

    private static final Pattern varPattern = Pattern.compile("\\{(?:([^\\?]+)(\\?))?([A-Za-z0-9]+)(\\?)?([^\\}]+)?\\}");
    private static final Matcher varMatcher = varPattern.matcher("");

    public final String message;

    public MessageBotCommand(String name, String regex, String message, UserLevel minUserLevel, String condition) {
        super(name, regex, minUserLevel, condition);
        this.message = message;
    }

    @Override
    public void execute(IRCChannel channel, IRCUser sender, String[] args) {
        StringBuffer sb = new StringBuffer();
        varMatcher.reset(message);
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
                varValue = TwitchAPI.getChannelData(channel.getName()).getTitle();
            } else if(varName.equals("GAME")) {
                varValue = TwitchAPI.getChannelData(channel.getName()).getGame();
            } else if(varName.equals("VIEWERS")) {
                varValue = String.valueOf(TwitchAPI.getStreamData(channel.getName()).getViewers());
            } else if(varName.equals("CHATTERS")) {
                varValue = String.valueOf(channel.getUserList().size());
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
        channel.message(sb.toString());
    }

}
