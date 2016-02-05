package net.blay09.balybot.module;

import net.blay09.balybot.Config;
import net.blay09.balybot.UserLevel;
import net.blay09.balybot.irc.IRCChannel;

public class ConfigEntry {
    public final Module module;
    public final String name;
    public final String description;
    public final String defaultVal;

    public ConfigEntry(Module module, String name, String description, String defaultVal) {
        this.module = module;
        this.name = name;
        this.description = description;
        this.defaultVal = defaultVal;
        module.addConfigEntry(this);
    }

    public String getString(IRCChannel channel) {
        return getString(channel.getName());
    }

    public String getString(String channelName) {
        return Config.getValue(channelName, module.getModuleCode() + "." + name, defaultVal);
    }

    public int getInt(IRCChannel channel) {
        try {
            return Integer.parseInt(Config.getValue(channel, module.getModuleCode() + "." + name, defaultVal));
        } catch (NumberFormatException e) {
            return Integer.parseInt(defaultVal);
        }
    }

    public UserLevel getUserLevel(String channelName) {
        UserLevel userLevel = UserLevel.fromName(Config.getValue(channelName, module.getModuleCode() + "." + name, defaultVal));
        if(userLevel == null) {
            userLevel = UserLevel.fromName(defaultVal);
        }
        return userLevel;
    }

    public boolean getBoolean(IRCChannel channel) {
        return Boolean.parseBoolean(getString(channel));
    }
}
