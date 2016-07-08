package net.blay09.balybot.script;

import jdk.nashorn.api.scripting.ScriptObjectMirror;
import net.blay09.balybot.module.Module;

public class ScriptEventHandler {

	private final Module module;
	private final EventType eventType;
	private final ScriptObjectMirror callback;

	public ScriptEventHandler(Module module, EventType eventType, ScriptObjectMirror callback) {
		this.module = module;
		this.eventType = eventType;
		this.callback = callback;
	}

	public Module getModule() {
		return module;
	}

	public EventType getEventType() {
		return eventType;
	}

	public ScriptObjectMirror getCallback() {
		return callback;
	}
}
