package net.blay09.balybot.module.time;

import com.google.common.eventbus.EventBus;
import net.blay09.balybot.module.ConfigEntry;
import net.blay09.balybot.module.Module;

public class ModuleTime extends Module {

    public ConfigEntry TIMEZONE = new ConfigEntry(this, "timezone", "The timezone to use, see https://docs.oracle.com/javase/7/docs/api/java/util/TimeZone.html for the format.", "");
    public ConfigEntry UL_TIME = new ConfigEntry(this, "ul.time", "The minimum user level for the !time command.", "reg");

    public ModuleTime(String context, String prefix) {
        super(context, prefix);
    }

    @Override
    public void activate(EventBus eventBus) {
        registerCommand(new TimeBotCommand(this, prefix, UL_TIME.getUserLevel(context)));
    }

    @Override
    public void deactivate(EventBus eventBus) {
    }

    @Override
    public String getModuleCode() {
        return "time";
    }

    @Override
    public String getModuleName() {
        return "Time Module";
    }

    @Override
    public String getModuleDescription() {
        return "Provides a !time command that prints the current time in a specific time zone. If time.timezone is configured, it will use that when no arguments are given.";
    }
}
