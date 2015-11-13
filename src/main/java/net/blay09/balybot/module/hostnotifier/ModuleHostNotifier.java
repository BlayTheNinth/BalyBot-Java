package net.blay09.balybot.module.hostnotifier;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import net.blay09.balybot.Config;
import net.blay09.balybot.Database;
import net.blay09.balybot.irc.event.TwitchHostStartEvent;
import net.blay09.balybot.module.Module;
import net.blay09.balybot.module.linkfilter.ModuleLinkFilter;

public class ModuleHostNotifier extends Module {

    public ModuleHostNotifier(String context, String prefix) {
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
        if(event.viewerCount >= Integer.parseInt(Config.getValue(event.channel.getName(), "hostnotifier_minviewers", "2"))) {
            String message = Config.getValue(event.channel.getName(), "hostnotifier_message", "Guys, guys!! {HOSTCHANNEL} is hosting us! Quick, hug them!");
            message = message.replace("{HOSTCHANNEL}", event.hostingChannel);
            message = message.replace("{HOSTVIEWERS}", String.valueOf(event.viewerCount));
            event.channel.message(message);
        }
    }

    @Override
    public String getModuleCode() {
        return "hostnotifier";
    }

    @Override
    public String getModuleName() {
        return "Host Notifier";
    }

    @Override
    public String getModuleDescription() {
        return "Sends a configured message (hostnotifier_message) when someone hosts the channel for a minimum of (hostnotifier_minviewers) viewers. The message can contain the variables {HOSTCHANNEL} and {HOSTVIEWERS}.";
    }
}
