package client;

import model.AuthData;
import model.GameData;
import org.junit.jupiter.api.*;
import server.Server;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ServerFacadeTests {

    private static Server server;
    private static ServerFacade facade;

    @BeforeAll
    public static void init() {
        server = new Server();
        int port = server.run(0);
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
}
