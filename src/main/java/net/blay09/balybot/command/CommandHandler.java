package net.blay09.balybot.command;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.google.common.eventbus.Subscribe;
import net.blay09.balybot.BalyBot;
import net.blay09.balybot.Database;
import net.blay09.balybot.Regulars;
import net.blay09.balybot.UserLevel;
import net.blay09.balybot.irc.IRCChannel;
import net.blay09.balybot.irc.IRCUser;
import net.blay09.balybot.irc.event.IRCChannelChatEvent;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.regex.Matcher;

public class CommandHandler {

    private final Multimap<String, BotCommand> commands = ArrayListMultimap.create();

    public CommandHandler(Database database) {
        commands.put("*", new SetBotCommand());
        commands.put("*", new SetRegexBotCommand());
        commands.put("*", new UnsetBotCommand());
        commands.put("*", new TimeBotCommand());
        commands.put("*", new RegularBotCommand());
        commands.put("*", new SongBotCommand());

        try {
            Statement stmt = database.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM commands");
            while(rs.next()) {
                commands.put(rs.getString("channel_name"), new MessageBotCommand(rs.getString("command_name"), rs.getString("regex"), rs.getString("message"), UserLevel.fromId(rs.getInt("userLevel"))));
            }
            rs.close();
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Subscribe
    public void onChannelChat(IRCChannelChatEvent event) {
        if(!checkCommandList(event, commands.get("*"))) {
            checkCommandList(event, commands.get(event.channel.getName()));
        }
    }

    private boolean checkCommandList(IRCChannelChatEvent event, Collection<BotCommand> commands) {
        Matcher matcher = null;
        for(BotCommand command : commands) {
            if(!passesUserLevel(event.sender, event.channel, command.minUserLevel)) {
                continue;
            }
            if(matcher == null) {
                matcher = command.pattern.matcher(event.message);
            } else {
                matcher.usePattern(command.pattern);
            }
            if(matcher.find()) {
                command.execute(event.channel, event.sender, matcher.groupCount() > 0 ? matcher.group(1).split(" ") : new String[0]);
                return true;
            }
        }
        return false;
    }

    public boolean registerMessageCommand(IRCChannel channel, MessageBotCommand botCommand) {
        try {
            PreparedStatement stmtRegisterCommand = BalyBot.instance.getDatabase().stmtRegisterCommand;
            stmtRegisterCommand.setString(1, channel.getName());
            stmtRegisterCommand.setString(2, botCommand.name);
            stmtRegisterCommand.setString(3, botCommand.regex);
            stmtRegisterCommand.setString(4, botCommand.message);
            stmtRegisterCommand.setInt(5, botCommand.minUserLevel.ordinal());
            stmtRegisterCommand.executeUpdate();
            commands.put(channel.getName(), botCommand);
            return true;
        } catch (SQLException e) {
            return false;
        }
    }

    public boolean unregisterCommand(IRCChannel channel, BotCommand botCommand) {
        try {
            PreparedStatement stmtUnregisterCommand = BalyBot.instance.getDatabase().stmtUnregisterCommand;
            stmtUnregisterCommand.setString(1, channel.getName());
            stmtUnregisterCommand.setString(2, botCommand.name);
            stmtUnregisterCommand.executeUpdate();
            commands.remove(channel.getName(), botCommand);
            return true;
        } catch (SQLException e) {
            return false;
        }
    }

    public boolean passesUserLevel(IRCUser user, IRCChannel channel, UserLevel level) {
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

    public Collection<BotCommand> getGlobalCommands() {
        return commands.get("*");
    }

    public Collection<BotCommand> getChannelCommands(IRCChannel channel) {
        return commands.get(channel.getName());
    }
}
