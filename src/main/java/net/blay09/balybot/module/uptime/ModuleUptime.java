package net.blay09.balybot.module.uptime;

import com.google.common.eventbus.EventBus;
import net.blay09.balybot.module.Module;

public class ModuleUptime extends Module {
    public ModuleUptime(String context, String prefix) {
        super(context, prefix);
    }

    @Override
    public void activate(EventBus eventBus) {
        registerCommand(new UptimeBotCommand(prefix));
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
        return "Utilizes the NightDev BTTV API to add an !uptime command showing how long the channel has been live.";
    }
}
