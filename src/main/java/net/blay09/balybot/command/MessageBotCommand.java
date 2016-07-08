package net.blay09.balybot.command;

import lombok.Getter;
import net.blay09.balybot.CommandHandler;
import net.blay09.javatmi.TwitchUser;

public class MessageBotCommand extends BotCommand {

    @Getter private final String commandMessage;

    public MessageBotCommand(String name, String regex, String commandMessage, int minUserLevel, String condition, String whisperTo) {
        super(name, regex, minUserLevel, condition, whisperTo);
        this.commandMessage = commandMessage;
    }

    @Override
    public String execute(String channelName, TwitchUser sender, String message, String[] args, int depth) {
        return CommandHandler.resolveVariables(commandMessage, this, channelName, sender, message, args, depth);
    }

}
