package net.blay09.balybot.script;

import jdk.nashorn.api.scripting.ScriptObjectMirror;
import net.blay09.balybot.module.Module;
import net.blay09.balybot.module.ModuleContext;

public class ScriptEventHandler {

	private final Module module;
	private final ModuleContext context;
	private final String eventType;
	private final ScriptObjectMirror callback;

	public ScriptEventHandler(Module module, ModuleContext context, String eventType, ScriptObjectMirror callback) {
		this.module = module;
		this.context = context;
		this.eventType = eventType;
		this.callback = callback;
	}

	public Module getModule() {
		return module;
	}

	public String getEventType() {
		return eventType;
	}

	public ScriptObjectMirror getCallback() {
		return callback;
	}

	public ModuleContext getContext() {
		return context;
	}
}
