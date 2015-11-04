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

    public BotCommand(String name, String regex, UserLevel minUserLevel) {
        this(name, regex, minUserLevel, null);
    }

    public BotCommand(String name, String regex, UserLevel minUserLevel, String condition) {
        this.name = name;
        this.regex = regex;
        this.pattern = Pattern.compile(regex);
        this.minUserLevel = minUserLevel;
        this.condition = condition;
    }

    public abstract void execute(IRCChannel channel, IRCUser sender, String[] args);

    public void setId(int id) {
        this.id = id;
    }

    public void setUserLevel(UserLevel minUserLevel) {
        this.minUserLevel = minUserLevel;
    }
}
