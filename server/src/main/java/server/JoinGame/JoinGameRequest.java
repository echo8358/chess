package server.JoinGame;

public record JoinGameRequest(String playerColor, int gameID, String auth) {
    public JoinGameRequest withAuth(String auth) {
        return new JoinGameRequest(playerColor(), gameID(), auth);
    }
}
