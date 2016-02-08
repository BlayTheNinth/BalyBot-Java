package net.blay09.balybot.module.linkfilter;

import com.google.common.collect.Lists;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import net.blay09.balybot.CommandHandler;
import net.blay09.balybot.irc.event.IRCChannelChatEvent;
import net.blay09.balybot.module.ConfigEntry;
import net.blay09.balybot.module.Module;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ModuleLinkFilter extends Module {

    public ConfigEntry SHOW_MESSAGE = new ConfigEntry(this, "show_message", "Should a message be shown when a link purge happens?", "true");
    public ConfigEntry MSG_LINK_PURGED = new ConfigEntry(this, "msg.link_purged", "The message displayed when a user is purged for posting a link. Variables: NICK", "Nooo! {NICK}, stop posting links or IPs without permission, please!");
    public ConfigEntry MSG_PERMITTED = new ConfigEntry(this, "msg.permitted", "The message displayed when a user is permitted to post a link. Variables: NICK", "{NICK}, you may now post one link. Make it count!");
    public ConfigEntry UL_LINKS = new ConfigEntry(this, "ul.links", "The minimum user level required to post links.", "reg");

    private static final Pattern ipPattern = Pattern.compile("[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}");
    private static final Matcher ipMatcher = ipPattern.matcher("");
    private static final Pattern linkPattern = Pattern.compile("((?:ftp|https?)://)?([A-Za-z0-9\\.-]+)\\.([A-Za-z]{2,6})([/\\w \\.-]*)*/?");
    private static final Matcher linkMatcher = linkPattern.matcher("");

    private final List<String> permissions = Lists.newArrayList();

    public ModuleLinkFilter(String context, String prefix) {
        super(context, prefix);
    }

    @Override
    public void activate(EventBus eventBus) {
        registerCommand(new PermitBotCommand(this, prefix));
        eventBus.register(this);
    }

    @Override
    public void deactivate(EventBus eventBus) {
        eventBus.unregister(this);
    }

    @Subscribe
    public void onChannelChat(IRCChannelChatEvent event) {
        linkMatcher.reset(event.message);
        ipMatcher.reset(event.message);
        if (linkMatcher.find() || ipMatcher.find()) {
            if(CommandHandler.passesUserLevel(event.sender, event.channel, UL_LINKS.getUserLevel(context))) {
                return;
            }
            if (permissions.contains(event.sender.getName())) {
                permissions.remove(event.sender.getName());
                return;
            }
            event.channel.message("/timeout " + event.sender.getName() + " 1");
            if (SHOW_MESSAGE.getBoolean(event.channel)) {
                String message = MSG_LINK_PURGED.getString(event.channel);
                message = message.replace("{NICK}", event.sender.getDisplayName());
                event.channel.message(message);
            }
        }
    }

    public void permit(String username) {
        permissions.add(username);
    }

    @Override
    public String getModuleCode() {
        return "linkfilter";
    }

    @Override
    public String getModuleName() {
        return "Link Filter";
    }

    @Override
    public String getModuleDescription() {
        return "Prevents people below a certain user level from posting links, unless they have been permitted using the !permit command.";
    }
}
