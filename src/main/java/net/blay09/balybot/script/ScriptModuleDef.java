package net.blay09.balybot.script;

import com.google.common.collect.Lists;
import jdk.nashorn.api.scripting.ScriptObjectMirror;
import net.blay09.balybot.command.UserLevel;
import net.blay09.balybot.command.BotCommand;
import net.blay09.balybot.module.ConfigEntry;
import net.blay09.balybot.module.Module;
import net.blay09.balybot.module.ModuleDef;

import javax.script.Invocable;
import javax.script.ScriptException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class ScriptModuleDef extends ModuleDef {

    private ScriptObjectMirror jsCommands;
    private ScriptObjectMirror jsEvents;

    private ScriptModuleDef(String id, String name, String desc, Invocable invocable) throws ScriptException {
        super(id, name, desc);

        try {
            ScriptObjectMirror jsConfig = (ScriptObjectMirror) invocable.invokeFunction("configure");
            for (Object object : jsConfig.values()) {
                ScriptObjectMirror jsConfigEntry = (ScriptObjectMirror) object;
                String configEntryName = (String) jsConfigEntry.get("name");
                String configEntryDefault = (String) jsConfigEntry.get("value");
                String configEntryDesc = jsConfigEntry.containsKey("desc") ? (String) jsConfigEntry.get("desc") : "<no description>";
                addConfigEntry(new ConfigEntry(this, configEntryName, configEntryDefault, configEntryDesc));
            }
        } catch (NoSuchMethodException ignored) {}

        try {
            jsCommands = (ScriptObjectMirror) invocable.invokeFunction("commands");
        } catch (NoSuchMethodException ignored) {}

        try {
            jsEvents = (ScriptObjectMirror) invocable.invokeFunction("events");
        } catch (NoSuchMethodException ignored) {}
    }

    public Collection<ScriptEventHandler> createEventHandlers(Module module) {
        if(jsEvents == null) {
            return Collections.emptyList();
        }
		List<ScriptEventHandler> eventHandlers = Lists.newArrayList();
        for(Map.Entry<String, Object> entry : jsEvents.entrySet()) {
			eventHandlers.add(ScriptManager.getInstance().registerEventHandler(module, EventType.fromName(entry.getKey()), (ScriptObjectMirror) entry.getValue()));
        }
		return eventHandlers;
    }

    public Collection<BotCommand> createCommands(Module module) {
        if(jsCommands == null) {
            return Collections.emptyList();
        }
        List<BotCommand> commandList = Lists.newArrayList();
        for(Object object : jsCommands.values()) {
            ScriptObjectMirror jsCommand = (ScriptObjectMirror) object;
            String commandName = (String) jsCommand.get("name");
            ConfigEntry userLevelConfig = getConfigEntry("userlevel." + commandName);
            if(userLevelConfig == null) {
                userLevelConfig = addConfigEntry(new ConfigEntry(this, "userlevel." + commandName, "mod", "The minimum user level required to run the " + module.getPrefix() + commandName + " command."));
            }
            UserLevel userLevel = UserLevel.fromName(userLevelConfig.getString(module.getChannelName()));
            if(userLevel == null) {
                userLevel = UserLevel.MOD;
            }
            commandList.add(new ScriptBotCommand(module, userLevel.getLevel(), jsCommand));
        }
        return commandList;
    }

    public static ModuleDef fromScript(Invocable invocable) throws ScriptException, NoSuchMethodException {
        ScriptObjectMirror jsModule = (ScriptObjectMirror) invocable.invokeFunction("module");
        String id = (String) jsModule.get("id");
        String name = (String) jsModule.get("name");
        String desc = jsModule.containsKey("desc") ? (String) jsModule.get("desc") : "<no description>";
        return new ScriptModuleDef(id, name, desc, invocable);
    }
}
