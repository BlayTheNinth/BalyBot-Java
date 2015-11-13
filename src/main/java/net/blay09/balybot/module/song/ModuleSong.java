package net.blay09.balybot.module.song;

import com.google.common.eventbus.EventBus;
import net.blay09.balybot.module.Module;

public class ModuleSong extends Module {

    public ModuleSong(String context, char prefix) {
        super(context, prefix);
    }

    @Override
    public void activate(EventBus eventBus) {
        registerCommand(new SongBotCommand(prefix));
    }

    @Override
    public void deactivate(EventBus eventBus) {
    }
    
}
