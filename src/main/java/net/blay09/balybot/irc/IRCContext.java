// Copyright (c) 2014, Christopher "blay09" Baker
// All rights reserved.
package net.blay09.balybot.irc;

public interface IRCContext {

	/**
	 * Defines the type of an IRC context.
	 */
	enum ContextType {
		Error,
		IRCConnection,
		IRCChannel,
		IRCUser
	}

	/**
	 * Examples for return values are #EiraIRC, irc.esper.net or BlayTheNinth
	 * @return the name of this context or in case of an error, the corresponding language key
	 */
	String getName();

	/**
	 * @return the type of this IRC context
	 */
	ContextType getContextType();

	/**
	 * Examples for return values are irc.esper.net/#EiraIRC or irc.epser.net/BlayTheNinth
	 * @return the unique identifier for this context
	 */
	String getIdentifier();

	/**
	 * @return the connection this context is based on or null if of type Error
	 */
	IRCConnection getConnection();

	/**
	 * Sends a commandMessage to this IRC context.
	 * Raw IRC Equivalent: PRIVMSG getName() :commandMessage
	 * Does nothing for types IRCConnection and Error.
	 * @param message the commandMessage to be sent to this context
	 */
	void message(String message);

	/**
	 * Sends a notice to this IRC context.
	 * Raw IRC Equivalent: NOTICE getName() :commandMessage
	 * Does nothing for types IRCConnection and Error.
	 * @param message the commandMessage to be sent to this context
	 */
	void notice(String message);

}
