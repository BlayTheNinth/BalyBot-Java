package net.blay09.balybot.command;

import net.blay09.balybot.CommandHandler;
import net.blay09.balybot.expr.ExpressionLibrary;
import net.blay09.balybot.twitch.TwitchAPI;
import net.blay09.balybot.UserLevel;
import net.blay09.balybot.irc.IRCChannel;
import net.blay09.balybot.irc.IRCUser;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MessageBotCommand extends BotCommand {

    public final String commandMessage;

    public MessageBotCommand(String name, String regex, String commandMessage, UserLevel minUserLevel, String condition, String whisperTo) {
        super(name, regex, minUserLevel, condition, whisperTo);
        this.commandMessage = commandMessage;
    }

    @Override
    public String execute(IRCChannel channel, IRCUser sender, String message, String[] args, int depth) {
        return CommandHandler.resolveVariables(commandMessage, this, channel, sender, message, args, depth);
    }

}
