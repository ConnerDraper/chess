package client;

import com.google.gson.Gson;
import jakarta.websocket.ContainerProvider;
import jakarta.websocket.Endpoint;
import jakarta.websocket.EndpointConfig;
import jakarta.websocket.Session;
import jakarta.websocket.WebSocketContainer;
import websocket.commands.UserGameCommand;
import websocket.messages.ServerMessage;

import java.net.URI;

public class WebSocketCommunicator extends Endpoint {
    private final Session session;
    private final ServerMessageObserver observer;
    private final Gson gson = new Gson();

    public WebSocketCommunicator(int port, ServerMessageObserver observer)
        throws Exception {
        this.observer = observer;
        URI uri = URI.create("ws://localhost:" + port + "/ws");
        WebSocketContainer container = ContainerProvider.getWebSocketContainer();
        session = container.connectToServer(this, uri);

        session.addMessageHandler(String.class, json -> {
            ServerMessage msg = gson.fromJson(json, ServerMessage.class);
            observer.notify(msg);
        });
    }

    @Override
    public void onOpen(Session session, EndpointConfig config) {
    }

    public void sendCommand(UserGameCommand cmd) throws Exception {
        session.getBasicRemote().sendText(gson.toJson(cmd));
    }

    public void close() throws Exception {
        session.close();
    }
}
