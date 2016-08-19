package net.blay09.balybot.module;

import com.google.common.collect.Lists;
import jdk.nashorn.api.scripting.ScriptObjectMirror;
import lombok.Getter;
import net.blay09.balybot.command.BotCommand;
import net.blay09.balybot.impl.api.Channel;
import net.blay09.balybot.script.ScriptModuleDef;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class Module {

    private final List<BotCommand> commands = Lists.newArrayList();
    @Getter
    private final ModuleDef definition;
    @Getter
    private final Channel channel;
    @Getter
    private final String prefix;

    public Module(ModuleDef definition, Channel channel) {
        this.definition = definition;
        this.channel = channel;
        this.prefix = definition.getConfigEntry("prefix").getString(channel);
        if(definition instanceof ScriptModuleDef) {
            ((ScriptModuleDef) definition).createEventHandlers(this);
        }
        commands.addAll(definition.createCommands(this));
        if(definition.getCommandSorting() != null) {
            Collections.sort(commands, definition.getCommandSorting());
        }
    }

    public String getId() {
        return definition.getId();
    }

    public Collection<BotCommand> getCommands() {
        return commands;
    }

	public void unregisterCommand(BotCommand command) {
		commands.remove(command);
	}

	public void registerCommand(BotCommand command) {
		commands.add(command);
        if(definition.getCommandSorting() != null) {
            Collections.sort(commands, definition.getCommandSorting());
        }
	}

    public void pushConfigVariable(ScriptObjectMirror function) {
        StringBuilder sb = new StringBuilder("{");
        for(ConfigEntry configEntry : definition.getConfigEntries()) {
            if(sb.length() > 1) {
                sb.append(",");
            }
            sb.append("'").append(configEntry.name).append("':'").append(configEntry.getString(channel)).append("'");
        }
        sb.append("};");
        function.eval("var config = " + sb.toString());
    }
}