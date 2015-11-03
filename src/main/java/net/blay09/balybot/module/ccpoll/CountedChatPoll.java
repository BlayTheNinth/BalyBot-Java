package net.blay09.balybot.module.ccpoll;

import com.google.common.eventbus.Subscribe;
import net.blay09.balybot.irc.IRCChannel;
import net.blay09.balybot.irc.event.IRCChannelChatEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CountedChatPoll {

    private static class Poll {
        public final List<String> users = new ArrayList<>();
        public final String searchText;
        public final int[] votes;

        public Poll(String searchText, int maxCount) {
            this.searchText = searchText;
            this.votes = new int[maxCount];
        }
    }

    public static final CountedChatPoll instance = new CountedChatPoll();

    private Map<String, Poll> channelPolls = new HashMap<>();

    public CountedChatPoll() {}

    @Subscribe
    @SuppressWarnings("unused")
    public void onChannelChat(IRCChannelChatEvent event) {
        if(event.message.startsWith("!")) {
            return;
        }
        Poll poll = channelPolls.get(event.channel.getName());
        if(poll != null) {
            if(poll.users.contains(event.sender.getName())) {
                return;
            }
            if(event.message.startsWith("-") || event.message.startsWith("_")) {
                poll.votes[0]++;
                poll.users.add(event.sender.getName());
            }
            int countIdx = 0;
            int idx = -1;
            while((idx = event.message.indexOf(poll.searchText, idx + 1)) != -1) {
                countIdx++;
            }
            if(countIdx > 0) {
                poll.votes[countIdx]++;
                poll.users.add(event.sender.getName());
            }
        }
    }

    public void startPoll(IRCChannel channel, String searchText, int maxCount, String description) {
        if(description == null) {
            description = "Counted Chat Poll started";
        }
        channelPolls.put(channel.getName(), new Poll(searchText, maxCount));
        channel.message(description + " - Type up to " + maxCount + "x " + searchText + " in chat or --- to vote zero!");
    }

    public void stop(IRCChannel channel) {
        Poll poll = channelPolls.get(channel.getName());
        if(poll != null) {
            int maxCount = 0;
            for(int i = 0; i < poll.votes.length; i++) {
                maxCount += poll.votes[i];
            }
            int voteCount = 0;
            StringBuilder sb = new StringBuilder();
            for(int i = 0; i < poll.votes.length; i++) {
                voteCount += poll.votes[i] * i;
                if(sb.length() > 0) {
                    sb.append(", ");
                }
                sb.append(getNumberName(i)).append(": ").append(poll.votes[i]).append(" (").append(Math.round((float) poll.votes[i] / (float) maxCount * 100f)).append("%)");
            }
            channel.message("Average Result: " + String.format("%.1f", (float) voteCount / (float) maxCount) + " [" + sb.toString() + "]");
            channelPolls.remove(channel.getName());
        }
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
}
