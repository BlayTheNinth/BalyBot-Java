package net.blay09.balybot.module.expr;

import com.google.common.eventbus.EventBus;
import net.blay09.balybot.module.Module;

public class ModuleExpr extends Module {

    public ModuleExpr(String context, String prefix) {
        super(context, prefix);
    }

    @Override
    public void activate(EventBus eventBus) {
        registerCommand(new ExprBotCommand(prefix));
    }

    @Override
    public void deactivate(EventBus eventBus) {
    }

    @Override
    public String getModuleCode() {
        return "expr";
    }

    @Override
    public String getModuleName() {
        return "Expression Module";
    }

    @Override
    public String getModuleDescription() {
        return "Provides a silly !expr command that can calculate any expression that works within the command conditions. Currently set to reg+ permissions and not configurable.";
    }
}
