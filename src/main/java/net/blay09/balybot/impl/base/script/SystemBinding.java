package net.blay09.balybot.impl.base.script;

import jdk.nashorn.api.scripting.ScriptObjectMirror;
import net.blay09.balybot.script.ScriptManager;

public class SystemBinding {

    public long currentTimeMillis() {
        return System.currentTimeMillis();
    }

    public void setInterval(ScriptObjectMirror function, int interval) {
        ScriptManager.getInstance().getTimerHandler().addInterval(function, interval);
    }

    public void setTimeout(ScriptObjectMirror function, int timeout) {
        ScriptManager.getInstance().getTimerHandler().addTimeout(function, timeout);
    }

}
