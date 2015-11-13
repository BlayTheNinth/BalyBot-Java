package net.blay09.balybot.module.linkfilter;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import net.blay09.balybot.Config;
import net.blay09.balybot.UserLevel;
import net.blay09.balybot.CommandHandler;
import net.blay09.balybot.command.BotCommand;
import net.blay09.balybot.irc.event.IRCChannelChatEvent;
import net.blay09.balybot.module.Module;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ModuleLinkFilter extends Module {

    private static final Pattern ipPattern = Pattern.compile("[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}");
    private static final Matcher ipMatcher = ipPattern.matcher("");
    private static final Pattern linkPattern = Pattern.compile("(https?://)?([A-Za-z0-9\\.-]+)\\.([A-Za-z\\.]{2,6})([/\\w \\.-]*)*/?");
    private static final Matcher linkMatcher = linkPattern.matcher("");

    private final List<String> permissions = new ArrayList<>();

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
        UserLevel userLevel = UserLevel.fromName(Config.getValue(event.channel.getName(), "linkfilter_userlevel", "reg"));
        if(!CommandHandler.passesUserLevel(event.sender, event.channel, userLevel)) {
            if (Config.getValue(event.channel.getName(), "linkfilter", "false").equals("true")) {
                linkMatcher.reset(event.message);
                ipMatcher.reset(event.message);
                if (linkMatcher.find() || ipMatcher.find()) {
                    if (permissions.contains(event.sender.getName())) {
                        permissions.remove(event.sender.getName());
                    } else {
                        event.channel.message("/timeout " + event.sender.getName() + " 1");
                        if (Config.getValue(event.channel.getName(), "linkfilter_message_show", "true").equals("true")) {
                            event.channel.message(Config.getValue(event.channel.getName(), "linkfilter_message", "Nooo! " + event.sender.getName() + ", stop posting links and IPs without permission, please!"));
                        }
                    }
                }
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
        return "Prevents people below a certain user level (linkfilter_userlevel) from posting links, unless they have been permitted using the !permit command.";
    }
}
