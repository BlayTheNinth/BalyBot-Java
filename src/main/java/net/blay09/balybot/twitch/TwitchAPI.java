package net.blay09.balybot.twitch;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpHeaders;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

public class TwitchAPI {

    public static final int CACHE_LIFETIME = 60 * 1000;

    private static final Map<String, ChannelData> channelDataCache = new HashMap<>();
    private static final Map<String, StreamData> streamDataCache = new HashMap<>();

    public static ChannelData getChannelData(String channelName) {
        ChannelData channelData = channelDataCache.get(channelName.toLowerCase());
        if(channelData == null || channelData.requiresUpdate()) {
            if(channelData == null) {
                channelData = new ChannelData();
            }
            try {
                URI uri = new URIBuilder()
                        .setScheme("https")
                        .setHost("api.twitch.tv")
                        .setPath("/kraken/channels/" + (channelName.startsWith("#") ? channelName.substring(1) : channelName))
                        .build();
                HttpGet httpGet = new HttpGet(uri);
                httpGet.setHeader(HttpHeaders.CONTENT_TYPE, "application/vnd.twitchtv.v3+json");
                httpGet.setHeader(HttpHeaders.ACCEPT, "application/vnd.twitchtv.v3+json");
                try(CloseableHttpClient httpClient = HttpClients.createDefault(); CloseableHttpResponse response = httpClient.execute(httpGet)) {
                    Gson gson = new Gson();
                    String jsonString = IOUtils.toString(response.getEntity().getContent());
                    JsonObject jsonObject = gson.fromJson(jsonString, JsonObject.class);
                    if(jsonObject != null) {
                        channelData.updateFromJson(jsonObject);
                    }
                    channelDataCache.put(channelName.toLowerCase(), channelData);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
        }
        return channelData;
    }

    public static StreamData getStreamData(String channelName) {
        StreamData streamData = streamDataCache.get(channelName.toLowerCase());
        if(streamData == null || streamData.requiresUpdate()) {
            if(streamData == null) {
                streamData = new StreamData();
            }
            try {
                URI uri = new URIBuilder()
                        .setScheme("https")
                        .setHost("api.twitch.tv")
                        .setPath("/kraken/streams/" + (channelName.startsWith("#") ? channelName.substring(1) : channelName))
                        .build();
                HttpGet httpGet = new HttpGet(uri);
                httpGet.setHeader(HttpHeaders.CONTENT_TYPE, "application/vnd.twitchtv.v3+json");
                httpGet.setHeader(HttpHeaders.ACCEPT, "application/vnd.twitchtv.v3+json");
                try(CloseableHttpClient httpClient = HttpClients.createDefault(); CloseableHttpResponse response = httpClient.execute(httpGet)) {
                    Gson gson = new Gson();
                    String jsonString = IOUtils.toString(response.getEntity().getContent());
                    JsonObject jsonObject = gson.fromJson(jsonString, JsonObject.class);
                    if(jsonObject != null) {
                        streamData.updateFromJson(jsonObject);
                    }
                    streamDataCache.put(channelName.toLowerCase(), streamData);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
        }
        return streamData;
    }

}
