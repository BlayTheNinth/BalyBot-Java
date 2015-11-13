package net.blay09.balybot.command;

import net.blay09.balybot.UserLevel;
import net.blay09.balybot.irc.IRCChannel;
import net.blay09.balybot.irc.IRCUser;

import java.util.regex.Pattern;

public abstract class BotCommand {

    public int id;
    public final String name;
    public final String regex;
    public final Pattern pattern;
    public UserLevel minUserLevel;
    public final String condition;
    public final String whisperTo;

    public BotCommand(String name, String regex, UserLevel minUserLevel) {
        this(name, regex, minUserLevel, null, null);
    }

    public BotCommand(String name, String regex, UserLevel minUserLevel, String condition, String whisperTo) {
        this.name = name;
        this.regex = regex;
        this.pattern = Pattern.compile(regex);
        this.minUserLevel = minUserLevel;
        this.condition = condition;
        this.whisperTo = whisperTo;
    }

    public abstract String execute(IRCChannel channel, IRCUser sender, String message, String[] args, int depth);

    public void setId(int id) {
        this.id = id;
    }

    public void setUserLevel(UserLevel minUserLevel) {
        this.minUserLevel = minUserLevel;
    }
}
