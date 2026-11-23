package server.websocket;

import chess.ChessGame;
import com.google.gson.Gson;
import dataaccess.AuthDAO;
import dataaccess.GameDAO;
import io.javalin.websocket.*;
import model.AuthData;
import model.GameData;
import org.eclipse.jetty.websocket.api.Session;
import service.exception.BadRequestException;
import service.exception.UnauthorizedException;
import websocket.commands.UserGameCommand;
import websocket.messages.ServerMessage;

import java.io.IOException;

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
        context.enableAutomaticPings();
    }

    @Override
    public void handleMessage(WsMessageContext context) throws IOException {
        Session session = context.session;
        try {
            UserGameCommand command = gson.fromJson(context.message(), UserGameCommand.class);
            AuthData authData = authDAO.getAuth(command.getAuthToken());
            if (authData == null) {
                throw new UnauthorizedException("unauthorized");
            }
            String username = authData.username();

            GameData game = gameDAO.getGame(command.getGameID());
            if (game == null) {
                throw new BadRequestException("game does not exist");
            }

            if (command.getCommandType() == UserGameCommand.CommandType.CONNECT) {
                connect(session, username, game);
            }
        }
        catch (Exception error) {
            connections.send(session, errorMessage("Error: " + error.getMessage()));
        }
    }

    @Override
    public void handleClose(WsCloseContext context) {
        connections.remove(context.session);
    }

    private void connect(Session session, String username, GameData game) throws IOException {
        connections.add(game.gameID(), session);
        connections.send(session, loadGameMessage(game.game()));
        connections.broadcastExcept(game.gameID(), session,
                notificationMessage(username +
                        " has joined the game as " + roleName(username, game) + "."
                ));
    }

    private String roleName(String username, GameData game) {
        boolean isWhite = username.equals(game.whiteUsername());
        boolean isBlack = username.equals(game.blackUsername());
        if (isWhite) {
            if (isBlack) {
                return "WHITE & BLACK";
            } else {
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
