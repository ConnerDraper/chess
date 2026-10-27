package dataaccess;

import dataaccess.mysql.MySqlGameDAO;
import model.GameData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MySqlGameDAOTest {
    private GameDAO gameDAO;

    @BeforeEach
    void setUp() throws Exception {
        gameDAO = new MySqlGameDAO();
        gameDAO.clear();
    }

    // clear removes all games
    @Test
    void clearPositive() throws Exception {
        gameDAO.createGame("gameName");
        gameDAO.clear();
        assertTrue(gameDAO.listGames().isEmpty());
    }

    // game is stored and can be read back by its ID
    @Test
    void createGamePositive() throws Exception {
        int gameID = gameDAO.createGame("gameName");
        assertEquals("gameName", gameDAO.getGame(gameID).gameName());
    }

    // missing game name is rejected by NOT NULL
    @Test
    void createGameNegative() {
        assertThrows(DataAccessException.class, () -> gameDAO.createGame(null));
    }

    // existing game is found with its chess game intact
    @Test
    void getGamePositive() throws Exception {
        int gameID = gameDAO.createGame("gameName");
        GameData gameData = gameDAO.getGame(gameID);
        assertEquals(gameID, gameData.gameID());
        assertNotNull(gameData.game());
    }

    // missing game returns null
    @Test
    void getGameNegative() throws Exception {
        assertNull(gameDAO.getGame(123));
    }

    // every game is listed
    @Test
    void listGamesPositive() throws Exception {
        gameDAO.createGame("gameOne");
        gameDAO.createGame("gameTwo");
        assertEquals(2, gameDAO.listGames().size());
    }

    // if no games, return an empty list
    @Test
    void listGamesNegative() throws Exception {
        assertTrue(gameDAO.listGames().isEmpty());
    }

    // updated player is saved
    @Test
    void updateGamePositive() throws Exception {
        int gameID = gameDAO.createGame("gameName");
        GameData gameData = gameDAO.getGame(gameID);
        gameDAO.updateGame(new GameData(gameID, "white", null, gameData.gameName(), gameData.game()));
        assertEquals("white", gameDAO.getGame(gameID).whiteUsername());
    }

    // updating a missing game does not create it
    @Test
    void updateGameNegative() throws Exception {
        GameData fakeGame = new GameData(999, "white", null, "gameName", new chess.ChessGame());
        gameDAO.updateGame(fakeGame);
        assertNull(gameDAO.getGame(999));
    }
}
