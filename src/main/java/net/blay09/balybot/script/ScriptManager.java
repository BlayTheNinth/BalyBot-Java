package net.blay09.balybot.script;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import jdk.nashorn.api.scripting.ScriptObjectMirror;
import jdk.nashorn.internal.runtime.ECMAException;
import lombok.extern.log4j.Log4j2;
import net.blay09.balybot.BalyBot;
import net.blay09.balybot.ChannelManager;
import net.blay09.balybot.impl.api.BotImplementation;
import net.blay09.balybot.module.Module;
import net.blay09.balybot.module.ModuleDef;

import javax.script.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Collection;
import java.util.List;

@Log4j2
public class ScriptManager {

    private static ScriptManager instance;
    private Module currentScript;

    public static ScriptManager getInstance() {
        if(instance == null) {
            instance = new ScriptManager();
        }
        return instance;
    }

    private final Multimap<String, ScriptEventHandler> callbacks = ArrayListMultimap.create();

    public Collection<ModuleDef> loadModules() {
        List<ModuleDef> modules = Lists.newArrayList();
        ScriptEngineManager factory = new ScriptEngineManager();
        File[] files = new File("modules").listFiles((dir, name) -> name.endsWith(".js") && !name.equals("types.js"));
        if(files != null) {
            for(File file : files) {
                try {
                    ScriptEngine engine = factory.getEngineByName("JavaScript");
                    loadBindings(engine);
                    engine.eval(new FileReader(file));
                    modules.add(ScriptModuleDef.fromScript((Invocable) engine));
                } catch (ScriptException | FileNotFoundException | NoSuchMethodException e) {
                    log.error(e);
                }
            }
        }
        return modules;
    }

    private void loadBindings(ScriptEngine engine) {
        Bindings globalBindings = engine.createBindings();
		for(BotImplementation impl : BalyBot.getInstance().getImplementations()) {
			impl.registerBindings(globalBindings);
		}
        engine.setBindings(globalBindings, ScriptContext.GLOBAL_SCOPE);
    }

    public ScriptEventHandler registerEventHandler(Module module, String eventType, ScriptObjectMirror callback) {
		ScriptEventHandler handler = new ScriptEventHandler(module, eventType, callback);
        callbacks.put(eventType, handler);
		return handler;
    }

    public void publishEvent(String eventType, Object... args) {
        for(ScriptEventHandler handler : callbacks.get(eventType)) {
            setCurrentScript(handler.getModule());
			handler.getModule().pushConfigVariable(handler.getCallback());
			callSafely(handler.getCallback(), null, args);
        }
    }

	public Object callSafely(ScriptObjectMirror function, Object thiz, Object... args) {
		try {
			return function.call(thiz, args);
		} catch (ECMAException e) {
			if(e.getColumnNumber() != -1) {
				log.error("{}.js:{} near {}: {}", ScriptManager.getInstance().getCurrentScript().getId(), e.getLineNumber(), e.getColumnNumber(), e.getMessage());
			} else {
				log.error("{}.js:{}: {}", ScriptManager.getInstance().getCurrentScript().getId(), e.getLineNumber(), e.getMessage());
			}
			return null;
		}
	}

    public void setCurrentScript(Module currentScript) {
        this.currentScript = currentScript;
    }

	public Module getCurrentScript() {
		return currentScript;
	}
}
