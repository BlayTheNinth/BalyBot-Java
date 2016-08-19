package net.blay09.balybot.impl.base;

import net.blay09.balybot.impl.api.Channel;
import net.blay09.balybot.impl.api.User;
import net.blay09.balybot.impl.api.UserLevel;

public class DefaultUserLevels {

    public static final UserLevel SUPER_USER = new UserLevel("super_user", 500) {
        @Override
        public boolean passes(Channel channel, User user) {
            return channel.getImplementation().isSuperUser(channel, user);
        }
    };

    public static final UserLevel CHANNEL_OWNER = new UserLevel("channel_owner", 400) {
        @Override
        public boolean passes(Channel channel, User user) {
            return channel.getImplementation().isChannelOwner(channel, user);
        }
    };

    public static final UserLevel ALL = new UserLevel("all", 0) {
        @Override
        public boolean passes(Channel channel, User user) {
            return true;
        }
    };

}
