package net.blay09.balybot.command;

import com.google.common.collect.Maps;
import lombok.Getter;
import net.blay09.balybot.Config;
import net.blay09.javatmi.TwitchUser;

import java.util.Map;

public abstract class UserLevel {

    public static final UserLevel OWNER = new UserLevel("owner", 500) {
        @Override
        public boolean passes(String channelName, TwitchUser user) {
            return Config.getGlobalString("username", null).toLowerCase().equals(user.getDisplayName().toLowerCase());
        }
    };

    public static final UserLevel BROADCASTER = new UserLevel("broadcaster", 400) {
        @Override
        public boolean passes(String channelName, TwitchUser user) {
            return user.getDisplayName().toLowerCase().equals(channelName);
        }
    };

    public static final UserLevel MOD = new UserLevel("mod", 300) {
        @Override
        public boolean passes(String channelName, TwitchUser user) {
            return user.isMod();
        }
    };

    public static final UserLevel SUB = new UserLevel("sub", 200) {
        @Override
        public boolean passes(String channelName, TwitchUser user) {
            return user.isSubscriber();
        }
    };

    public static final UserLevel TURBO = new UserLevel("sub", 100) {
        @Override
        public boolean passes(String channelName, TwitchUser user) {
            return user.isTurbo();
        }
    };

    public static final UserLevel ALL = new UserLevel("all", 0) {
        @Override
        public boolean passes(String channelName, TwitchUser user) {
            return true;
        }
    };

    private static final Map<String, UserLevel> userLevelMap = Maps.newHashMap();

    static {
        register(OWNER);
        register(BROADCASTER);
        register(MOD);
        register(SUB);
        register(TURBO);
        register(ALL);
    }

    public static void register(UserLevel userLevel) {
        userLevelMap.put(userLevel.name.toLowerCase(), userLevel);
    }

    public static UserLevel fromName(String name) {
        return userLevelMap.get(name.toLowerCase());
    }

    @Getter
    private final String name;
    @Getter
    private final int level;

    public UserLevel(String name, int level) {
        this.name = name;
        this.level = level;
    }

    public static String[] getValidLevels() {
        return userLevelMap.keySet().toArray(new String[userLevelMap.size()]);
    }

    public abstract boolean passes(String channelName, TwitchUser user);

    public static UserLevel getUserLevel(String channelName, TwitchUser user) {
        UserLevel highestLevel = ALL;
        for (UserLevel userLevel : userLevelMap.values()) {
            if (highestLevel.level < userLevel.level && userLevel.passes(channelName, user)) {
                highestLevel = userLevel;
            }
        }
        return highestLevel;
    }

    public static UserLevel getMinimumUserLevel(int commandLevel) {
		UserLevel lowestLevel = OWNER;
		for(UserLevel userLevel : userLevelMap.values()) {
			if(userLevel.getLevel() >= commandLevel && userLevel.getLevel() < lowestLevel.getLevel()) {
				lowestLevel = userLevel;
			}
		}
        return lowestLevel;
    }
}
