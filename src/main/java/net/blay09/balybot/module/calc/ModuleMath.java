package net.blay09.balybot.module.calc;

import com.google.common.eventbus.EventBus;
import net.blay09.balybot.module.Module;

public class ModuleMath extends Module {

    public ModuleMath(String context, String prefix) {
        super(context, prefix);
    }

    @Override
    public void activate(EventBus eventBus) {
        registerCommand(new MathBotCommand(prefix));
    }

    @Override
    public void deactivate(EventBus eventBus) {
    }

    @Override
    public String getModuleCode() {
        return "math";
    }

    @Override
    public String getModuleName() {
        return "Math Module";
    }

    @Override
    public String getModuleDescription() {
        return "Provides a silly !math command that can calculate any expression that works within the command conditions. Currently set to reg+ permissions and not configurable.";
    }
}
