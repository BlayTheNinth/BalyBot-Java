// Copyright (c) 2014, Christopher "blay09" Baker
// All rights reserved.
package net.blay09.balybot.irc.event;

import net.blay09.balybot.irc.IRCConnection;

public class IRCExceptionEvent extends IRCEvent {

	public final Exception exception;

	public IRCExceptionEvent(IRCConnection connection, Exception exception) {
		super(connection);
		this.exception = exception;
	}

}
