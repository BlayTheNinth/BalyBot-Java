package net.blay09.balybot;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import net.blay09.balybot.command.*;
import net.blay09.balybot.expr.ExpressionLibrary;
import net.blay09.balybot.module.calc.MathBotCommand;
import net.blay09.balybot.module.ccpoll.CountedChatPollBotCommand;
import net.blay09.balybot.module.manager.RegularBotCommand;
import net.blay09.balybot.irc.IRCChannel;
import net.blay09.balybot.irc.IRCUser;
import net.blay09.balybot.irc.event.IRCChannelChatEvent;
import net.blay09.balybot.module.linkfilter.PermitBotCommand;
import net.blay09.balybot.module.manager.*;
import net.blay09.balybot.module.song.SongBotCommand;
import net.blay09.balybot.module.time.TimeBotCommand;
import net.blay09.balybot.module.uptime.UptimeBotCommand;
import net.blay09.balybot.twitch.TwitchAPI;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CommandHandler {

    public static final CommandHandler instance = new CommandHandler();
    private static final Multimap<String, BotCommand> commands = ArrayListMultimap.create();
    private static final Pattern varPattern = Pattern.compile("\\{(?:([^\\?]+)(\\?))?([^\\}\\?]+)(\\?)?([^\\}]+)?\\}");
    private static final int MAX_CMD_DEPTH = 3;

    public static void load(Database database, EventBus eventBus) {
        try {
            Statement stmt = database.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM commands");
            while(rs.next()) {
                MessageBotCommand botCommand = new MessageBotCommand(rs.getString("command_name"), rs.getString("regex"), rs.getString("message"), UserLevel.fromId(rs.getInt("user_level")), rs.getString("condition"), rs.getString("whisper_to"));
                botCommand.setId(rs.getInt("id"));
                commands.put(rs.getString("channel_name").toLowerCase(), botCommand);
            }
            rs.close();
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        eventBus.register(instance);
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

    public static void handleCommand(IRCChannel channel, IRCUser sender, String message) {
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

    public static BotCommand findFirstCommand(IRCChannel channel, IRCUser sender, String message) {
        BotCommand command = findFirstCommand(channel, sender, message, commands.get("*"));
        if(command == null) {
            command = findFirstCommand(channel, sender, message, commands.get(channel.getName().toLowerCase()));
        }
        return command;
    }

    public static BotCommand findFirstCommand(IRCChannel channel, IRCUser sender, String message, Collection<BotCommand> commands) {
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

    public static boolean registerMessageCommand(IRCChannel channel, MessageBotCommand botCommand) {
        try {
            PreparedStatement stmtRegisterCommand = BalyBot.instance.getDatabase().stmtRegisterCommand;
            stmtRegisterCommand.setString(1, channel.getName());
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
            addCommand(channel.getName(), botCommand);
            rebuildDocs(channel);
            return true;
        } catch (SQLException e) {
            return false;
        }
    }

    public static boolean unregisterCommand(IRCChannel channel, BotCommand botCommand) {
        try {
            PreparedStatement stmtUnregisterCommand = BalyBot.instance.getDatabase().stmtUnregisterCommand;
            stmtUnregisterCommand.setString(1, channel.getName());
            stmtUnregisterCommand.setString(2, botCommand.name);
            stmtUnregisterCommand.executeUpdate();
            removeCommand(channel.getName(), botCommand);
            rebuildDocs(channel);
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
        return commands.get("*");
    }

    public static Collection<BotCommand> getChannelCommands(IRCChannel channel) {
        return commands.get(channel.getName());
    }

    public static String resolveVariables(String variables, BotCommand botCommand, IRCChannel channel, IRCUser sender, String message, String[] args, int depth) {
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
                    BotCommand command = CommandHandler.findFirstCommand(channel, sender, varName.substring(4));
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

    public static BotCommand addCommand(String context, BotCommand command) {
        commands.put(context, command);
        return command;
    }

    public static BotCommand removeCommand(String context, BotCommand command) {
        commands.remove(context, command);
        return command;
    }

    public static void rebuildDocs(IRCChannel channel) {
        DocBuilder.buildDocs(BalyBot.instance.getDatabase(), channel.getName());
    }
}
