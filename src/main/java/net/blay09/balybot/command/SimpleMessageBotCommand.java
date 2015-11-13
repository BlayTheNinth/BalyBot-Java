package net.blay09.balybot.command;

import net.blay09.balybot.UserLevel;
import net.blay09.balybot.irc.IRCChannel;

public class SimpleMessageBotCommand extends MessageBotCommand {

    public SimpleMessageBotCommand(String name, String message, UserLevel minUserLevel, String condition, String whisperTo) {
        super(name, "^!" + name + "(?:\\s(.*)|$)", message, minUserLevel, condition, whisperTo);
    }

}
