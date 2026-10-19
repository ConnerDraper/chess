package dataaccess;

import java.util.Collection;

public interface GameDAO {
    void clear() throws DataAccessException;

    // Returns an int: gameID
    int createGame(String gameName) throws DataAccessException;

    model.GameData getGame(int gameID) throws DataAccessException;

    Collection<model.GameData> listGames() throws DataAccessException;

    void updateGame(model.GameData game) throws DataAccessException;
}
