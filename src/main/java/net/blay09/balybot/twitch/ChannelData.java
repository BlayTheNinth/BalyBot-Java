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
        return System.currentTimeMillis() - lastUpdated > TwitchAPI.CACHE_LIFETIME;
    }

    public void updateFromJson(JsonObject object) {
        if(object.has("game")) {
            game = object.get("game").getAsString();
        }
        if(object.has("status")) {
            title = object.get("status").getAsString();
        }
        lastUpdated = System.currentTimeMillis();
    }
}
