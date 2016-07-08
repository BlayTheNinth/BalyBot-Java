package net.blay09.balybot.command;

import lombok.Getter;
import net.blay09.balybot.Config;
import net.blay09.javatmi.TwitchUser;

import java.util.regex.Pattern;

public abstract class BotCommand {

    @Getter protected final String name;
    @Getter private final Pattern pattern;
    @Getter private final String condition;
    @Getter private final String whisperTo;
    private int minUserLevel;
    private long lastExecution;

    public BotCommand(String name, String regex, int minUserLevel) {
        this(name, regex, minUserLevel, null, null);
    }

    public BotCommand(String name, String regex, int minUserLevel, String condition, String whisperTo) {
        this.name = name;
        this.pattern = Pattern.compile(regex);
        this.minUserLevel = minUserLevel;
        this.condition = condition;
        this.whisperTo = whisperTo;
    }

    public String execute(String channelName, TwitchUser sender, String message, String[] args, int depth, boolean ignoreCooldown) {
        long now = System.currentTimeMillis();
        if(ignoresCommandCooldown() || ignoreCooldown || now - lastExecution >= Config.getGlobalInt("command_cooldown", 30)) {
            lastExecution = now;
            return execute(channelName, sender, message, args, depth);
        }
        return null;
    }

    public abstract String execute(String channelName, TwitchUser sender, String message, String[] args, int depth);

    public void setUserLevel(UserLevel minUserLevel) {
        this.minUserLevel = minUserLevel.getLevel();
    }

    public UserLevel getUserLevel() {
        return UserLevel.getMinimumUserLevel(minUserLevel);
    }

    public String getCommandSyntax() {
        return "<no syntax set>";
    }

    public boolean ignoresCommandCooldown() {
        return false;
    }

    public int getUserLevelValue() {
        return minUserLevel;
    }
}
