package net.blay09.balybot.impl.api;

import lombok.Value;

@Value
public class User {
	String nick;
	String displayName;
	Object backend;
}
