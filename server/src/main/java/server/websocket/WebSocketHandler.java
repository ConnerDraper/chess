package server.websocket;

import chess.ChessGame;
import com.google.gson.Gson;
import dataaccess.AuthDAO;
import dataaccess.DataAccessException;
import dataaccess.GameDAO;
import io.javalin.websocket.WsCloseContext;
import io.javalin.websocket.WsConnectContext;
import io.javalin.websocket.WsMessageContext;
import model.GameData;

import java.io.IOException;
import org.eclipse.jetty.websocket.api.Session;
import service.exception.BadRequestException;
import service.exception.UnauthorizedException;
import websocket.messages.ServerMessage;

public class WebSocketHandler {
    private final Gson gson = new Gson();
    private final ConnectionManager connections = new ConnectionManager();
    private final AuthDAO authDAO;
    private final GameDAO gameDAO;

    public WebSocketHandler(
            AuthDAO authDAO,
            GameDAO gameDAO
    ) {
        this.authDAO = authDAO;
        this.gameDAO = gameDAO;
    }

    public void handleConnect(WsConnectContext context) {
    }

    public void handleMessage(WsMessageContext context) throws IOException {
    }

    public void handleClose(WsCloseContext context) {
    }

    private void connect(Session session, String username, GameData game) throws IOException {
    }

    private String findUsername(String authToken) throws DataAccessException, UnauthorizedException {
        return null;
    }

    private GameData findGame(Integer gameID) throws DataAccessException, BadRequestException {
        return null;
    }

    private String roleName(String username, GameData game) {
        return null;
    }

    private ServerMessage loadGameMessage(ChessGame game) {
        return null;
    }

    private ServerMessage notificationMessage(String text) {
        return null;
    }

    private ServerMessage errorMessage(String text) {
        return null;
    }
}
