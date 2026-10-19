package dataaccess;

import chess.ChessGame;
import model.GameData;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;

public class MemoryGameDAO implements GameDAO {
    private final HashMap<Integer, model.GameData> allGames = new HashMap<>();
    private int currentID = 1;

    @Override
    public void clear() throws DataAccessException {
        allGames.clear();
        currentID = 1;
    }

    @Override
    public int createGame(String gameName) {
        int newID = currentID;
        currentID++;

        chess.ChessGame newGame = new ChessGame();
        GameData newGameData = new GameData(newID, null, null, gameName, newGame);

        allGames.put(newID, newGameData);
        return newID;
    }

    @Override
    public GameData getGame(int gameID) {
        return allGames.get(gameID);
    }

    @Override
    public Collection<GameData> listGames() {
        return List.copyOf(allGames.values());
    }

    @Override
    public void updateGame(GameData game) {
        allGames.put(game.gameID(), game);
    }
}
