package server;

import com.google.gson.Gson;
import io.javalin.http.Context;
import service.DataService;
import service.GameService;
import service.UserService;
import service.exception.AlreadyTakenException;
import service.exception.BadRequestException;
import service.exception.UnauthorizedException;
import service.request.*;

import java.util.Map;
import java.util.concurrent.Callable;

public class Handler {
    private final Gson gson = new Gson();
    private final DataService dataService;
    private final UserService userService;
    private final GameService gameService;

    public Handler(
            DataService dataService,
            UserService userService,
            GameService gameService
    ) {
        this.dataService = dataService;
        this.userService = userService;
        this.gameService = gameService;
    }

    public void handleClear(Context context) {
        respond(context, () -> {
            dataService.clear();
            return Map.of();
        });
    }

    public void handleRegister(Context context) {
        respond(context, () -> {
            RegisterRequest registerRequest = gson.fromJson(context.body(), RegisterRequest.class);
            return userService.register(registerRequest);
        });
    }

    public void handleLogin(Context context) {
        respond(context, () -> {
            LoginRequest loginRequest = gson.fromJson(context.body(), LoginRequest.class);
            return userService.login(loginRequest);
        });
    }

    public void handleLogout(Context context) {
        respond(context, () -> {
            LogoutRequest logoutRequest = new LogoutRequest(context.header("authorization"));
            return userService.logout(logoutRequest);
        });
    }

    public void handleListGames(Context context) {
        respond(context, () -> {
            ListGamesRequest listGamesRequest = new ListGamesRequest(context.header("authorization"));
            return gameService.listGames(listGamesRequest);
        });
    }

    public void handleCreateGame(Context context) {
        respond(context, () -> {
            CreateGameRequest body = gson.fromJson(context.body(), CreateGameRequest.class);
            CreateGameRequest createGameRequest = new CreateGameRequest(body.gameName(), context.header("authorization"));
            return gameService.createGame(createGameRequest);
        });
    }

    public void handleJoinGame(Context context) {
        respond(context, () -> {
            JoinGameRequest body = gson.fromJson(context.body(), JoinGameRequest.class);
            JoinGameRequest joinGameRequest = new JoinGameRequest(body.gameID(), body.playerColor(), context.header("authorization"));
            return gameService.joinGame(joinGameRequest);
        });
    }

    // run the service call, then send its result or its error
    private void respond(
            Context context,
            Callable<Object> serviceCall
    ) {
        try {
            context.result(gson.toJson(serviceCall.call()));
        } catch (Exception error) {
            sendError(context, error);
        }
    }

    // service exception -> status code
    private void sendError(
            Context context,
            Exception error
    ) {
        int status = switch (error) {
            case BadRequestException badRequest -> 400;
            case UnauthorizedException unauthorized -> 401;
            case AlreadyTakenException alreadyTaken -> 403;
            default -> 500;
        };

        String message = "Error: " + error.getMessage();
        context.status(status).result(gson.toJson(Map.of("message", message)));
    }
}
