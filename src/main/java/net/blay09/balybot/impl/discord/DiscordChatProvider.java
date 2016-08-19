package net.blay09.balybot.impl.discord;

import lombok.extern.log4j.Log4j2;
import net.blay09.balybot.impl.api.Channel;
import net.blay09.balybot.impl.api.ChatProvider;
import net.dv8tion.jda.JDA;
import net.dv8tion.jda.entities.TextChannel;
import net.dv8tion.jda.entities.User;

import java.util.Optional;

@Log4j2
public class DiscordChatProvider implements ChatProvider {

	private final JDA jda;

	public DiscordChatProvider(JDA jda) {
		this.jda = jda;
	}

	@Override
	public void sendMessage(Channel channel, String message) {
		Optional<TextChannel> textChannel = jda.getTextChannelsByName(channel.getName()).stream().findFirst();
		if(textChannel.isPresent()) {
			textChannel.get().sendMessage(message);
		} else {
			log.warn("Could not find Discord channel " + channel.getName());
		}
	}

	@Override
	public void sendDirectMessage(String username, String message) {
		User user = jda.getUserById(username);
		if(user != null) {
			user.getPrivateChannel().sendMessage(message);
		} else {
			log.warn("Could not find Discord user " + username);
		}
	}

	@Override
	public int getUserCount(Channel channel) {
		TextChannel textChannel = jda.getTextChannelById(channel.getName());
		if(textChannel != null) {
			return textChannel.getUsers().size();
		} else {
			log.warn("Could not find Discord channel " + channel.getName());
			return 0;
		}
	}

}
