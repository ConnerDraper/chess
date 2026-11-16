package client;

import com.google.gson.Gson;
import java.net.http.HttpClient;

public class ServerFacade {
    private String url;
    private HttpClient http = HttpClient.newHttpClient();
    private Gson gson = new Gson();

    public ServerFacade(int port) {
        url = "http://localhost:" + port;
    }
}
