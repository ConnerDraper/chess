package client;

import chess.ChessMove;
import chess.ChessPosition;
import model.AuthData;
import model.GameData;
import org.junit.jupiter.api.*;
import server.Server;
import websocket.messages.ServerMessage;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ServerFacadeTests {

    private static Server server;
    private static ServerFacade facade;
    private static int port;

    @BeforeAll
    public static void init() {
        server = new Server();
        port = server.run(0);
        System.out.println("Started test HTTP server on " + port);
        facade = new ServerFacade(port);
    }

    @AfterAll
    static void stopServer() {
        server.stop();
    }

    @BeforeEach
    void clearDatabase() throws Exception {
        facade.clear();
    }

    /**
     * Helper function: registers account and returns authToken
     * @return
     * @throws Exception
     */
    private String registerHelper() throws Exception {
        AuthData auth = facade.register("testUsername", "testPassword", "testEmail@email.com");
        return auth.authToken();
    }

    /**
     * Helper function: creates a game, joins it as white, and returns the gameID
     * @return
     * @throws Exception
     */
    private int gameHelper(String authToken) throws Exception {
        int gameID = facade.createGame(authToken, "testGame");
        facade.joinGame(authToken, "WHITE", gameID);
        return gameID;
    }

    /**
     * Helper function: connects to a game, waits for the first board, then empties the list
     * @throws Exception
     */
    private void connectHelper(String authToken, int gameID, List<ServerMessage> received) throws Exception {
        facade.connect(authToken, gameID, msg -> received.add(msg));
        waitHelper();
        received.clear();
    }

    /**
     * Helper function: gives WebSocket messages time to arrive
     * @throws Exception
     */
    private void waitHelper() throws Exception {
        Thread.sleep(500);
    }

    @Test
    void registerPositive() throws Exception {
        AuthData auth = facade.register("testUsername", "testPassword", "testEmail@email.com");
        assertEquals("testUsername", auth.username());
        assertNotNull(auth.authToken());
    }

    @Test
    void registerNegative() throws Exception {
        registerHelper();
        // same username
        assertThrows(Exception.class, () -> facade.register("testUsername", "p", "e@email.com"));
        // missing password
        assertThrows(Exception.class, () -> facade.register("u", null, "e@email.com"));
    }

    @Test
    void loginPositive() throws Exception {
        registerHelper();
        AuthData auth = facade.login("testUsername", "testPassword");
        assertEquals("testUsername", auth.username());
        assertNotNull(auth.authToken());
    }

    @Test
    void loginNegative() throws Exception {
        registerHelper();
        assertThrows(Exception.class, () -> facade.login("testUsername", "wrongPassword"));
        assertThrows(Exception.class, () -> facade.login("nobody", "testPassword"));
    }

    @Test
    void logoutPositive() throws Exception {
        String authToken = registerHelper();
        assertDoesNotThrow(() -> facade.logout(authToken));
    }

    @Test
    void logoutNegative() throws Exception {
        String authToken = registerHelper();
        facade.logout(authToken);
        // already logged out
        assertThrows(Exception.class, () -> facade.logout(authToken));
    }

    @Test
    void createGamePositive() throws Exception {
        String authToken = registerHelper();
        int gameID = facade.createGame(authToken, "testGame");
        assertNotEquals(0, gameID);
    }

    @Test
    void createGameNegative() {
        assertThrows(Exception.class, () -> facade.createGame("fakeToken", "testGame"));
    }

    @Test
    void listGamesPositive() throws Exception {
        String authToken = registerHelper();
        facade.createGame(authToken, "testGame1");
        facade.createGame(authToken, "testGame2");
        List<GameData> games = facade.listGames(authToken);
        assertEquals(2, games.size());
    }

    @Test
    void listGamesNegative() {
        assertThrows(Exception.class, () -> facade.listGames("fakeToken"));
    }

    @Test
    void joinGamePositive() throws Exception {
        String authToken = registerHelper();
        int gameID = facade.createGame(authToken, "testGame");
        facade.joinGame(authToken, "WHITE", gameID);
        GameData game = facade.listGames(authToken).get(0);
        assertEquals("testUsername", game.whiteUsername());
    }

    @Test
    void joinGameNegative() throws Exception {
        String authToken = registerHelper();
        int gameID = facade.createGame(authToken, "testGame");
        facade.joinGame(authToken, "WHITE", gameID);
        AuthData otherUser = facade.register("otherUsername", "otherPassword", "other@email.com");
        assertThrows(Exception.class, () -> facade.joinGame(otherUser.authToken(), "WHITE", gameID));
    }

    @Test
    void clearPositive() throws Exception {
        registerHelper();
        facade.clear();
        assertThrows(Exception.class, () -> facade.login("testUsername", "testPassword"));
    }

    @Test
    void connectPositive() throws Exception {
        String authToken = registerHelper();
        int gameID = gameHelper(authToken);
        List<ServerMessage> received = new ArrayList<>();

        facade.connect(authToken, gameID, msg -> received.add(msg));
        waitHelper();

        assertEquals(1, received.size());
        assertEquals(ServerMessage.ServerMessageType.LOAD_GAME, received.get(0).getServerMessageType());
    }

    @Test
    void connectNegative() throws Exception {
        String authToken = registerHelper();
        List<ServerMessage> received = new ArrayList<>();

        // no game has this ID
        facade.connect(authToken, 999, msg -> received.add(msg));
        waitHelper();

        assertEquals(1, received.size());
        assertEquals(ServerMessage.ServerMessageType.ERROR, received.get(0).getServerMessageType());
    }

    @Test
    void makeMovePositive() throws Exception {
        String authToken = registerHelper();
        int gameID = gameHelper(authToken);
        List<ServerMessage> received = new ArrayList<>();
        connectHelper(authToken, gameID, received);

        // white pawn e2 to e4
        ChessMove move = new ChessMove(new ChessPosition(2, 5), new ChessPosition(4, 5), null);
        facade.makeMove(authToken, gameID, move);
        waitHelper();

        assertEquals(1, received.size());
        assertNotNull(received.get(0).getGame().getBoard().getPiece(new ChessPosition(4, 5)));
    }

    @Test
    void makeMoveNegative() throws Exception {
        String authToken = registerHelper();
        int gameID = gameHelper(authToken);
        List<ServerMessage> received = new ArrayList<>();
        connectHelper(authToken, gameID, received);

        // rook can't jump over its own pawn
        ChessMove move = new ChessMove(new ChessPosition(1, 1), new ChessPosition(5, 1), null);
        facade.makeMove(authToken, gameID, move);
        waitHelper();

        assertEquals(1, received.size());
        assertEquals(ServerMessage.ServerMessageType.ERROR, received.get(0).getServerMessageType());
    }

    @Test
    void resignPositive() throws Exception {
        String authToken = registerHelper();
        int gameID = gameHelper(authToken);
        List<ServerMessage> received = new ArrayList<>();
        connectHelper(authToken, gameID, received);

        facade.resign(authToken, gameID);
        waitHelper();

        assertEquals(1, received.size());
        assertEquals(ServerMessage.ServerMessageType.NOTIFICATION, received.get(0).getServerMessageType());
    }

    @Test
    void resignNegative() throws Exception {
        String authToken = registerHelper();
        int gameID = gameHelper(authToken);
        List<ServerMessage> received = new ArrayList<>();
        connectHelper(authToken, gameID, received);
        facade.resign(authToken, gameID);
        waitHelper();
        received.clear();

        // game is already over
        facade.resign(authToken, gameID);
        waitHelper();

        assertEquals(1, received.size());
        assertEquals(ServerMessage.ServerMessageType.ERROR, received.get(0).getServerMessageType());
    }

    @Test
    void leavePositive() throws Exception {
        String authToken = registerHelper();
        int gameID = gameHelper(authToken);
        List<ServerMessage> received = new ArrayList<>();
        connectHelper(authToken, gameID, received);

        facade.leave(authToken, gameID);

        // no connection left to send a move on
        ChessMove move = new ChessMove(new ChessPosition(2, 5), new ChessPosition(4, 5), null);
        assertThrows(Exception.class, () -> facade.makeMove(authToken, gameID, move));
    }

    @Test
    void leaveNegative() throws Exception {
        String authToken = registerHelper();
        // a facade that never connected to a game
        ServerFacade newFacade = new ServerFacade(port);

        assertThrows(Exception.class, () -> newFacade.leave(authToken, 1));
    }
}