package client;

import com.google.gson.Gson;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class ServerFacade {
    private String url;
    private HttpClient http = HttpClient.newHttpClient();
    private Gson gson = new Gson();

    public ServerFacade(int port) {
        url = "http://localhost:" + port;
    }

    private record ErrorMessage(String message) {}

    private String sendRequest(String method, String path, Object body, String authToken) throws Exception {
        String jsonBody = "";
        if (body != null) {
            jsonBody = gson.toJson(body);
        }

        URI uri = URI.create(url + path);
        HttpRequest.Builder builder = HttpRequest.newBuilder(uri);
        builder.method(method, HttpRequest.BodyPublishers.ofString(jsonBody));

        if (authToken != null) {
            builder.header("authorization", authToken);
        }
        HttpRequest request = builder.build();

        HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            ErrorMessage msg = gson.fromJson(response.body(), ErrorMessage.class);
            throw new Exception(msg.message());
        }

        return response.body();
    }
}
