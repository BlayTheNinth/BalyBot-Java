// Copyright (c) 2014, Christopher "blay09" Baker
// All rights reserved.
package net.blay09.balybot.irc.event;


import net.blay09.balybot.irc.IRCConnection;

/**
 * Base class for events based on an IRC connection.
 */
public abstract class IRCEvent extends Event {

	/**
	 * the connection this event is based on
	 */
	public final IRCConnection connection;

	/**
	 * INTERNAL EVENT. YOU SHOULD NOT POST THIS YOURSELF.
	 * @param connection the connection this event is based on
	 */
	public IRCEvent(IRCConnection connection) {
		this.connection = connection;
	}

}
