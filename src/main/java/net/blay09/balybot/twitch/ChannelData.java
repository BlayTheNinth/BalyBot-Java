package net.blay09.balybot.twitch;

import com.google.gson.JsonObject;

public class ChannelData {

    private long lastUpdated;
    private String game = "Could not retrieve game.";
    private String title = "Could not retrieve title.";

    public String getTitle() {
        return title;
    }

    public String getGame() {
        return game;
    }

    public long getLastUpdated() {
        return lastUpdated;
    }

    public boolean requiresUpdate() {
        return lastUpdated - System.currentTimeMillis() > TwitchAPI.CACHE_LIFETIME;
    }

    public void updateFromJson(JsonObject object) {
        game = object.get("game").getAsString();
        title = object.get("status").getAsString();
        lastUpdated = System.currentTimeMillis();
    }
}
