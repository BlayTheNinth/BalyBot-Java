package net.blay09.balybot.script;

public enum EventType {
    CHANNEL_CHAT,
    CHANNEL_HOSTED;

    public static EventType fromName(String key) {
        try {
            return valueOf(key.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Unknown event type '" + key + "'");
        }
    }
}
