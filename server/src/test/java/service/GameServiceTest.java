package service;

import dataaccess.GameDAO;
import dataaccess.MemoryGameDAO;
import dataaccess.AuthDAO;
import dataaccess.MemoryAuthDAO;
import dataaccess.UserDAO;
import dataaccess.MemoryUserDAO;

import model.GameData;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GameServiceTest {
    private GameDAO gameDAO;
    private GameService gameService;
    private String authToken;

    @BeforeEach
    void setUp() throws Exception {
        AuthDAO authDAO = new MemoryAuthDAO();
        gameDAO = new MemoryGameDAO();
        UserDAO userDAO = new MemoryUserDAO();

        gameService = new GameService(gameDAO, authDAO);
        UserService userService = new UserService(userDAO, authDAO);
        RegisterRequest registerRequest = new RegisterRequest(
                "username", "password", "email@email.com"
        );
        authToken = userService.register(registerRequest).authToken();
    }

    @Test
    void listGamesSuccess() throws Exception {
        CreateGameRequest createGameRequest = new CreateGameRequest("game67", authToken);
        gameService.createGame(createGameRequest);

        ListGamesRequest listGamesRequest = new ListGamesRequest(authToken);
        ListGamesResult listGamesResult = gameService.listGames(listGamesRequest);

        assertEquals(1, listGamesResult.games().size());
    }

    @Test
    void listGamesBadToken() {
        // token was never issued
        ListGamesRequest listGamesRequest = new ListGamesRequest("badToken");

        assertThrows(UnauthorizedException.class, () -> gameService.listGames(listGamesRequest));
    }

    @Test
    void createGameSuccess() throws Exception {
        CreateGameRequest createGameRequest = new CreateGameRequest("game67", authToken);
        CreateGameResult createGameResult = gameService.createGame(createGameRequest);

        // game is actually stored
        GameData gameData = gameDAO.getGame(createGameResult.gameID());
        assertNotNull(gameData);
        assertEquals("game67", gameData.gameName());
    }

    @Test
    void createGameNoName() {
        CreateGameRequest createGameRequest = new CreateGameRequest(null, authToken);

        assertThrows(BadRequestException.class, () -> gameService.createGame(createGameRequest));
    }

    @Test
    void joinGameSuccess() throws Exception {
        CreateGameRequest createGameRequest = new CreateGameRequest("game67", authToken);
        int gameID = gameService.createGame(createGameRequest).gameID();

        JoinGameRequest joinGameRequest = new JoinGameRequest(gameID, "WHITE", authToken);
        gameService.joinGame(joinGameRequest);

        // username is now in the white seat
        GameData gameData = gameDAO.getGame(gameID);
        assertEquals("username", gameData.whiteUsername());
    }

    @Test
    void joinGameBothColors() throws Exception {
        CreateGameRequest createGameRequest = new CreateGameRequest("game67", authToken);
        int gameID = gameService.createGame(createGameRequest).gameID();

        JoinGameRequest joinWhiteRequest = new JoinGameRequest(gameID, "WHITE", authToken);
        JoinGameRequest joinBlackRequest = new JoinGameRequest(gameID, "BLACK", authToken);
        gameService.joinGame(joinWhiteRequest);
        gameService.joinGame(joinBlackRequest);

        // both seats filled
        GameData gameData = gameDAO.getGame(gameID);
        assertAll(
                () -> assertEquals("username", gameData.whiteUsername()),
                () -> assertEquals("username", gameData.blackUsername())
        );
    }

    @Test
    void joinGameColorTaken() throws Exception {
        CreateGameRequest createGameRequest = new CreateGameRequest("game67", authToken);
        int gameID = gameService.createGame(createGameRequest).gameID();

        JoinGameRequest joinGameRequest = new JoinGameRequest(gameID, "WHITE", authToken);
        gameService.joinGame(joinGameRequest);

        // same seat twice
        assertThrows(AlreadyTakenException.class, () -> gameService.joinGame(joinGameRequest));
    }

    @Test
    void joinGameNoGame() {
        // game 6767 was never created
        JoinGameRequest joinGameRequest = new JoinGameRequest(6767, "WHITE", authToken);
        assertThrows(BadRequestException.class, () -> gameService.joinGame(joinGameRequest));
    }

    @Test
    void joinGameInvalidColor() throws Exception {
        CreateGameRequest createGameRequest = new CreateGameRequest("game1", authToken);
        int gameID = gameService.createGame(createGameRequest).gameID();

        // none of these are WHITE or BLACK
        String[] badColors = {null, "", "INVALID_COLOR", "white", "GREEN"};
        for (String playerColor : badColors) {
            JoinGameRequest joinGameRequest = new JoinGameRequest(gameID, playerColor, authToken);
            assertThrows(BadRequestException.class, () -> gameService.joinGame(joinGameRequest));
        }
    }
}
