package net.blay09.balybot.module.manager;

import com.google.common.collect.Lists;
import net.blay09.balybot.command.BotCommand;
import net.blay09.balybot.module.Module;
import net.blay09.balybot.module.ModuleDef;

import java.util.Collection;
import java.util.List;

public class ManagerModule extends ModuleDef {

    public ManagerModule() {
        super("manager", "Manager Module", "Base module of the bot. Provides commands to manage modules and config options.");
    }

    @Override
    public Collection<BotCommand> createCommands(Module module) {
        List<BotCommand> commands = Lists.newArrayList();
        commands.add(new ModuleCommand(module));
        commands.add(new ConfigCommand(module));
        return commands;
    }

}
