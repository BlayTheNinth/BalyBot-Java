package net.blay09.balybot.impl.twitch.kraken;

import com.google.gson.JsonObject;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.TimeZone;

@Getter
@Log4j2
public class StreamData {

    public final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");

    private long lastUpdated;
    private boolean isLive;
    private int viewers;
    private long created_at;

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
                log.error("Failed to parse stream data", e);
            }
        } else {
            viewers = 0;
        }
        lastUpdated = System.currentTimeMillis();
    }

    public long getUptime() {
        return System.currentTimeMillis() - created_at;
    }
}
