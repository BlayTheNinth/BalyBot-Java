package net.blay09.balybot.command;

import lombok.Getter;
import net.blay09.balybot.impl.api.Channel;
import net.blay09.balybot.impl.api.User;

public class MessageBotCommand extends BotCommand {

    @Getter private final String commandMessage;

    public MessageBotCommand(String name, String regex, String commandMessage, int minUserLevel, String condition, String whisperTo) {
        super(name, regex, minUserLevel, condition, whisperTo);
        this.commandMessage = commandMessage;
    }

    @Override
    public String execute(Channel channel, User sender, String message, String[] args, int depth) {
        return CommandHandler.resolveVariables(commandMessage, this, channel, sender, message, args, depth);
    }

}
