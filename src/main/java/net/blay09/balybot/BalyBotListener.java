package net.blay09.balybot;

import net.blay09.balybot.command.BotCommand;
import net.blay09.balybot.script.EventType;
import net.blay09.balybot.script.ScriptManager;
import net.blay09.javatmi.TMIAdapter;
import net.blay09.javatmi.TMIClient;
import net.blay09.javatmi.TwitchUser;

import java.util.regex.Matcher;

public class BalyBotListener extends TMIAdapter {

    @Override
    public void onConnected(TMIClient client) {
		ChannelManager.getChannels().forEach(client::join);
    }

	@Override
	public void onUnhandledException(TMIClient tmiClient, Exception e) {
		e.printStackTrace();
	}

	@Override
    public void onChatMessage(TMIClient client, String channel, TwitchUser user, String message) {
        System.out.println(user.getDisplayName() + ": " + message);
        ScriptManager.getInstance().publishEvent(EventType.CHANNEL_CHAT, channel, user, message);
        BotCommand command = CommandHandler.findCommand(channel, user, message);
        if (command != null) {
            Matcher matcher = command.getPattern().matcher(message);
            if (matcher.find()) {
                String[] args;
                if (matcher.groupCount() > 0 && matcher.group(1) != null && matcher.group(1).trim().length() > 0) {
                    args = matcher.group(1).split(" ");
                } else {
                    args = new String[0];
                }
                String result = command.execute(channel, user, message, args, 0, false);
                if (result != null) {
                    if (result.startsWith("/") || result.startsWith(".")) {
                        if (!result.startsWith("/me") && !result.startsWith(".me")) {
                            result = "-" + result;
                        }
                    }
                    if (command.getWhisperTo() != null) {
						String whisperTarget = CommandHandler.resolveVariables(command.getWhisperTo(), command, channel, user, message, args, 0);
						if(whisperTarget.contains("{") || whisperTarget.contains("}")) {
							whisperTarget = user.getDisplayName();
						}
						System.out.println("BalyBot -> " + whisperTarget + ": " + result);
						BalyBot.getInstance().getClient().getTwitchCommands().whisper(whisperTarget, result);
                    } else {
						System.out.println("BalyBot: " + result);
						BalyBot.getInstance().getClient().send(channel, result);
                    }
                }
            }
        }
    }


    @Override
    public void onHosted(TMIClient client, String channel, String username, int viewers) {
        ScriptManager.getInstance().publishEvent(EventType.CHANNEL_HOSTED, channel, username, viewers);
    }
}
