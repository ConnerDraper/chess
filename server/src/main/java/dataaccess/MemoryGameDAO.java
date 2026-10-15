package dataaccess;

import chess.ChessGame;
import model.GameData;

import java.util.Collection;
import java.util.List;

import java.util.HashMap;

public class MemoryGameDAO implements GameDAO{
    private HashMap<Integer, model.GameData> allGames = new HashMap<>();
    private int currentID = 0;

    @Override
    public void clear() throws DataAccessException {
        allGames.clear();
        currentID = 0;
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
