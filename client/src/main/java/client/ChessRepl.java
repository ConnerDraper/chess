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

    private ServerFacade facade;
    private Scanner scanner = new Scanner(System.in);
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
                    if (e.getMessage() == null) {
                        System.out.println("Can't reach the chess server right now. Make sure it's running.");
                    } else {
                        System.out.println("Something went wrong: " + e.getMessage());
                    }
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
    }

    private void register(String[] words) throws Exception {
    }

    private void login(String[] words) throws Exception {
    }

    private void logout() throws Exception {
    }

    private void createGame(String[] words) throws Exception {
    }

    private void listGames() throws Exception {
    }

    private void joinGame(String[] words) throws Exception {
    }

    private void observeGame(String[] words) {
    }

    private GameData pickGame(String number) {
        return null;
    }

    private String yourGameMarker(GameData game) {
        return "";
    }

    private void printHint(String hint) {
    }

    private void printBoardTitle(String gameName, String role) {
    }
}
