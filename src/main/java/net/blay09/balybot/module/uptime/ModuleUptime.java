package net.blay09.balybot.module.uptime;

import com.google.common.eventbus.EventBus;
import net.blay09.balybot.module.ConfigEntry;
import net.blay09.balybot.module.Module;

public class ModuleUptime extends Module {

    public ConfigEntry MSG_UPTIME_PREFIX = new ConfigEntry(this, "msg.uptime_prefix", "The prefix put in front of the uptime.", "Stream uptime: ");
    public ConfigEntry MSG_NOT_LIVE = new ConfigEntry(this, "msg.not_live", "The message displayed if the channel is not live.", "This channel is not live.");
    public ConfigEntry UL_UPTIME = new ConfigEntry(this, "ul.uptime", "The minimum user level for the !uptime command.", "all");

    public ModuleUptime(String context, String prefix) {
        super(context, prefix);
    }

    @Override
    public void activate(EventBus eventBus) {
        registerCommand(new UptimeBotCommand(this, prefix, UL_UPTIME.getUserLevel(context)));
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
