package net.blay09.balybot.impl.base.script;

import net.blay09.balybot.command.BotCommand;

public class ErrorBinding {

    public String notEnoughParameters(BotCommand command) {
        return "Not enough parameters for " + command.getName() + " command. Syntax: " + command.getCommandSyntax();
    }

    public String invalidParameters(BotCommand command) {
        return "Invalid parameters for " + command.getName() + " command. Syntax: " + command.getCommandSyntax();
    }

}
