package net.blay09.balybot.module.calc;

import com.google.common.eventbus.EventBus;
import net.blay09.balybot.module.Module;

public class ModuleMath extends Module {

    public ModuleMath(String context, char prefix) {
        super(context, prefix);
    }

    @Override
    public void activate(EventBus eventBus) {
        registerCommand(new MathBotCommand(prefix));
    }

    @Override
    public void deactivate(EventBus eventBus) {
    }

}
