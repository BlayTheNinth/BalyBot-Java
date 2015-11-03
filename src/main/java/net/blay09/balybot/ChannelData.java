package net.blay09.balybot;

import com.google.gson.JsonObject;

public class ChannelData {

    private long lastUpdated;
    private boolean isLive;
    private String game = "Could not retrieve game.";
    private String title = "Could not retrieve title.";
    private int viewers;

    public String getTitle() {
        return title;
    }

    public String getGame() {
        return game;
    }

    public long getLastUpdated() {
        return lastUpdated;
    }

    public boolean isLive() {
        return isLive;
    }

    public int getViewers() {
        return viewers;
    }

    public void updateFromJson(JsonObject object) {
        isLive = !object.get("stream").isJsonNull();
        if(isLive) {
            JsonObject stream = object.getAsJsonObject("stream");
            game = stream.get("game").getAsString();
            title = stream.get("title").getAsString();
            viewers = stream.get("viewers").getAsInt();
            JsonObject channel = stream.getAsJsonObject("channel");
            title = channel.get("status").getAsString();
        }
        lastUpdated = System.currentTimeMillis();
    }
}
