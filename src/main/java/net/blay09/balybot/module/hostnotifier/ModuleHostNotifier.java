package net.blay09.balybot.module.hostnotifier;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import net.blay09.balybot.irc.event.TwitchHostStartEvent;
import net.blay09.balybot.module.ConfigEntry;
import net.blay09.balybot.module.Module;

public class ModuleHostNotifier extends Module {

    public ConfigEntry MIN_VIEWERS = new ConfigEntry(this, "min_viewers", "The minimum amount of viewers from the host to trigger the message.", "2");
    public ConfigEntry MSG_HOSTED = new ConfigEntry(this, "msg.hosted", "The message displayed when this channel is hosted.", "Guys, guys!! {CHANNEL} is hosting us! Quick, hug them!");

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
        if(event.viewerCount >= MIN_VIEWERS.getInt(event.channel)) {
            String message = MSG_HOSTED.getString(event.channel);
            message = message.replace("{CHANNEL}", event.hostingChannel);
            message = message.replace("{VIEWERS}", String.valueOf(event.viewerCount));
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
