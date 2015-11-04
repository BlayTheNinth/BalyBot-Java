package net.blay09.balybot.twitch;

import com.google.gson.JsonObject;

public class StreamData {

    private long lastUpdated;
    private boolean isLive;
    private int viewers;

    public int getViewers() {
        return viewers;
    }

    public long getLastUpdated() {
        return lastUpdated;
    }

    public boolean requiresUpdate() {
        return lastUpdated - System.currentTimeMillis() > TwitchAPI.CACHE_LIFETIME;
    }

    public void updateFromJson(JsonObject object) {
        isLive = !object.get("stream").isJsonNull();
        if(isLive) {
            JsonObject stream = object.getAsJsonObject("stream");
            viewers = stream.get("viewers").getAsInt();
        } else {
            viewers = 0;
        }
        lastUpdated = System.currentTimeMillis();
    }

    public boolean isLive() {
        return isLive;
    }
}
