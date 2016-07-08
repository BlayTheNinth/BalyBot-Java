package net.blay09.balybot.command;

public class SimpleMessageBotCommand extends MessageBotCommand {

    public SimpleMessageBotCommand(String name, String message, int minUserLevel, String condition, String whisperTo) {
        super(name, "^!" + name + "(?:\\s(.*)|$)", message, minUserLevel, condition, whisperTo);
    }

}
