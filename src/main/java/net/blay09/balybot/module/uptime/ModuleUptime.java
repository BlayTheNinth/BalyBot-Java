package net.blay09.balybot.module.uptime;

import com.google.common.eventbus.EventBus;
import net.blay09.balybot.module.ConfigEntry;
import net.blay09.balybot.module.Module;

public class ModuleUptime extends Module {

    public ConfigEntry UL_UPTIME = new ConfigEntry(this, "ul.uptime", "The minimum user level for the !uptime command.", "all");

    public ModuleUptime(String context, String prefix) {
        super(context, prefix);
    }

    @Override
    public void activate(EventBus eventBus) {
        registerCommand(new UptimeBotCommand(prefix, UL_UPTIME.getUserLevel(context)));
    }

    @Override
    public void deactivate(EventBus eventBus) {

    }

    @Override
    public String getModuleCode() {
        return "uptime";
    }

    @Override
    public String getModuleName() {
        return "Uptime Module";
    }

    @Override
    public String getModuleDescription() {
        return "Provides the !uptime command that displays the time the channel has been live for";
    }
}
