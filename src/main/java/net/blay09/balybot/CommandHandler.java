package net.blay09.balybot;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.eventbus.Subscribe;
import net.blay09.balybot.command.*;
import net.blay09.balybot.expr.ExpressionLibrary;
import net.blay09.balybot.irc.IRCChannel;
import net.blay09.balybot.irc.IRCUser;
import net.blay09.balybot.irc.event.IRCChannelChatEvent;
import net.blay09.balybot.twitch.TwitchAPI;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CommandHandler {

    private static final Pattern varPattern = Pattern.compile("\\{(?:([^\\?]+)(\\?))?([^\\}\\?]+)(\\?)?([^\\}]+)?\\}");
    private static final Map<String, CommandHandler> handlers = Maps.newHashMap();
    private static final int MAX_CMD_DEPTH = 3;
    private static final List<BotCommand> globalCommands = Lists.newArrayList();

    public static void loadGlobalCommands(Database database) {
        try {
            Statement stmt = database.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM commands WHERE channel_name = '*'");
            while(rs.next()) {
                MessageBotCommand botCommand = new MessageBotCommand(rs.getString("command_name"), rs.getString("regex"), rs.getString("message"), UserLevel.fromId(rs.getInt("user_level")), rs.getString("condition"), rs.getString("whisper_to"));
                botCommand.setId(rs.getInt("id"));
                globalCommands.add(botCommand);
            }
            rs.close();
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void loadChannelCommands(Database database) {
        try {
            Statement stmt = database.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM commands ORDER BY channel_name");
            String currentChannelName = null;
            CommandHandler currentCommandHandler = null;
            while(rs.next()) {
                String channelName = rs.getString("channel_name");
                if(currentChannelName == null || !currentChannelName.equals(channelName)) {
                    currentCommandHandler = CommandHandler.get(channelName);
                    EventManager.get(channelName).register(currentCommandHandler);
                    currentChannelName = channelName;
                }
                MessageBotCommand botCommand = new MessageBotCommand(rs.getString("command_name"), rs.getString("regex"), rs.getString("message"), UserLevel.fromId(rs.getInt("user_level")), rs.getString("condition"), rs.getString("whisper_to"));
                botCommand.setId(rs.getInt("id"));
                currentCommandHandler.addCommand(botCommand);
            }
            rs.close();
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    private final List<BotCommand> commands = Lists.newArrayList();
    private final String channelName;

    public CommandHandler(String channelName) {
        this.channelName = channelName;
    }

    @Subscribe
    @SuppressWarnings("unused")
    public void onChannelChat(IRCChannelChatEvent event) {
        if(event.channel == null) {
            return;
        }
        try {
            handleCommand(event.channel, event.sender, event.message);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void handleCommand(IRCChannel channel, IRCUser sender, String message) {
        BotCommand command = findFirstCommand(channel, sender, message);
        if(command != null) {
            String[] args;
            Matcher matcher = command.pattern.matcher(message);
            if(matcher.find()) {
                if (matcher.groupCount() > 0 && matcher.group(1) != null && matcher.group(1).trim().length() > 0) {
                    args = matcher.group(1).split(" ");
                } else {
                    args = new String[0];
                }
                String result = command.execute(channel, sender, message, args, 0);
                if(result != null) {
                    if(result.startsWith("/") || result.startsWith(".")) {
                        if(!result.startsWith("/me") && !result.startsWith(".me")) {
                            result = "-" + result;
                        }
                    }
                    if(command.whisperTo != null) {
                        String whisperTarget = resolveVariables(command.whisperTo, command, channel, sender, message, args, 0);
                        if(whisperTarget.contains("{") || whisperTarget.contains("}")) {
                            whisperTarget = sender.getName();
                        }
                        BalyBot.instance.getGroupConnection().message("#jtv", "/w " + whisperTarget + " " + result);
                    } else {
                        channel.message(result);
                    }
                }
            }
        }
    }

    public BotCommand findFirstCommand(IRCChannel channel, IRCUser sender, String message) {
        BotCommand command = findFirstCommand(channel, sender, message, globalCommands);
        if(command == null) {
            command = findFirstCommand(channel, sender, message, commands);
        }
        return command;
    }

    public BotCommand findFirstCommand(IRCChannel channel, IRCUser sender, String message, Collection<BotCommand> commands) {
        Matcher matcher = null;
        for(BotCommand command : commands) {
            if(!passesUserLevel(sender, channel, command.minUserLevel)) {
                continue;
            }
            if(matcher == null) {
                matcher = command.pattern.matcher(message);
            } else {
                matcher.usePattern(command.pattern);
            }
            if(matcher.find()) {
                if(command.condition != null) {
                    try {
                        Boolean obj = (Boolean) ExpressionLibrary.eval(channel, command.condition);
                        if (obj == null || !obj) {
                            continue;
                        }
                    } catch (Throwable e) {
                        System.err.println("Condition failed at command " + command.name + ": " + command.condition + " (" + e.getMessage() + ")");
                        continue;
                    }
                }

                return command;
            }
        }
        return null;
    }

    public boolean registerMessageCommand(MessageBotCommand botCommand) {
        try {
            PreparedStatement stmtRegisterCommand = BalyBot.instance.getDatabase().stmtRegisterCommand;
            stmtRegisterCommand.setString(1, channelName);
            stmtRegisterCommand.setString(2, botCommand.name);
            stmtRegisterCommand.setString(3, botCommand.regex);
            stmtRegisterCommand.setString(4, botCommand.commandMessage);
            stmtRegisterCommand.setInt(5, botCommand.minUserLevel.ordinal());
            stmtRegisterCommand.setString(6, botCommand.condition);
            stmtRegisterCommand.setString(7, botCommand.whisperTo);
            stmtRegisterCommand.executeUpdate();
            ResultSet rs = stmtRegisterCommand.getGeneratedKeys();
            if(rs.next()) {
                botCommand.setId(rs.getInt(1));
            }
            addCommand(botCommand);
            rebuildDocs();
            return true;
        } catch (SQLException e) {
            return false;
        }
    }

    public boolean unregisterCommand(BotCommand botCommand) {
        try {
            PreparedStatement stmtUnregisterCommand = BalyBot.instance.getDatabase().stmtUnregisterCommand;
            stmtUnregisterCommand.setString(1, channelName);
            stmtUnregisterCommand.setString(2, botCommand.name);
            stmtUnregisterCommand.executeUpdate();
            removeCommand(botCommand);
            rebuildDocs();
            return true;
        } catch (SQLException e) {
            return false;
        }
    }

    public static boolean passesUserLevel(IRCUser user, IRCChannel channel, UserLevel level) {
        if(user.getName().equalsIgnoreCase(BalyBot.instance.getConnection().getNick())) {
            return true;
        } else if(level == UserLevel.OWNER) {
            return false;
        }
        if(("#" + user.getName()).equalsIgnoreCase(channel.getName())) {
            return true;
        } else if(level == UserLevel.BROADCASTER) {
            return false;
        }
        if(user.isOperator(channel)) {
            return true;
        } else if(level == UserLevel.MODERATOR) {
            return false;
        }
        if(user.isTwitchSubscriber(channel)) {
            return true;
        } else if(level == UserLevel.SUBSCRIBER) {
            return false;
        }
        if(Regulars.isRegular(channel, user.getName())) {
            return true;
        } else if(level == UserLevel.REGULAR) {
            return false;
        }
        if(user.isTwitchTurbo()) {
            return true;
        } else if(level == UserLevel.TURBO) {
            return false;
        }
        return true;
    }

    public static Collection<BotCommand> getGlobalCommands() {
        return globalCommands;
    }

    public Collection<BotCommand> getChannelCommands() {
        return commands;
    }

    public String resolveVariables(String variables, BotCommand botCommand, IRCChannel channel, IRCUser sender, String message, String[] args, int depth) {
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
                varValue = TwitchAPI.getChannelData(channel.getName()).getTitle();
            } else if(varName.equals("GAME")) {
                varValue = TwitchAPI.getChannelData(channel.getName()).getGame();
            } else if(varName.equals("VIEWERS")) {
                varValue = String.valueOf(TwitchAPI.getStreamData(channel.getName()).getViewers());
            } else if(varName.equals("CHATTERS")) {
                varValue = String.valueOf(channel.getUserList().size());
            } else if(varName.startsWith("EXPR:") && varName.length() > 5) {
                try {
                    varValue = String.valueOf(ExpressionLibrary.eval(channel, varName.substring(5)));
                } catch (Throwable e) {
                    varValue = e.getMessage();
                }
            } else if(varName.startsWith("CMD:") && varName.length() > 4) {
                if(depth <= MAX_CMD_DEPTH) {
                    BotCommand command = findFirstCommand(channel, sender, varName.substring(4));
                    if (command != null) {
                        varValue = command.execute(channel, sender, message, args, depth + 1);
                    }
                }
            } else if(varName.startsWith("REG:")) {
                Matcher matcher = botCommand.pattern.matcher(message);
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

    public BotCommand addCommand(BotCommand command) {
        commands.add(command);
        return command;
    }

    public BotCommand removeCommand(BotCommand command) {
        commands.remove(command);
        return command;
    }

    public void rebuildDocs() {
        DocBuilder.buildDocs(BalyBot.instance.getDatabase(), channelName);
    }

    public static CommandHandler get(String name) {
        CommandHandler handler = handlers.get(name);
        if(handler == null) {
            handler = new CommandHandler(name);
            handlers.put(name, handler);
        }
        return handler;
    }

    public static CommandHandler get(IRCChannel channel) {
        return get(channel.getName());
    }

}
