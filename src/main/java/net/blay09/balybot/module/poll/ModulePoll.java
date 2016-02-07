package net.blay09.balybot.module.poll;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import net.blay09.balybot.irc.event.IRCChannelChatEvent;
import net.blay09.balybot.module.ConfigEntry;
import net.blay09.balybot.module.Module;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public class ModulePoll extends Module {

    public ConfigEntry MSG_START_PREFIX = new ConfigEntry(this, "msg.start_prefix", "The prefix used for the poll started message.", "[New Poll]");
    public ConfigEntry MSG_RESULTS_PREFIX = new ConfigEntry(this, "msg.results_prefix", "The prefix used for the poll ended message.", "[Results]");
    public ConfigEntry MSG_NO_POLL = new ConfigEntry(this, "msg.no_poll", "The message displayed if no poll was running.", "There was no poll running, stupid.");
    public ConfigEntry UL_POLL = new ConfigEntry(this, "ul.poll", "The minimum user level for the !poll command.", "mod");

    private static class Poll {
        public final List<String> users = new ArrayList<>();
        public final String[] triggers;
        public final String[] options;
        public final int[] votes;

        public Poll(String[] triggers, String[] options) {
            this.triggers = triggers;
            this.options = options;
            this.votes = new int[triggers.length];
        }

        public PollResult[] getResults() {
            PollResult[] results = new PollResult[votes.length];
            for(int i = 0; i < results.length; i++) {
                results[i] = new PollResult(options[i], votes[i]);
            }
            Arrays.sort(results, new PollResultComparator());
            return results;
        }

        public int getTotalVotes() {
            int totalVotes = 0;
            for (int vote : votes) {
                totalVotes += vote;
            }
            return totalVotes;
        }
    }

    public static class PollResultComparator implements Comparator<PollResult> {
        @Override
        public int compare(PollResult o1, PollResult o2) {
            return o2.votes - o1.votes;
        }
    }

    private static class PollResult {
        public final String optionName;
        public final int votes;

        public PollResult(String optionName, int votes) {
            this.optionName = optionName;
            this.votes = votes;
        }
    }

    private Poll currentPoll;

    public ModulePoll(String context, String prefix) {
        super(context, prefix);
    }

    @Override
    public void activate(EventBus eventBus) {
        eventBus.register(this);
        registerCommand(new PollBotCommand(this, prefix, UL_POLL.getUserLevel(context)));
    }

    @Override
    public void deactivate(EventBus eventBus) {
        eventBus.unregister(this);
    }

    @Subscribe
    @SuppressWarnings("unused")
    public void onChannelChat(IRCChannelChatEvent event) {
        if(event.message.startsWith(prefix)) {
            return;
        }
        if(currentPoll != null) {
            if(currentPoll.users.contains(event.sender.getName())) {
                return;
            }
            for(int i = 0; i < currentPoll.triggers.length; i++) {
                if(event.message.contains(currentPoll.triggers[i])) {
                    currentPoll.votes[i]++;
                    currentPoll.users.add(event.sender.getName());
                    break;
                }
            }
        }
    }

    public String startPoll(String[] searchTexts, String[] options) {
        currentPoll = new Poll(searchTexts, options);
        StringBuilder message = new StringBuilder(MSG_START_PREFIX.getString(context) + " ");
        for(int i = 0; i < searchTexts.length; i++) {
            if(i > 0) {
                message.append(" // ");
            }
            message.append(searchTexts[i]).append(" (").append(searchTexts[i]).append("): ").append(options[i]);
        }
        return message.toString();
    }

    public String stop() {
        if (currentPoll != null) {
            int totalVotes = currentPoll.getTotalVotes();
            StringBuilder message = new StringBuilder(MSG_RESULTS_PREFIX.getString(context) + " ");
            boolean first = true;
            for (PollResult result : currentPoll.getResults()) {
                if (!first) {
                    message.append(" // ");
                }
                first = false;
                message.append(result.optionName).append(": ").append(result.votes).append(" (").append(Math.round(((float) result.votes / (float) totalVotes) * 100f)).append("%)");
            }
            currentPoll = null;
            return message.toString();
        }
        return MSG_NO_POLL.getString(context);
    }

    @Override
    public String getModuleCode() {
        return "poll";
    }

    @Override
    public String getModuleName() {
        return "Trigger Polls";
    }

    @Override
    public String getModuleDescription() {
        return "Provides the !poll command to create polls that are voted for within chat by posting a trigger word bound to an option.";
    }
}
