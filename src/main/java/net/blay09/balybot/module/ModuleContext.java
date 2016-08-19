package net.blay09.balybot.module;

import lombok.Data;
import net.blay09.balybot.impl.api.Channel;
import net.blay09.balybot.impl.api.Server;

@Data
public class ModuleContext {
	private Server server;
	private Channel channel;

	public ModuleContext(Server server) {
		this.server = server;
	}

	public ModuleContext(Channel channel) {
		this.server = channel.getServer();
		this.channel = channel;
	}
}
