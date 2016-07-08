package net.blay09.balybot;

import lombok.extern.log4j.Log4j2;
import net.blay09.javairc.IRCUser;
import net.blay09.javatmi.TwitchUser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

@Log4j2
public class CommandLineHandler {

    public static void handleCommands() {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        while(true) {
            try {
                String s = reader.readLine();
                if(s != null) {
                    if (s.equals("/quit")) {
                        BalyBot.getInstance().getClient().disconnect();
                        break;
                    } else if (s.startsWith("/join ")) {
                        ChannelManager.joinChannel(s.substring(6));
                    } else if(s.startsWith("/part ")) {
                        ChannelManager.partChannel(s.substring(6));
                    } else {
                        BalyBot.getInstance().getListener().onChatMessage(BalyBot.getInstance().getClient(), "#blay09", new TwitchUser(new IRCUser("blay09", "", "")), s);
                    }
                }
            } catch (IOException e) {
                log.error(e);
            }
            try {
                Thread.sleep(10);
            } catch (InterruptedException ignored) {}
        }
    }

}
