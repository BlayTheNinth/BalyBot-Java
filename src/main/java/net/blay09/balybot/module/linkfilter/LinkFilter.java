package net.blay09.balybot.module.linkfilter;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import net.blay09.balybot.Config;
import net.blay09.balybot.Database;
import net.blay09.balybot.UserLevel;
import net.blay09.balybot.CommandHandler;
import net.blay09.balybot.irc.event.IRCChannelChatEvent;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

// TODO move into separate module
public class LinkFilter {

    private static final LinkFilter instance = new LinkFilter();
    private static final Pattern ipPattern = Pattern.compile("[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}");
    private static final Matcher ipMatcher = ipPattern.matcher("");
    private static final Pattern linkPattern = Pattern.compile("(https?://)?([A-Za-z0-9\\.-]+)\\.([A-Za-z\\.]{2,6})([/\\w \\.-]*)*/?");
    private static final Matcher linkMatcher = linkPattern.matcher("");

    private static final Multimap<String, String> permissions = ArrayListMultimap.create();

    public static void load(Database database, EventBus eventBus) {
        eventBus.register(instance);
    }

    @Subscribe
    public void onChannelChat(IRCChannelChatEvent event) {
        UserLevel userLevel = UserLevel.fromName(Config.getValue(event.channel.getName(), "linkfilter_userlevel", "reg"));
        if(!CommandHandler.passesUserLevel(event.sender, event.channel, userLevel)) {
            if (Config.getValue(event.channel.getName(), "linkfilter", "false").equals("true")) {
                linkMatcher.reset(event.message);
                ipMatcher.reset(event.message);
                if (linkMatcher.find() || ipMatcher.find()) {
                    if (permissions.containsEntry(event.channel.getName(), event.sender.getName())) {
                        permissions.remove(event.channel.getName(), event.sender.getName());
                    } else {
                        event.channel.message("/timeout " + event.sender.getName() + " 1");
                        if (Config.getValue(event.channel.getName(), "linkfilter_message_show", "true").equals("true")) {
                            event.channel.message(Config.getValue(event.channel.getName(), "linkfilter_message", "Nooo! " + event.sender.getName() + ", stop posting links and IPs without permission, please!!"));
                        }
                    }
                }
            }
        }
    }

    public static void permit(String channelName, String username) {
        permissions.put(channelName, username);
    }
}
