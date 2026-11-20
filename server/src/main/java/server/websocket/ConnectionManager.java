package server.websocket;

import com.google.gson.Gson;
import org.eclipse.jetty.websocket.api.Session;
import websocket.messages.ServerMessage;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ConnectionManager {
    private final Map<Integer, Set<Session>> sessionsByGameID = new HashMap<>();
    private final Gson gson = new Gson();

    public synchronized void add(int gameID, Session session) {
        if (!sessionsByGameID.containsKey(gameID)) {
            sessionsByGameID.put(gameID, new HashSet<>());
        }
        sessionsByGameID.get(gameID).add(session);
    }

    public synchronized void remove(Session session) {
        for (Set<Session> sessions : sessionsByGameID.values()) {
            sessions.remove(session);
        }
    }

    public synchronized void send(Session session, ServerMessage message) throws IOException {
        if (session.isOpen()) {
            session.getRemote().sendString(gson.toJson(message));
        }
    }

    public synchronized void broadcast(int gameID, ServerMessage message) throws IOException {
        broadcastExcept(gameID, null, message);
    }

    public synchronized void broadcastExcept(int gameID, Session excludedSession, ServerMessage message) throws IOException {
        for (Session session : sessionsInGame(gameID)) {
            if (!session.equals(excludedSession)) {
                send(session, message);
            }
        }
    }

    private Set<Session> sessionsInGame(int gameID) {
        return sessionsByGameID.getOrDefault(gameID, new HashSet<>());
    }
}
