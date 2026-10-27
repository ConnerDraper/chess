package dataaccess.sql;

import chess.ChessGame;
import com.google.gson.Gson;
import dataaccess.DataAccessException;
import dataaccess.DatabaseManager;
import dataaccess.GameDAO;
import model.GameData;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;

public class SqlGameDAO extends SqlDAO implements GameDAO {
    // converts ChessGame objects to and from JSON text, since MySQL can't store Java objects
    private final Gson gson = new Gson();

    public SqlGameDAO() throws DataAccessException {
        // whiteUsername and blackUsername can be NULL until a player joins
        super("""
                CREATE TABLE IF NOT EXISTS games (
                    gameID INT NOT NULL AUTO_INCREMENT,
                    whiteUsername VARCHAR(255),
                    blackUsername VARCHAR(255),
                    gameName VARCHAR(255) NOT NULL,
                    game LONGTEXT NOT NULL,
                    PRIMARY KEY (gameID)
                )""");
    }

    @Override
    public void clear() throws DataAccessException {
        // TRUNCATE also resets AUTO_INCREMENT, so the next game gets ID 1 again
        runUpdate("TRUNCATE TABLE games");
    }

    @Override
    public int createGame(String gameName) throws DataAccessException {
        String statement = "INSERT INTO games (gameName, game) VALUES (?, ?)";
        String newGameJson = gson.toJson(new ChessGame());

        // RETURN_GENERATED_KEYS asks MySQL to tell us the gameID it picks
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(statement, Statement.RETURN_GENERATED_KEYS)) {
            preparedStatement.setString(1, gameName);
            preparedStatement.setString(2, newGameJson);
            preparedStatement.executeUpdate();

            ResultSet generatedKeys = preparedStatement.getGeneratedKeys();
            generatedKeys.next();
            return generatedKeys.getInt(1);
        } catch (SQLException exception) {
            throw new DataAccessException("Could not create game", exception);
        }
    }

    @Override
    public GameData getGame(int gameID) throws DataAccessException {
        String statement = "SELECT gameID, whiteUsername, blackUsername, gameName, game FROM games WHERE gameID = ?";
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(statement)) {
            preparedStatement.setInt(1, gameID);
            ResultSet results = preparedStatement.executeQuery();

            // no row means no game with that ID
            if (!results.next()) {
                return null;
            }
            return rowToGameData(results);
        } catch (SQLException exception) {
            throw new DataAccessException("Could not find game", exception);
        }
    }

    @Override
    public Collection<GameData> listGames() throws DataAccessException {
        String statement = "SELECT gameID, whiteUsername, blackUsername, gameName, game FROM games";
        Collection<GameData> games = new ArrayList<>();
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(statement)) {
            ResultSet results = preparedStatement.executeQuery();

            // move through the rows one at a time until there are none left
            while (results.next()) {
                games.add(rowToGameData(results));
            }
        } catch (SQLException exception) {
            throw new DataAccessException("Could not list games", exception);
        }
        return games;
    }

    @Override
    public void updateGame(GameData game) throws DataAccessException {
        String statement = "UPDATE games SET whiteUsername = ?, blackUsername = ?, gameName = ?, game = ? WHERE gameID = ?";
        String gameJson = gson.toJson(game.game());
        runUpdate(statement, game.whiteUsername(), game.blackUsername(), game.gameName(), gameJson, game.gameID());
    }

    // builds a GameData from the row the ResultSet is currently on
    private GameData rowToGameData(ResultSet results) throws SQLException {
        String gameJson = results.getString("game");
        ChessGame chessGame = gson.fromJson(gameJson, ChessGame.class);

        return new GameData(results.getInt("gameID"),
                results.getString("whiteUsername"),
                results.getString("blackUsername"),
                results.getString("gameName"),
                chessGame
        );
    }
}
