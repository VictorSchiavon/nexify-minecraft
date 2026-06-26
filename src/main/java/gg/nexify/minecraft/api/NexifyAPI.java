package gg.nexify.minecraft.api;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import gg.nexify.minecraft.api.model.DeliveryResponse;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class NexifyAPI {

    private final String baseUrl;
    private final String apiToken;
    private final String encodedToken;
    private final Gson gson = new Gson();

    public NexifyAPI(String baseUrl, String apiToken) {
        this.baseUrl = baseUrl;
        this.apiToken = apiToken;
        try {
            this.encodedToken = URLEncoder.encode(apiToken, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    public DeliveryResponse fetchPending() throws IOException {
        String url = baseUrl + "/store/deliveries/pending/" + encodedToken;
        String response = get(url);
        return gson.fromJson(response, DeliveryResponse.class);
    }

    public boolean markComplete(String deliveryId) throws IOException {
        String url = baseUrl + "/store/deliveries/complete/" + encodedToken;
        JsonObject body = new JsonObject();
        body.addProperty("deliveryId", deliveryId);
        String response = post(url, body.toString());
        return response != null;
    }

    private String get(String urlStr) throws IOException {
        HttpURLConnection conn = openConnection(urlStr, "GET");
        return readResponse(conn);
    }

    private String post(String urlStr, String jsonBody) throws IOException {
        HttpURLConnection conn = openConnection(urlStr, "POST");
        conn.setDoOutput(true);
        conn.setRequestProperty("Content-Type", "application/json");
        try (OutputStream os = conn.getOutputStream()) {
            os.write(jsonBody.getBytes(StandardCharsets.UTF_8));
        }
        return readResponse(conn);
    }

    private HttpURLConnection openConnection(String urlStr, String method) throws IOException {
        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod(method);
        conn.setRequestProperty("Accept", "application/json");
        conn.setRequestProperty("User-Agent", "NexifyMinecraft/1.0");
        conn.setConnectTimeout(10000);
        conn.setReadTimeout(10000);
        return conn;
    }

    private String readResponse(HttpURLConnection conn) throws IOException {
        int code = conn.getResponseCode();
        InputStream stream = (code >= 200 && code < 300) ? conn.getInputStream() : conn.getErrorStream();
        if (stream == null) return null;
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) sb.append(line);
            return sb.toString();
        }
    }
}
