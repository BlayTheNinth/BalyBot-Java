package net.blay09.balybot.module.ccpoll;

import com.google.common.collect.Lists;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import net.blay09.balybot.irc.IRCChannel;
import net.blay09.balybot.irc.event.IRCChannelChatEvent;
import net.blay09.balybot.module.ConfigEntry;
import net.blay09.balybot.module.Module;

import java.util.List;

public class ModuleCountedChatPoll extends Module {

    public ConfigEntry MSG_POLL_STARTED = new ConfigEntry(this, "msg.poll_started", "The message that appears in front of the instructions, when no description is supplied.", "Counted Chat Poll started");
    public ConfigEntry MSG_INSTRUCTIONS = new ConfigEntry(this, "msg.instructions", "The instructions that appear after the poll description. Variables: MAX, TEXT", "Type up to {MAX}x {TEXT} ({TEXT}) in chat or --- to vote zero!");
    public ConfigEntry MSG_RESULTS = new ConfigEntry(this, "msg.results", "The result output when the poll is ended. Variables: AVERAGE, PERCENTAGE", "Average Result: {AVERAGE} [{PERCENTAGE}]");
    public ConfigEntry MSG_NO_POLL = new ConfigEntry(this, "msg.no_poll", "The message displayed if no poll was running.", "There was no poll running, silly!");
    public ConfigEntry UL_CCP = new ConfigEntry(this, "ul.ccp", "The minimum user level for the !ccp command.", "mod");

    private static class Poll {
        public final List<String> users = Lists.newArrayList();
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
        registerCommand(new CountedChatPollBotCommand(this, prefix, UL_CCP.getUserLevel(context)));
    }

    @Override
    public void deactivate(EventBus eventBus) {
        eventBus.unregister(this);
    }

    @Subscribe
    public void onChannelChat(IRCChannelChatEvent event) {
        if(event.message.startsWith(prefix)) {
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
            description = MSG_POLL_STARTED.getString(channel);
        }
        currentPoll = new Poll(searchText, maxCount);
        String instructions = MSG_INSTRUCTIONS.getString(channel);
        instructions = instructions.replace("{MAX}", String.valueOf(maxCount));
        instructions = instructions.replace("{TEXT}", searchText);
        return description + " - " + instructions;
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
            String result = MSG_RESULTS.getString(channel);
            result = result.replace("{AVERAGE}", String.format("%.1f", (float) voteCount / (float) maxCount));
            result = result.replace("{PERCENTAGE}", sb.toString());
            return result;
        }
        return MSG_NO_POLL.getString(channel);
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
