package net.blay09.balybot.module;

import com.google.common.collect.Maps;
import lombok.Getter;
import net.blay09.balybot.command.BotCommand;
import net.blay09.balybot.impl.api.Channel;

import java.util.Collection;
import java.util.Comparator;
import java.util.Map;

public abstract class ModuleDef {

    private final Map<String, ConfigEntry> config = Maps.newHashMap();

    @Getter private final String id;
    @Getter private final String name;
    @Getter private final String description;
    @Getter protected Comparator<BotCommand> commandSorting;

    public ModuleDef(String id, String name, String description) {
        this.id = id;
        this.name = name;
        this.description = description;

        addConfigEntry(new ConfigEntry(this, "prefix", "!", "The prefix to use for commands of this module."));
    }

    public Module create(Channel channel) {
        return new Module(this, channel);
    }

    protected ConfigEntry addConfigEntry(ConfigEntry entry) {
        config.put(entry.name, entry);
        return entry;
    }

    public ConfigEntry getConfigEntry(String name) {
        return config.get(name);
    }

    public Collection<ConfigEntry> getConfigEntries() {
        return config.values();
    }

    public abstract Collection<BotCommand> createCommands(Module module);

}
