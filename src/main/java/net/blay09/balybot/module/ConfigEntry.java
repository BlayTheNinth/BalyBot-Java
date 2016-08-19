package net.blay09.balybot.module;

import net.blay09.balybot.BalyBot;
import net.blay09.balybot.ChannelManager;
import net.blay09.balybot.impl.api.Channel;
import net.blay09.balybot.impl.api.UserLevel;

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

    public String getString(Channel channel) {
        return ChannelManager.getChannelString(channel, module.getId() + "." + name, defaultVal);
    }

    public int getInt(Channel channel) {
        return ChannelManager.getChannelInt(channel, module.getId() + "." + name, Integer.parseInt(defaultVal));
    }

    public UserLevel getUserLevel(Channel channel) {
        UserLevel userLevel = BalyBot.getUserLevelRegistry().fromName(ChannelManager.getChannelString(channel, module.getId() + "." + name, defaultVal));
        if(userLevel == null) {
            userLevel = BalyBot.getUserLevelRegistry().fromName(defaultVal);
        }
        return userLevel;
    }

    public boolean getBoolean(Channel channel) {
        return Boolean.parseBoolean(getString(channel));
    }
}
