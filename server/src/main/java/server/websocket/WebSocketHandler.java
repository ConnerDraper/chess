package server.websocket;

import chess.ChessGame;
import com.google.gson.Gson;
import dataaccess.AuthDAO;
import dataaccess.DataAccessException;
import dataaccess.GameDAO;
import io.javalin.websocket.*;
import model.GameData;

import java.io.IOException;
import org.eclipse.jetty.websocket.api.Session;
import service.exception.BadRequestException;
import service.exception.UnauthorizedException;
import websocket.messages.ServerMessage;

public class WebSocketHandler implements WsConnectHandler, WsMessageHandler, WsCloseHandler {
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

    @Override
    public void handleConnect(WsConnectContext context) {
    }

    @Override
    public void handleMessage(WsMessageContext context) throws IOException {
    }

    @Override
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
        boolean isWhite = username.equals(game.whiteUsername());
        boolean isBlack = username.equals(game.blackUsername());
        if (isWhite) {
            if (isBlack) {
                return "WHITE & BLACK";
            }
            else {
                return "WHITE";
            }
        } else if (isBlack) {
            return "BLACK";
        } else {
            return "OBSERVER";
        }
    }

    private ServerMessage loadGameMessage(ChessGame game) {
        ServerMessage msg = new ServerMessage(ServerMessage.ServerMessageType.LOAD_GAME);
        msg.setGame(game);
        return msg;
    }

    private ServerMessage notificationMessage(String text) {
        ServerMessage msg = new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION);
        msg.setMessage(text);
        return msg;
    }

    private ServerMessage errorMessage(String text) {
        ServerMessage msg = new ServerMessage(ServerMessage.ServerMessageType.ERROR);
        msg.setErrorMessage(text);
        return msg;
    }
}
