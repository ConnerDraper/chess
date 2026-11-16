package client;

import chess.ChessBoard;
import model.AuthData;
import model.GameData;
import ui.BoardRenderer;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import static ui.EscapeSequences.*;

public class ChessRepl {

    private final ServerFacade facade;
    private final Scanner scanner = new Scanner(System.in);
    private String authToken = null;
    private String username = null;
    private List<GameData> savedGames = new ArrayList<>();

    public ChessRepl(int port) {
        facade = new ServerFacade(port);
    }

    public void run() {
        System.out.println("♕ Let's play some chess! Type 'help' if you get stuck.");
        boolean running = true;

        while (running) {
            if (authToken == null) {
                System.out.print(SET_TEXT_COLOR_GREEN + "chess > " + RESET_TEXT_COLOR);
            } else {
                System.out.print(SET_TEXT_COLOR_BLUE + "chess (" + username + ") > " + RESET_TEXT_COLOR);
            }

            String line = scanner.nextLine();
            String[] words = line.trim().split("\\s+");
            String command = words[0].toLowerCase();

            if (command.equals("quit") || command.equals("q")) {
                System.out.println("Thanks for playing. See you next time!");
                running = false;
            } else {
                try {
                    if (authToken == null) {
                        loggedOutMenu(command, words);
                    } else {
                        loggedInMenu(command, words);
                    }
                } catch (Exception e) {
                    System.out.println(friendlyError(e.getMessage()));
                }
            }
        }
    }

    private void loggedOutMenu(String command, String[] words) throws Exception {
        if (command.equals("help") || command.equals("h")) {
            showHelp();
        } else if (command.equals("register") || command.equals("r")) {
            register(words);
        } else if (command.equals("login")) {
            login(words);
        } else {
            System.out.println("I don't know that command. Type 'help' to see what you can do.");
        }
    }

    private void loggedInMenu(String command, String[] words) throws Exception {
        if (command.equals("help") || command.equals("h")) {
            showHelp();
        } else if (command.equals("create") || command.equals("c")) {
            createGame(words);
        } else if (command.equals("list") || command.equals("l")) {
            listGames();
        } else if (command.equals("join") || command.equals("j")) {
            joinGame(words);
        } else if (command.equals("observe") || command.equals("o")) {
            observeGame(words);
        } else if (command.equals("logout")) {
            logout();
        } else {
            System.out.println("I don't know that command. Type 'help' to see what you can do.");
        }
    }

    private void showHelp() {
        if (authToken == null) {
            System.out.println("register <username> <password> <email> | Make a new account");
            System.out.println("login <username> <password>            | Logs in");
            System.out.println("quit                                   | Self-explanatory");
            System.out.println("help                                   | Shows valid commands");
        } else {
            System.out.println("create <name>                   | Creates a new game");
            System.out.println("list                            | Shows all games");
            System.out.println("join <number> <WHITE or BLACK>  | Joins a game");
            System.out.println("observe <number>                | Spectates a game");
            System.out.println("logout                          | Logs out");
            System.out.println("quit                            | Self-explanatory");
            System.out.println("help                            | Shows valid commands");
        }
    }

    private void register(String[] words) throws Exception {
        if (words.length != 4) {
            System.out.println("Error. Correct usage: register <username> <password> <email>");
            return;
        }

        AuthData auth = facade.register(words[1], words[2], words[3]);
        authToken = auth.authToken();
        username = auth.username();

        System.out.println("Successfully registered " + username);
        printHint("Try 'create <name>' to create a game, or 'help' to see valid commands.");
    }

    private void login(String[] words) throws Exception {
        if (words.length != 3) {
            System.out.println("Error. Correct usage: login <username> <password>");
            return;
        }

        AuthData auth = facade.login(words[1], words[2]);
        authToken = auth.authToken();
        username = auth.username();

        System.out.println("Welcome, " + username);
        printHint("Type 'list' to see games, or 'help' to see everything.");
    }

    private void logout() throws Exception {
        facade.logout(authToken);

        System.out.println("Goodbye, " + username);
        authToken = null;
        username = null;
        savedGames = new ArrayList<>();

        printHint("Type 'login' to sign back in, or 'quit' to leave.");
    }

    private void createGame(String[] words) throws Exception {
        if (words.length != 2) {
            System.out.println("Error. Correct usage: create <name>");
            return;
        }

        facade.createGame(authToken, words[1]);

        System.out.println("Created game " + words[1]);
        printHint("Type 'list' to find it.");
    }

    private void listGames() throws Exception {
        savedGames = facade.listGames(authToken);

        if (savedGames.isEmpty()) {
            System.out.println("No games to list.");
            printHint("Type 'create <name>' to make one.");
            return;
        }

        for (int i = 0; i < savedGames.size(); i++) {
            GameData game = savedGames.get(i);

            String whiteUser = game.whiteUsername();
            if (whiteUser == null) {
                whiteUser = "open";
            }

            String blackUser = game.blackUsername();
            if (blackUser == null) {
                blackUser = "open";
            }

            System.out.println((i + 1) + ") " + game.gameName() + ": "
                    + whiteUser + " vs. " + blackUser + yourGameMarker(game));
        }

        printHint("Type 'join <number> <WHITE or BLACK>' to play, or 'observe <number>' to watch.");
    }

    private void joinGame(String[] words) throws Exception {
        if (words.length != 3) {
            System.out.println("Error. Correct usage: join <number> <WHITE or BLACK>");
            return;
        }

        GameData game = pickGame(words[1]);
        if (game == null) {
            return;
        }

        String color = words[2].toUpperCase();
        if (!color.equals("WHITE") && !color.equals("BLACK")) {
            System.out.println("Error. Color must be WHITE or BLACK.");
            return;
        }

        facade.joinGame(authToken, color, game.gameID());

        ChessBoard board = new ChessBoard();
        board.resetBoard();
        printBoardTitle(game.gameName(), color);
        BoardRenderer.drawBoard(board, color.equals("WHITE"));
    }

    private void observeGame(String[] words) {
        if (words.length != 2) {
            System.out.println("Error. Correct usage: observe <number>");
            return;
        }

        GameData game = pickGame(words[1]);
        if (game == null) {
            return;
        }

        ChessBoard board = new ChessBoard();
        board.resetBoard();
        printBoardTitle(game.gameName(), "observing");
        BoardRenderer.drawBoard(board, true);
    }

    private GameData pickGame(String number) {
        int gameNumber;
        try {
            gameNumber = Integer.parseInt(number);
        } catch (Exception e) {
            System.out.println("Error. Invalid number: " + number);
            return null;
        }

        if (gameNumber < 1 || gameNumber > savedGames.size()) {
            System.out.println("Error. There is no game with number " + gameNumber);
            printHint("Type 'list' to see the games.");
            return null;
        }

        return savedGames.get(gameNumber - 1);
    }


    private String yourGameMarker(GameData game) {
        if (username.equals(game.whiteUsername()) || username.equals(game.blackUsername())) {
            return " ★";
        }
        return "";
    }

    private void printHint(String hint) {
        System.out.println(SET_TEXT_FAINT + "  " + hint + RESET_TEXT_BOLD_FAINT);
    }

    private void printBoardTitle(String gameName, String role) {
        System.out.println(SET_TEXT_BOLD + "Game: " + gameName + " | You are " + role + RESET_TEXT_BOLD_FAINT);
    }

    private String friendlyError(String serverMessage) {
        if (serverMessage == null) {
            return "Can't reach the chess server right now. Make sure it's running.";
        } else if (serverMessage.contains("unauthorized")) {
            if (authToken == null) {
                return "Wrong username or password.";
            }
            return "You're not allowed to do that. Try logging out and back in.";
        } else if (serverMessage.contains("already taken")) {
            if (authToken == null) {
                return "That username is already taken. Try a different one.";
            }
            return "That color is already taken in this game. Try the other color or a different game.";
        } else if (serverMessage.contains("bad request")) {
            return "Something about that input wasn't right. Type 'help' to check the format.";
        } else {
            return "Something went wrong. Please try again.";
        }
    }

}
