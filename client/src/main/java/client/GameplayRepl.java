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
import java.util.Locale;
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
        facade.connect(authToken, game.gameID(), this);
        boolean inGame = true;

        while (inGame) {
            printPrompt();
            String line = scanner.nextLine();
            String[] words = line.trim().split("\\s+");
            String command = words[0].toLowerCase();

            if (command.equals("leave")) {
                facade.leave(authToken, game.gameID());
                System.out.println("You have left the game.");
                inGame = false;
            } else {
                try {
                    gameplayMenu(command, words);
                } catch (Exception exception) {
                    System.out.println("Lost connection to the server. Type 'leave' and join again.");
                }
            }
        }
    }

    private void gameplayMenu(String command, String[] words) throws Exception {
        if (command.equals("help") || command.equals("h")) {
            showHelp();
        } else if (command.equals("resign")) {
            resign();
        } else if (command.equals("highlight") || command.equals("hl")) {
            highlight(words);
        } else if (command.equals("move") || command.equals("m")) {
            makeMove(words);
        } else if (command.equals("redraw") || command.equals("r")) {
            redraw();
        } else {
            System.out.println("Invalid command. Type 'help' to view commands.");
        }
    }

    private void printPrompt() {
        System.out.print(SET_TEXT_COLOR_MAGENTA + "chess [" + roleName() + "] >" + RESET_TEXT_COLOR);
    }

    private String roleName() {
        if (color == null) {
            return "observing";
        }
        return color.toLowerCase();
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

    @Override
    public void notify(ServerMessage msg) {
    }
}