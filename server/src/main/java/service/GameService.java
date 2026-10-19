package service;
import dataaccess.AuthDAO;
import dataaccess.GameDAO;

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
}
