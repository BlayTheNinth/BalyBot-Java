package net.blay09.balybot;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import net.blay09.balybot.irc.event.TwitchHostStartEvent;

// TODO move into separate module
public class HostNotifier {

    private static final LinkFilter instance = new LinkFilter();

    public static void load(Database database, EventBus eventBus) {
        eventBus.register(instance);
    }

    @Subscribe
    public void onStartHosting(TwitchHostStartEvent event) {
        if(Config.getValue(event.channel.getName(), "host_notifier", "false").equals("true")) {
            if(event.viewerCount >= Integer.parseInt(Config.getValue(event.channel.getName(), "host_notifier_minviewers", "2"))) {
                String message = Config.getValue(event.channel.getName(), "host_notifier_message", "Guys, guys!! {HOSTNAME} is hosting us! Quick, hug them!");
                message = message.replace("{HOSTCHANNEL}", event.hostingChannel);
                message = message.replace("{HOSTVIEWERS}", String.valueOf(event.viewerCount));
                event.channel.message(message);
            }
        }
    }
}
