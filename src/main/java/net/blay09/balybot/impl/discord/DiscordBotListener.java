package net.blay09.balybot.impl.discord;

import lombok.extern.log4j.Log4j2;
import net.blay09.balybot.BalyBot;
import net.blay09.balybot.command.BotCommand;
import net.blay09.balybot.command.CommandHandler;
import net.blay09.balybot.impl.api.Channel;
import net.blay09.balybot.impl.api.User;
import net.blay09.balybot.impl.base.script.DefaultEvents;
import net.blay09.balybot.script.ScriptManager;
import net.dv8tion.jda.Permission;
import net.dv8tion.jda.entities.Guild;
import net.dv8tion.jda.entities.TextChannel;
import net.dv8tion.jda.events.ReadyEvent;
import net.dv8tion.jda.events.guild.GuildJoinEvent;
import net.dv8tion.jda.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.hooks.ListenerAdapter;

import java.util.regex.Matcher;

@Log4j2
public class DiscordBotListener extends ListenerAdapter {

	@Override
	public void onReady(ReadyEvent event) {
		DiscordImplementation impl = (DiscordImplementation) BalyBot.getInstance().getImplementation("discord");
		for(Guild guild : event.getJDA().getGuilds()) {
			impl.joinServer(guild.getId());
		}
		event.getJDA().getTextChannels().stream()
				.filter(channel -> channel.checkPermission(event.getJDA().getSelfInfo(), Permission.MESSAGE_READ, Permission.MESSAGE_WRITE))
				.forEach(channel -> impl.joinChannel(channel.getGuild().getId() + "/" + channel.getId()));
	}

	@Override
	public void onGuildJoin(GuildJoinEvent event) {
		DiscordImplementation impl = (DiscordImplementation) BalyBot.getInstance().getImplementation("discord");
		impl.joinServer(event.getGuild().getId());
	}

	@Override
	public void onGuildMessageReceived(GuildMessageReceivedEvent event) {
		Channel channel = DiscordImplementation.getChannel(event.getGuild(), event.getChannel());
		if(channel == null) {
			log.debug("Skipping message in {} because it's not configured: {}", event.getChannel().getName(), event.getMessage().getContent());
			return;
		}
		log.info(event.getAuthorName() + ": " + event.getMessage().getContent());
		User user = DiscordImplementation.createUserFrom(event);
		String message = event.getMessage().getContent();
		ScriptManager.getInstance().publishEvent(DefaultEvents.CHANNEL_CHAT, channel, user, message);
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
						log.info("BalyBot -> {}: {}", whisperTarget, result);
						channel.getChatProvider().sendDirectMessage(whisperTarget, result);
					} else {
						log.info("BalyBot: {}", result);
						channel.getChatProvider().sendMessage(channel, result);
					}
				}
			}
		}
	}

}
