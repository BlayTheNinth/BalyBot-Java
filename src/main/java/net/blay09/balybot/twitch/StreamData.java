package net.blay09.balybot.twitch;

import com.google.gson.JsonObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.TimeZone;

public class StreamData {

    public final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");

    private long lastUpdated;
    private boolean isLive;
    private int viewers;
    private long created_at;

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
            try {
                dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
                created_at = dateFormat.parse(stream.get("created_at").getAsString()).getTime();
            } catch (ParseException e) {
                e.printStackTrace();
            }
        } else {
            viewers = 0;
        }
        lastUpdated = System.currentTimeMillis();
    }

    public boolean isLive() {
        return isLive;
    }

    public long getUptime() {
        return System.currentTimeMillis() - created_at;
    }
}
