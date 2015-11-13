package net.blay09.balybot.module.hostnotifier;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import net.blay09.balybot.Config;
import net.blay09.balybot.Database;
import net.blay09.balybot.irc.event.TwitchHostStartEvent;
import net.blay09.balybot.module.Module;
import net.blay09.balybot.module.linkfilter.ModuleLinkFilter;

public class ModuleHostNotifier extends Module {

    public ModuleHostNotifier(String context, char prefix) {
        super(context, prefix);
    }

    @Override
    public void activate(EventBus eventBus) {
        eventBus.register(this);
    }

    @Override
    public void deactivate(EventBus eventBus) {
        eventBus.unregister(this);
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
