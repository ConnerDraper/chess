package server.websocket;

import chess.*;
import com.google.gson.Gson;
import dataaccess.AuthDAO;
import dataaccess.DataAccessException;
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

            UserGameCommand.CommandType commandType = command.getCommandType();
            if (commandType == UserGameCommand.CommandType.CONNECT) {
                connect(session, username, game);
            } else if (commandType == UserGameCommand.CommandType.MAKE_MOVE) {
                makeMove(session, username, game, command.getMove());
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

    private void checkMoveForErrors(String username, GameData game, ChessMove move) throws BadRequestException {
        ChessGame chessGame = game.game();

        if (!username.equals(game.whiteUsername()) && (!username.equals(game.blackUsername()))) {
            throw new BadRequestException("cannot make a move because you are an observer.");
        } else if (chessGame.isGameOver()) {
            throw new BadRequestException("the game is already over.");
        } else if (!username.equals(currentPlayer(game))) {
            throw new BadRequestException("it is not your turn.");
        }

        ChessPiece piece = chessGame.getBoard().getPiece(move.getStartPosition());
        if (piece == null || piece.getTeamColor() != chessGame.getTeamTurn()) {
            throw new BadRequestException("you can only move your own pieces.");
        }
    }

    private void makeMove(Session session, String username, GameData game, ChessMove move)
        throws BadRequestException, DataAccessException, IOException {
        // check for invalid cases
        checkMoveForErrors(username, game, move);
        ChessGame chessGame = game.game();
        try {
            chessGame.makeMove(move);
        } catch (InvalidMoveException exception) {
            throw new BadRequestException("that move is not legal.");
        }

        // check for status updates
        ChessGame.TeamColor teamTurn = chessGame.getTeamTurn();
        String player = currentPlayer(game);
        String statusText = null;
        if (chessGame.isInCheckmate(teamTurn)) {
            statusText = player + " is in checkmate. Game over.";
            chessGame.setGameOver(true);
        } else if (chessGame.isInStalemate(teamTurn)) {
            statusText = "Stalemate. Game over.";
            chessGame.setGameOver(true);
        } else if (chessGame.isInCheck(teamTurn)) {
            statusText = player + " is in check.";
        }

        // status updates
        gameDAO.updateGame(game);
        connections.broadcast(game.gameID(), loadGameMessage(chessGame));
        connections.broadcastExcept(game.gameID(), session,
                notificationMessage(username + " moved " + moveName(move) + ".")
        );
        if (statusText != null) {
            connections.broadcast(game.gameID(), notificationMessage(statusText));
        }
    }

    private String currentPlayer(GameData game) {
        if (game.game().getTeamTurn() == ChessGame.TeamColor.WHITE) {
            return game.whiteUsername();
        }
        return game.blackUsername();
    }

    private String statusText(GameData game) {return null;}

    private String moveName(ChessMove move) {
        return squareName(move.getStartPosition()) + " to " + squareName(move.getEndPosition());
    }

    private String squareName(ChessPosition position) {
        return String.valueOf("abcdefgh".charAt(position.getColumn() - 1)) + position.getRow();
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
