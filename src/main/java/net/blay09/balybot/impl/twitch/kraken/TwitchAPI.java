package net.blay09.balybot.impl.twitch.kraken;

import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import lombok.extern.log4j.Log4j2;
import net.blay09.balybot.impl.api.Channel;
import org.apache.commons.io.Charsets;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpHeaders;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.jetbrains.annotations.NonNls;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;

@Log4j2
public class TwitchAPI {

    @NonNls
    public static final String CLIENT_ID = "qnwfc63w5qcttxt6u7fobly398f6cwi";

    public static final int CACHE_LIFETIME = 60 * 1000;

    private static final Map<Channel, ChannelData> channelDataCache = Maps.newHashMap();
    private static final Map<Channel, StreamData> streamDataCache = Maps.newHashMap();

    public static String getUsername(String token) {
        if(token.startsWith("oauth:") && token.length() > 6) {
            token = token.substring(6);
        }
        try {
            URI uri = new URIBuilder()
                    .setScheme("https")
                    .setHost("api.twitch.tv")
                    .setPath("/kraken")
                    .setParameter("api_version", "3")
                    .setParameter("client_id", CLIENT_ID)
                    .setParameter("oauth_token", token)
                    .build();
            HttpGet httpGet = new HttpGet(uri);
            httpGet.setHeader(HttpHeaders.CONTENT_TYPE, "application/vnd.twitchtv.v3+json");
            httpGet.setHeader(HttpHeaders.ACCEPT, "application/vnd.twitchtv.v3+json");
            try(CloseableHttpClient httpClient = HttpClients.createDefault(); CloseableHttpResponse response = httpClient.execute(httpGet)) {
                Gson gson = new Gson();
                String jsonString = IOUtils.toString(response.getEntity().getContent(), Charsets.UTF_8);
                JsonObject jsonObject = gson.fromJson(jsonString, JsonObject.class);
                if(jsonObject != null && jsonObject.has("token")) {
                    JsonObject tokenObject = jsonObject.getAsJsonObject("token");
                    if(tokenObject.has("user_name")) {
                        return tokenObject.get("user_name").getAsString();
                    } else {
                        throw new RuntimeException("Failed to lookup Twitch username: missing user_name in json response");
                    }
                } else {
                    throw new RuntimeException("Failed to lookup Twitch username: missing token in json response");
                }
            } catch (IOException e) {
                throw new RuntimeException("Failed to lookup Twitch username: ", e);
            }
        } catch (URISyntaxException e) {
            throw new RuntimeException("Failed to lookup Twitch username: ", e);
        }
    }

    public static ChannelData getChannelData(Channel channel) {
        ChannelData channelData = channelDataCache.get(channel);
        if(channelData == null || channelData.requiresUpdate()) {
            if(channelData == null) {
                channelData = new ChannelData();
            }
            try {
                URI uri = new URIBuilder()
                        .setScheme("https")
                        .setHost("api.twitch.tv")
                        .setPath("/kraken/channels/" + (channel.getName().startsWith("#") ? channel.getName().substring(1) : channel.getName()))
                        .build();
                HttpGet httpGet = new HttpGet(uri);
                httpGet.setHeader(HttpHeaders.CONTENT_TYPE, "application/vnd.twitchtv.v3+json");
                httpGet.setHeader(HttpHeaders.ACCEPT, "application/vnd.twitchtv.v3+json");
                try(CloseableHttpClient httpClient = HttpClients.createDefault(); CloseableHttpResponse response = httpClient.execute(httpGet)) {
                    Gson gson = new Gson();
                    String jsonString = IOUtils.toString(response.getEntity().getContent(), Charsets.UTF_8);
                    JsonObject jsonObject = gson.fromJson(jsonString, JsonObject.class);
                    if(jsonObject != null) {
                        channelData.updateFromJson(jsonObject);
                    }
                    channelDataCache.put(channel, channelData);
                } catch (IOException e) {
                    log.error("Failed to retrieve channel data", e);
                }
            } catch (URISyntaxException e) {
                log.error("Failed to retrieve channel data", e);
            }
        }
        return channelData;
    }

    public static StreamData getStreamData(Channel channel) {
        StreamData streamData = streamDataCache.get(channel);
        if(streamData == null || streamData.requiresUpdate()) {
            if(streamData == null) {
                streamData = new StreamData();
            }
            try {
                URI uri = new URIBuilder()
                        .setScheme("https")
                        .setHost("api.twitch.tv")
                        .setPath("/kraken/streams/" + (channel.getName().startsWith("#") ? channel.getName().substring(1) : channel.getName()))
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
                    streamDataCache.put(channel, streamData);
                } catch (IOException e) {
                    log.error("Failed to retrieve stream data", e);
                }
            } catch (URISyntaxException e) {
                log.error("Failed to retrieve stream data", e);
            }
        }
        return streamData;
    }

}
