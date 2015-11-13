package net.blay09.balybot.module.uptime;

import com.google.common.eventbus.EventBus;
import net.blay09.balybot.module.Module;

public class ModuleUptime extends Module {
    public ModuleUptime(String context, char prefix) {
        super(context, prefix);
    }

    @Override
    public void activate(EventBus eventBus) {
        registerCommand(new UptimeBotCommand(prefix));
    }

    @Override
    public void deactivate(EventBus eventBus) {

    }
}
