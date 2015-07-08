package net.blay09.balybot;

public enum UserLevel {
    ALL("all"),
    TURBO("turbo"),
    REGULAR("reg"),
    SUBSCRIBER("sub"),
    MODERATOR("mod"),
    BROADCASTER("broadcaster"),
    OWNER("owner");

    private static final UserLevel[] values = values();

    public final String name;

    UserLevel(String name) {
        this.name = name;
    }

    public static UserLevel fromName(String name) {
        for(UserLevel userLevel : values) {
            if(userLevel.name.equals(name)) {
                return userLevel;
            }
        }
        return null;
    }

    public static UserLevel fromId(int id) {
        return values[id];
    }
}
