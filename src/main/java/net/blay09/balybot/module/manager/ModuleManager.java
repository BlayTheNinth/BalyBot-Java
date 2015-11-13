package net.blay09.balybot.module.manager;

import com.google.common.eventbus.EventBus;
import net.blay09.balybot.module.Module;

public class ModuleManager extends Module {

    public ModuleManager(String context, String prefix) {
        super(context, prefix);
    }

    @Override
    public void activate(EventBus eventBus) {
        registerCommand(new SetBotCommand(prefix));
        registerCommand(new SetRegexBotCommand(prefix));
        registerCommand(new SetUserLevelCommand(prefix));
        registerCommand(new UnsetBotCommand(prefix));
        registerCommand(new RegularBotCommand(prefix));
        registerCommand(new ConfigBotCommand(prefix));
    }

    @Override
    public void deactivate(EventBus eventBus) {
    }

    @Override
    public String getModuleCode() {
        return "manager";
    }

    @Override
    public String getModuleName() {
        return "Manager Module";
    }

    @Override
    public String getModuleDescription() {
        return "Base module of the bot. Provides commands to define and edit other commands, as well as regular handling using !reg.";
    }
}
