package net.blay09.balybot;

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
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

public class TwitchAPI {

    private static final int CACHE_LIFETIME = 60 * 1000;

    private static final Map<String, ChannelData> channelDataCache = new HashMap<>();

    public static ChannelData getChannelData(String channelName) {
        ChannelData channelData = channelDataCache.get(channelName.toLowerCase());
        if(channelData == null || channelData.getLastUpdated() - System.currentTimeMillis() > CACHE_LIFETIME) {
            if(channelData == null) {
                channelData = new ChannelData();
            }
            try {
                URI uri = new URIBuilder()
                        .setScheme("https")
                        .setHost("api.twitch.tv")
                        .setPath("/kraken/streams/" + channelName.substring(1))
                        .build();
                HttpGet httpGet = new HttpGet(uri);
                httpGet.setHeader(HttpHeaders.CONTENT_TYPE, "application/vnd.twitchtv.v3+json");
                httpGet.setHeader(HttpHeaders.ACCEPT, "application/vnd.twitchtv.v3+json");
                try(CloseableHttpClient httpClient = HttpClients.createDefault(); CloseableHttpResponse response = httpClient.execute(httpGet)) {
                    Gson gson = new Gson();
                    String jsonString = IOUtils.toString(response.getEntity().getContent());
                    JsonObject jsonObject = gson.fromJson(jsonString, JsonObject.class);
                    channelData.updateFromJson(jsonObject);
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

}
