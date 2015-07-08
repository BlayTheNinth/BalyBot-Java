package net.blay09.balybot.command;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import net.blay09.balybot.Config;
import net.blay09.balybot.UserLevel;
import net.blay09.balybot.irc.IRCChannel;
import net.blay09.balybot.irc.IRCUser;
import org.apache.logging.log4j.core.util.FileUtils;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class SongBotCommand extends BotCommand {

    public SongBotCommand() {
        super("song", "^!song\\s?(.*)", UserLevel.ALL);
    }

    @Override
    public void execute(IRCChannel channel, IRCUser sender, String[] args) {
        try {
            channel.message(Files.readFirstLine(new File(Config.getValue(channel, "song_file", "playing.txt")), Charsets.UTF_8));
        } catch (IOException e) {
            channel.message("Failed to grab song information, sorry :3");
            e.printStackTrace();
        }
    }

}
