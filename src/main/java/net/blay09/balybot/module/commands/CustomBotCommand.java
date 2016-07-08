package net.blay09.balybot.module.commands;

import java.util.regex.Pattern;

public class CustomBotCommand extends CustomBotRegexCommand {

    public CustomBotCommand(String name, String message, int minUserLevel, String condition, String whisperTo) {
        super(name, "^!" + Pattern.quote(name) + "(?:\\s(.*)|$)", message, minUserLevel, condition, whisperTo);
    }

}
