package server.http.CreateGame;

public record CreateGameRequest (String gameName, String auth){
    public CreateGameRequest withAuth(String auth) {
        return new CreateGameRequest(gameName(), auth);
    }
}
