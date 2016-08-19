package net.blay09.balybot.script;

import jdk.nashorn.api.scripting.ScriptObjectMirror;
import net.blay09.balybot.command.SimpleMessageBotCommand;
import net.blay09.balybot.impl.api.Channel;
import net.blay09.balybot.impl.api.User;
import net.blay09.balybot.module.Module;
import net.blay09.javatmi.TwitchUser;

public class ScriptBotCommand extends SimpleMessageBotCommand {

    private final ScriptObjectMirror function;
    private final Module module;
    private final String usage;

    public ScriptBotCommand(Module module, int minUserLevel, ScriptObjectMirror jsCommand) {
        super((String) jsCommand.get("name"), module.getPrefix() + jsCommand.get("name"), minUserLevel, null, null);
        this.module = module;
        this.function = (ScriptObjectMirror) jsCommand.get("func");
        this.usage = (String) jsCommand.get("usage");
    }

    @Override
    public String execute(Channel channel, User sender, String message, String[] args, int depth) {
        ScriptManager.getInstance().setCurrentScript(module);
        module.pushConfigVariable(function);
		return (String) ScriptManager.getInstance().callSafely(function, this, channel, sender, args);
    }

    @Override
    public String getCommandSyntax() {
        return module.getPrefix() + name + (usage != null ? " " + usage : null);
    }

}
