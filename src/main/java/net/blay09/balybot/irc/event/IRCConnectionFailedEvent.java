// Copyright (c) 2014, Christopher "blay09" Baker
// All rights reserved.
package net.blay09.balybot.irc.event;

import net.blay09.balybot.irc.IRCConnection;

/**
 * This event is published on the MinecraftForge.EVENTBUS bus whenever EiraIRC fails to connect to an IRC server.
 */
public class IRCConnectionFailedEvent extends IRCEvent {

	/**
	 * the exception that caused this connection to fail.
	 */
	public final Exception exception;

	/**
	 * INTERNAL EVENT. YOU SHOULD NOT POST THIS YOURSELF.
	 * @param connection the connection that was created
	 */
	public IRCConnectionFailedEvent(IRCConnection connection, Exception exception) {
		super(connection);
		this.exception = exception;
	}

}
