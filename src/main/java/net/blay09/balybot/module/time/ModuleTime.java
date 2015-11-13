package net.blay09.balybot.module.time;

import com.google.common.eventbus.EventBus;
import net.blay09.balybot.module.Module;

public class ModuleTime extends Module {

    public ModuleTime(String context, String prefix) {
        super(context, prefix);
    }

    @Override
    public void activate(EventBus eventBus) {
        registerCommand(new TimeBotCommand(prefix));
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
        return "Provides a !time command that prints the current time in a specific time zone. If time_timezone is configured, it will use that when no arguments are given.";
    }
}
