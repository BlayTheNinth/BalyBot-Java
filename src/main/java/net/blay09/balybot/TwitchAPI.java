package net.blay09.balybot;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
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

public class TwitchAPI {

    public static boolean isChannelLive(String channelName) {
        try {
            URI uri = new URIBuilder()
                    .setScheme("https")
                    .setHost("api.twitch.tv")
                    .setPath("kraken/streams/" + channelName)
                    .build();
            HttpGet httpGet = new HttpGet(uri);
            httpGet.setHeader(HttpHeaders.CONTENT_TYPE, "application/vnd.twitchtv.v3+json");
            httpGet.setHeader(HttpHeaders.ACCEPT, "application/vnd.twitchtv.v3+json");
            try(CloseableHttpClient httpClient = HttpClients.createDefault(); CloseableHttpResponse response = httpClient.execute(httpGet)) {
                Gson gson = new Gson();
                JsonObject jsonObject = gson.fromJson(new InputStreamReader(response.getEntity().getContent()), JsonObject.class);
                return jsonObject.get("stream").isJsonNull();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        return false;
    }

}
