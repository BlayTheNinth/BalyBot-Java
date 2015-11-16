package net.blay09.balybot.module.raffle;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import net.blay09.balybot.CommandHandler;
import net.blay09.balybot.Config;
import net.blay09.balybot.irc.IRCChannel;
import net.blay09.balybot.irc.event.IRCChannelChatEvent;
import net.blay09.balybot.module.Module;

import java.util.Random;
import java.util.Set;

public class ModuleRaffle extends Module {

    private static final int MAX_ALLOWED_ENTRIES = 3;
    private static final String MAX_ALLOWED_ENTRIES_MESSAGE = "{SENDER} decided to spam the message and has gotten themselves disqualified. Good job!";
    private static final int MAX_ALLOWED_ENTRIES_TIMEOUT = 600;

    private static final Random random = new Random();

    private static class Raffle {
        public final Multiset<String> users = HashMultiset.create();
        public final String searchText;

        public Raffle(String searchText) {
            this.searchText = searchText;
        }
    }

    private Raffle currentRaffle;

    public ModuleRaffle(String context, String prefix) {
        super(context, prefix);
    }

    @Override
    public void activate(EventBus eventBus) {
        eventBus.register(this);
        registerCommand(new RaffleBotCommand(this, prefix));
    }

    @Override
    public void deactivate(EventBus eventBus) {
        eventBus.unregister(this);
    }

    @Subscribe
    public void onChannelChat(IRCChannelChatEvent event) {
        if(currentRaffle != null) {
            if(event.message.equals(currentRaffle.searchText)) {
                currentRaffle.users.add(event.sender.getDisplayName());
                int maxCount = Config.getValueAsInt(event.channel.getName(), "raffle_maxAllowedEntries", MAX_ALLOWED_ENTRIES);
                if(currentRaffle.users.count(event.sender.getDisplayName()) > maxCount) {
                    String message = Config.getValue(event.channel.getName(), "raffle_maxAllowedEntriesMessage", MAX_ALLOWED_ENTRIES_MESSAGE);
                    message = CommandHandler.resolveVariables(message, null, event.channel, event.sender, event.message, new String[0], 0);
                    event.channel.message(message);
                    event.channel.message("/timeout " + event.sender.getName() + " " + Config.getValueAsInt(event.channel.getName(), "raffle_maxAllowedEntriesTimeout", MAX_ALLOWED_ENTRIES_TIMEOUT));
                }
            }
        }
    }

    public String startRaffle(IRCChannel channel, String searchText, String description) {
        if(description == null) {
            description = "Raffle started";
        }
        currentRaffle = new Raffle(searchText);
        return description + " - type '" + searchText + "' to enter for a chance to win. Don't spam it!";
    }

    public String draw(IRCChannel channel) {
        if(currentRaffle != null) {
            Set<String> users = currentRaffle.users.elementSet();
            String[] userList = users.toArray(new String[users.size()]);
            String winner = userList[random.nextInt(userList.length)];
            return "We have a winner! Congratulations, " + winner + "! You got lucky.";
        }
        return "There was no raffle running, silly!";
    }

    public String stop(IRCChannel channel) {
        currentRaffle = null;
        return "Raffle entries have been cleared.";
    }

    @Override
    public String getModuleCode() {
        return "raffle";
    }

    @Override
    public String getModuleName() {
        return "Raffle Module";
    }

    @Override
    public String getModuleDescription() {
        return "Provides the !raffle command to create raffles that people can enter by typing a keyword in chat.";
    }
}
