package net.blay09.balybot.impl.twitch.kraken;

import com.google.gson.JsonObject;
import lombok.Getter;

@Getter
public class ChannelData {

    private long lastUpdated;
    private String game = "Could not retrieve game.";
    private String title = "Could not retrieve title.";

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
