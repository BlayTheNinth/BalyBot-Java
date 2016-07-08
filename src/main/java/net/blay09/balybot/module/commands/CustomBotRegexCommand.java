package net.blay09.balybot.module.commands;

import lombok.Getter;
import lombok.Setter;
import net.blay09.balybot.command.MessageBotCommand;

public class CustomBotRegexCommand extends MessageBotCommand {

    @Getter @Setter private int id;

    public CustomBotRegexCommand(String name, String pattern, String message, int minUserLevel, String condition, String whisperTo) {
        super(name, pattern, message, minUserLevel, condition, whisperTo);
    }

}
