package net.blay09.balybot.module.uptime;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import net.blay09.balybot.UserLevel;
import net.blay09.balybot.command.BotCommand;
import net.blay09.balybot.irc.IRCChannel;
import net.blay09.balybot.irc.IRCUser;

import java.io.IOException;
import java.net.URL;

public class UptimeBotCommand extends BotCommand {

    public UptimeBotCommand() {
        super("uptime", "^!uptime\\s?(.*)", UserLevel.ALL);
    }

    @Override
    public void execute(IRCChannel channel, IRCUser sender, String[] args) {
        try {
            channel.message(Resources.toString(new URL("https://nightdev.com/hosted/uptime.php?channel=" + channel.getName().substring(1)), Charsets.UTF_8));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
