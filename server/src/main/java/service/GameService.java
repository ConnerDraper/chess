package service;

import dataaccess.AuthDAO;
import dataaccess.DataAccessException;
import dataaccess.GameDAO;
import model.AuthData;
import model.GameData;
import service.exception.AlreadyTakenException;
import service.exception.BadRequestException;
import service.exception.UnauthorizedException;
import service.request.CreateGameRequest;
import service.request.JoinGameRequest;
import service.request.ListGamesRequest;
import service.result.CreateGameResult;
import service.result.JoinGameResult;
import service.result.ListGamesResult;

public class GameService {
    private final GameDAO gameDAO;
    private final AuthDAO authDAO;

    public GameService(
            GameDAO gameDAO,
            AuthDAO authDAO
    ) {
        this.gameDAO = gameDAO;
        this.authDAO = authDAO;
    }

    public ListGamesResult listGames(
            ListGamesRequest listGamesRequest
    ) throws DataAccessException, UnauthorizedException {
        // no auth
        AuthData authData = authDAO.getAuth(listGamesRequest.authToken());
        if (authData == null) {
            throw new UnauthorizedException("unauthorized");
        }

        return new ListGamesResult(gameDAO.listGames());
    }

    public CreateGameResult createGame(
            CreateGameRequest createGameRequest
    ) throws DataAccessException, UnauthorizedException, BadRequestException {
        // no auth
        AuthData authData = authDAO.getAuth(createGameRequest.authToken());
        if (authData == null) {
            throw new UnauthorizedException("unauthorized");
        }

        // no name
        String gameName = createGameRequest.gameName();
        if (gameName == null) {
            throw new BadRequestException("bad request");
        }

        int gameID = gameDAO.createGame(gameName);
        return new CreateGameResult(gameID);
    }

    public JoinGameResult joinGame(
            JoinGameRequest joinGameRequest
    ) throws DataAccessException, UnauthorizedException, BadRequestException, AlreadyTakenException {
        // no auth
        AuthData authData = authDAO.getAuth(joinGameRequest.authToken());
        if (authData == null) {
            throw new UnauthorizedException("unauthorized");
        }
        String username = authData.username();

        // game has to exist
        GameData game = gameDAO.getGame(joinGameRequest.gameID());
        if (game == null) {
            throw new BadRequestException("bad request");
        }

        // seat has to be open
        String playerColor = joinGameRequest.playerColor();
        if ("WHITE".equals(playerColor)) {
            if (game.whiteUsername() != null) {
                throw new AlreadyTakenException("already taken");
            }
            game = new GameData(game.gameID(), username, game.blackUsername(), game.gameName(), game.game());
        } else if ("BLACK".equals(playerColor)) {
            if (game.blackUsername() != null) {
                throw new AlreadyTakenException("already taken");
            }
            game = new GameData(game.gameID(), game.whiteUsername(), username, game.gameName(), game.game());
        } else {
            throw new BadRequestException("bad request");
        }

        gameDAO.updateGame(game);
        return new JoinGameResult();
    }
}
