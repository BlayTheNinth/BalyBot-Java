package net.blay09.balybot.module.ccpoll;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import net.blay09.balybot.irc.IRCChannel;
import net.blay09.balybot.irc.event.IRCChannelChatEvent;
import net.blay09.balybot.module.Module;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ModuleCountedChatPoll extends Module {

    private static class Poll {
        public final List<String> users = new ArrayList<>();
        public final String searchText;
        public final int[] votes;
        public Poll(String searchText, int maxCount) {
            this.searchText = searchText;
            this.votes = new int[maxCount + 1];
        }
    }

    private Poll currentPoll;

    public ModuleCountedChatPoll(String context, String prefix) {
        super(context, prefix);
    }

    @Override
    public void activate(EventBus eventBus) {
        eventBus.register(this);
        registerCommand(new CountedChatPollBotCommand(this, prefix));
    }

    @Override
    public void deactivate(EventBus eventBus) {
        eventBus.unregister(this);
    }

    @Subscribe
    public void onChannelChat(IRCChannelChatEvent event) {
        if(event.message.startsWith("!")) {
            return;
        }
        if(currentPoll != null) {
            if(currentPoll.users.contains(event.sender.getName())) {
                return;
            }
            if(event.message.startsWith("-") || event.message.startsWith("_")) {
                currentPoll.votes[0]++;
                currentPoll.users.add(event.sender.getName());
            }
            int countIdx = 0;
            int idx = -1;
            while((idx = event.message.indexOf(currentPoll.searchText, idx + 1)) != -1) {
                countIdx++;
            }
            if(countIdx > 0) {
                currentPoll.votes[Math.min(countIdx, currentPoll.votes.length - 1)]++;
                currentPoll.users.add(event.sender.getName());
            }
        }
    }

    public String startPoll(IRCChannel channel, String searchText, int maxCount, String description) {
        if(description == null) {
            description = "Counted Chat Poll started";
        }
        currentPoll = new Poll(searchText, maxCount);
        return description + " - Type up to " + maxCount + "x " + searchText + " (" + searchText + ") in chat or --- to vote zero!";
    }

    public String stop(IRCChannel channel) {
        if(currentPoll != null) {
            int maxCount = 0;
            for(int i = 0; i < currentPoll.votes.length; i++) {
                maxCount += currentPoll.votes[i];
            }
            int voteCount = 0;
            StringBuilder sb = new StringBuilder();
            for(int i = 0; i < currentPoll.votes.length; i++) {
                voteCount += currentPoll.votes[i] * i;
                if(sb.length() > 0) {
                    sb.append(", ");
                }
                sb.append(getNumberName(i)).append(": ").append(currentPoll.votes[i]).append(" (").append(Math.round((float) currentPoll.votes[i] / (float) maxCount * 100f)).append("%)");
            }
            currentPoll = null;
            return "Average Result: " + String.format("%.1f", (float) voteCount / (float) maxCount) + " [" + sb.toString() + "]";
        }
        return "There was no poll running, silly!";
    }

    private static String getNumberName(int i) {
        switch(i) {
            case 0:
                return "None";
            case 1:
                return "Single";
            case 2:
                return "Double";
            case 3:
                return "Triple";
            case 4:
                return "Quadruple";
            case 5:
                return "Quintuple";
            case 6:
                return "Sextuple";
            case 7:
                return "Septuple";
            case 8:
                return "Octuple";
            case 9:
                return "Nonuple";
            case 10:
                return "Decuple";
            case 11:
                return "Undecuple";
            case 12:
                return "Duodecuple";
        }
        return null;
    }

    @Override
    public String getModuleCode() {
        return "ccp";
    }

    @Override
    public String getModuleName() {
        return "Counted Chat Polls";
    }

    @Override
    public String getModuleDescription() {
        return "Provides the !ccp command to create polls that are voted for within chat by posting a certain amount of emoticons. Returns percentages and an average.";
    }
}
