package net.blay09.balybot.impl;

import com.google.common.collect.Maps;
import net.blay09.balybot.impl.api.Channel;
import net.blay09.balybot.impl.api.User;
import net.blay09.balybot.impl.api.UserLevel;
import net.blay09.balybot.impl.base.DefaultUserLevels;

import java.util.Map;

public class UserLevelRegistry {

	private static final Map<String, UserLevel> userLevelMap = Maps.newHashMap();

	public void register(UserLevel userLevel) {
		userLevelMap.put(userLevel.getName().toLowerCase(), userLevel);
	}

	public UserLevel fromName(String name) {
		return userLevelMap.get(name.toLowerCase());
	}

	public String[] getValidLevels() {
		return userLevelMap.keySet().toArray(new String[userLevelMap.size()]);
	}

	public UserLevel getUserLevel(Channel channel, User user) {
		UserLevel highestLevel = DefaultUserLevels.ALL;
		for (UserLevel userLevel : userLevelMap.values()) {
			if (highestLevel.getLevel() < userLevel.getLevel() && userLevel.passes(channel, user)) {
				highestLevel = userLevel;
			}
		}
		return highestLevel;
	}

	public UserLevel getMinimumUserLevel(int commandLevel) {
		UserLevel lowestLevel = DefaultUserLevels.SUPER_USER;
		for(UserLevel userLevel : userLevelMap.values()) {
			if(userLevel.getLevel() >= commandLevel && userLevel.getLevel() < lowestLevel.getLevel()) {
				lowestLevel = userLevel;
			}
		}
		return lowestLevel;
	}

}
