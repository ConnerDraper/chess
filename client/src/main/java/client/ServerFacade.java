package client;

import com.google.gson.Gson;
import model.AuthData;
import model.GameData;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

import chess.ChessMove;
import websocket.commands.UserGameCommand;

public class ServerFacade {
    private final String url;
    private final int port;
    private final HttpClient http = HttpClient.newHttpClient();
    private final Gson gson = new Gson();
    private WebSocketCommunicator communicator = null;

    public ServerFacade(int port) {
        this.port = port;
        url = "http://localhost:" + port;
    }

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

    public AuthData register(String username, String password, String email) throws Exception {
        RegisterBody body = new RegisterBody(username, password, email);
        String json = sendRequest("POST", "/user", body, null);
        return gson.fromJson(json, AuthData.class);
    }

    public AuthData login(String username, String password) throws Exception {
        LoginBody body = new LoginBody(username, password);
        String json = sendRequest("POST", "/session", body, null);
        return gson.fromJson(json, AuthData.class);
    }

    public void logout(String authToken) throws Exception {
        sendRequest("DELETE", "/session", null, authToken);
    }

    public int createGame(String authToken, String gameName) throws Exception {
        CreateGameBody body = new CreateGameBody(gameName);
        String json = sendRequest("POST", "/game", body, authToken);
        CreateGameReply reply = gson.fromJson(json, CreateGameReply.class);
        return reply.gameID();
    }

    public List<GameData> listGames(String authToken) throws Exception {
        String json = sendRequest("GET", "/game", null, authToken);
        ListGamesReply reply = gson.fromJson(json, ListGamesReply.class);
        return reply.games();
    }

    public void joinGame(String authToken, String playerColor, int gameID) throws Exception {
        JoinGameBody body = new JoinGameBody(playerColor, gameID);
        sendRequest("PUT", "/game", body, authToken);
    }

    public void clear() throws Exception {
        sendRequest("DELETE", "/db", null, null);
    }

    public void connect(String authToken, int gameID, ServerMessageObserver observer) throws Exception {
        communicator = new WebSocketCommunicator(port, observer);
        sendCommand(new UserGameCommand(UserGameCommand.CommandType.CONNECT, authToken, gameID));
    }

    public void makeMove(String authToken, int gameID, ChessMove move) throws Exception {
        UserGameCommand cmd = new UserGameCommand(UserGameCommand.CommandType.MAKE_MOVE, authToken, gameID);
        cmd.setMove(move);
        sendCommand(cmd);
    }

    public void resign(String authToken, int gameID) throws Exception {
        sendCommand(new UserGameCommand(UserGameCommand.CommandType.RESIGN, authToken, gameID));
    }

    public void leave(String authToken, int gameID) throws Exception{
        sendCommand(new UserGameCommand(UserGameCommand.CommandType.LEAVE, authToken, gameID));
        communicator.close();
        communicator = null;
    }

    private void sendCommand(UserGameCommand cmd) throws Exception {
        if (communicator == null) {
            throw new Exception("not currently connected to a game.");
        }
        communicator.sendCommand(cmd);
    }

    private record ErrorMessage(String message) {
    }

    private record RegisterBody(String username, String password, String email) {
    }

    private record LoginBody(String username, String password) {
    }

    private record CreateGameBody(String gameName) {
    }

    private record CreateGameReply(int gameID) {
    }

    private record JoinGameBody(String playerColor, int gameID) {
    }

    private record ListGamesReply(List<GameData> games) {
    }

}
