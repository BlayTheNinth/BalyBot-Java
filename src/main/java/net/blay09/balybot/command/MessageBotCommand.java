package net.blay09.balybot.command;

import net.blay09.balybot.UserLevel;
import net.blay09.balybot.irc.IRCChannel;
import net.blay09.balybot.irc.IRCUser;

public class MessageBotCommand extends BotCommand {

    public final String message;

    public MessageBotCommand(String name, String regex, String message, UserLevel minUserLevel) {
        super(name, regex, minUserLevel);
        this.message = message;
    }

    @Override
    public void execute(IRCChannel channel, IRCUser sender, String[] args) {
        String msg = message;
        msg = msg.replace("{SENDER}", sender.getName());
        for(int i = 0; i < args.length; i++) {
            msg = msg.replace("{" + i + "}", args[i]);
        }
        channel.message(msg);
    }

}
