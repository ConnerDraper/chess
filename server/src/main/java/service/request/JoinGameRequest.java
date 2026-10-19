package service.request;

public record JoinGameRequest(
        int gameID,
        String playerColor,
        String authToken
) {
}
