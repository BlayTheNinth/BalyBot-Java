package net.blay09.balybot.module.time;

import com.google.common.eventbus.EventBus;
import net.blay09.balybot.module.Module;

public class ModuleTime extends Module {

    public ModuleTime(String context, char prefix) {
        super(context, prefix);
    }

    @Override
    public void activate(EventBus eventBus) {
        registerCommand(new TimeBotCommand(prefix));
    }

    @Override
    public void deactivate(EventBus eventBus) {
    }

}
