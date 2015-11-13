package net.blay09.balybot.module.song;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import net.blay09.balybot.Config;
import net.blay09.balybot.UserLevel;
import net.blay09.balybot.command.BotCommand;
import net.blay09.balybot.irc.IRCChannel;
import net.blay09.balybot.irc.IRCUser;

import java.io.File;
import java.io.IOException;

public class SongBotCommand extends BotCommand {

    private final String prefix;

    public SongBotCommand(String prefix) {
        super("song", "^" + prefix + "song(?:\\s+(.*)|$)", UserLevel.ALL);
        this.prefix = prefix;
    }

    @Override
    public String getCommandSyntax() {
        return prefix + "song";
    }

    @Override
    public String execute(IRCChannel channel, IRCUser sender, String message, String[] args, int depth) {
        try {
            return Files.readFirstLine(new File(Config.getValue(channel.getName(), "song_file", "playing.txt")), Charsets.UTF_8);
        } catch (IOException e) {
            e.printStackTrace();
            return "Failed to grab song information, sorry :(";
        }
    }

}
