package net.blay09.balybot.module;

import net.blay09.balybot.Config;
import net.blay09.balybot.command.UserLevel;

public class ConfigEntry {
    public final ModuleDef module;
    public final String name;
    public final String defaultVal;
    public final String description;

    public ConfigEntry(ModuleDef module, String name, String defaultVal, String description) {
        this.name = name;
        this.module = module;
        this.defaultVal = defaultVal;
        this.description = description;
    }

    public String getString(String channelName) {
        return Config.getChannelString(channelName, module.getId() + "." + name, defaultVal);
    }

    public int getInt(String channelName) {
        return Config.getChannelInt(channelName, module.getId() + "." + name, Integer.parseInt(defaultVal));
    }

    public UserLevel getUserLevel(String channelName) {
        UserLevel userLevel = UserLevel.fromName(Config.getChannelString(channelName, module.getId() + "." + name, defaultVal));
        if(userLevel == null) {
            userLevel = UserLevel.fromName(defaultVal);
        }
        return userLevel;
    }

    public boolean getBoolean(String channelname) {
        return Boolean.parseBoolean(getString(channelname));
    }
}
