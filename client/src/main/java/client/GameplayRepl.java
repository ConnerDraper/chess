package client;

import chess.ChessGame;
import chess.ChessMove;
import chess.ChessPiece;
import chess.ChessPosition;
import model.GameData;
import ui.BoardRenderer;
import websocket.messages.ServerMessage;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Scanner;

import static ui.EscapeSequences.*;

public class GameplayRepl implements ServerMessageObserver {
    private final ServerFacade facade;
    private final Scanner scanner;
    private final String authToken;
    private final GameData game;
    private final String color;
    private ChessGame chessGame = null;

    public GameplayRepl(ServerFacade facade, Scanner scanner, String authToken, GameData game, String color) {
        this.facade = facade;
        this.scanner = scanner;
        this.authToken = authToken;
        this.game = game;
        this.color = color;
    }

    public void run() throws Exception {
    }

    public void gameplayMenu(String command, String[] words) throws Exception {
    }

    private void showHelp() {
    }

    private void redraw() {
    }

    private void highlight(String[] words) {
    }

    private void makeMove(String[] words) throws Exception {
    }

    private void resign() throws Exception {
    }

    private ChessPosition pickSquare(String square) {
        return null;
    }

    private ChessPiece.PieceType pickPromotion(String piece) {
        return null;
    }

    private void printBoard(Collection<ChessMove> highlights) {
    }

    private void printPrompt() {
    }

    private String roleName() {
        return null;
    }

    @Override
    public void notify(ServerMessage msg) {
    }
}