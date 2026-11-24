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
        switch (command) {
            case "help", "h" -> showHelp();
            case "resign" -> resign();
            case "highlight", "hl" -> highlight(words);
            case "move", "m" -> makeMove(words);
            case "redraw", "r" -> redraw();
            default -> System.out.println("Invalid command. Type 'help' to view commands.");
        }
    }

    private void printPrompt() {
        System.out.print(SET_TEXT_COLOR_MAGENTA + "chess [" + roleName() + "] > " + RESET_TEXT_COLOR);
    }

    private String roleName() {
        if (color == null) {
            return "observing";
        }
        return color.toLowerCase();
    }

    private void showHelp() {
        System.out.println("redraw                         | Redraws the board");
        System.out.println("highlight <square>             | Shows legal moves for a piece, e.g. highlight e2");
        if (color != null) {
            System.out.println("move <from> <to> [promotion]   | Moves a piece, e.g. move e2 e4 or move a7 a8 queen");
            System.out.println("resign                         | Forfeits the game");
        }
        System.out.println("leave                          | Leaves the game");
        System.out.println("help                           | Shows valid commands");
    }

    private void redraw() {
        if (chessGame == null) {
            System.out.println("The board is still loading. Try again in a second.");
            return;
        }
        printBoard(new ArrayList<>());
    }

    private void highlight(String[] words) {
        if (words.length != 2) {
            System.out.println("Error. Correct usage: highlight <square>");
            return;
        }
        if (chessGame == null) {
            System.out.println("The board is still loading. Try again in a second.");
            return;
        }

        ChessPosition square = pickSquare(words[1]);
        if (square == null) {
            return;
        }
        if (chessGame.getBoard().getPiece(square) == null) {
            System.out.println("There is no piece on " + words[1] + ".");
            return;
        }

        Collection<ChessMove> legalMoves = chessGame.validMoves(square);
        printBoard(legalMoves);
        if (legalMoves.isEmpty()) {
            System.out.println("That piece has no legal moves.");
        }
    }

    private void makeMove(String[] words) throws Exception {
        if (color == null) {
            System.out.println("Error. Observers cannot move pieces.");
            return;
        }
        if (words.length != 3 && words.length != 4) {
            System.out.println("Error. Correct usage: move <from> <to> [promotion]");
            return;
        }

        ChessPosition from = pickSquare(words[1]);
        ChessPosition to = pickSquare(words[2]);
        if (from == null || to == null) {
            return;
        }

        ChessPiece.PieceType promotion = null;
        if (words.length == 4) {
            promotion = pickPromotion(words[3]);
            if (promotion == null) {
                return;
            }
        }

        facade.makeMove(authToken, game.gameID(), new ChessMove(from, to, promotion));
    }

    private void resign() throws Exception {
        if (color == null) {
            System.out.println("Error. Observers cannot resign.");
            return;
        }

        System.out.print("Are you sure you want to resign? (yes/no) ");
        String answer = scanner.nextLine().trim().toLowerCase();
        if (answer.equals("yes") || answer.equals("y")) {
            facade.resign(authToken, game.gameID());
        } else {
            System.out.println("Okay, keep playing!");
        }
    }

    private ChessPosition pickSquare(String square) {
        String lowered = square.toLowerCase();
        if (lowered.length() == 2) {
            int col = "abcdefgh".indexOf(lowered.charAt(0)) + 1;
            int row = "12345678".indexOf(lowered.charAt(1)) + 1;
            if (col > 0 && row > 0) {
                return new ChessPosition(row, col);
            }
        }

        System.out.println("Error. Invalid square: " + square + ". Squares look like e2 or h7.");
        return null;
    }

    private ChessPiece.PieceType pickPromotion(String piece) {
        String lowered = piece.toLowerCase();
        if (lowered.equals("queen")) {
            return ChessPiece.PieceType.QUEEN;
        } else if (lowered.equals("rook")) {
            return ChessPiece.PieceType.ROOK;
        } else if (lowered.equals("bishop")) {
            return ChessPiece.PieceType.BISHOP;
        } else if (lowered.equals("knight")) {
            return ChessPiece.PieceType.KNIGHT;
        }

        System.out.println("Error. Promotion must be queen, rook, bishop, or knight.");
        return null;
    }

    private void printBoard(Collection<ChessMove> highlights) {
        System.out.println(SET_TEXT_BOLD + "Game: " + game.gameName() + " | You are " + roleName() + RESET_TEXT_BOLD_FAINT);
        boolean whiteOnBottom = !"BLACK".equals(color);
        BoardRenderer.drawBoard(chessGame.getBoard(), whiteOnBottom, highlights);

        if (chessGame.isGameOver()) {
            System.out.println("The game is over.");
        } else {
            System.out.println("It's " + chessGame.getTeamTurn().toString().toLowerCase() + "'s turn.");
        }
    }

    @Override
    public void notify(ServerMessage msg) {
        System.out.println();
        ServerMessage.ServerMessageType type = msg.getServerMessageType();
        if (type == ServerMessage.ServerMessageType.LOAD_GAME) {
            chessGame = msg.getGame();
            printBoard(new ArrayList<>());
        } else if (type == ServerMessage.ServerMessageType.NOTIFICATION) {
            System.out.println(SET_TEXT_COLOR_YELLOW + msg.getMessage() + RESET_TEXT_COLOR);
        } else if (type == ServerMessage.ServerMessageType.ERROR) {
            System.out.println(SET_TEXT_COLOR_RED + msg.getErrorMessage() + RESET_TEXT_COLOR);
        }
        printPrompt();
    }
}