package net.blay09.balybot.module.song;

import com.google.common.eventbus.EventBus;
import net.blay09.balybot.module.Module;

public class ModuleSong extends Module {

    public ModuleSong(String context, String prefix) {
        super(context, prefix);
    }

    @Override
    public void activate(EventBus eventBus) {
        registerCommand(new SongBotCommand(prefix));
    }

    @Override
    public void deactivate(EventBus eventBus) {
    }

    @Override
    public String getModuleCode() {
        return "song";
    }

    @Override
    public String getModuleName() {
        return "Song Module";
    }

    @Override
    public String getModuleDescription() {
        return "Looks up the title of the currently playing song from a configured file (song_file).";
    }
}
