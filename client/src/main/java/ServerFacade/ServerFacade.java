package ServerFacade;

import chess.ChessGame;
import chess.ChessMove;
import model.AuthData;
import responses.ListGameResponse;

public class ServerFacade {
    HttpCommunicator httpCommunicator;
    WebSocketCommunicator webSocketCommunicator;
    String serverUrl;
    public ServerFacade(String remote) {
        serverUrl = remote;
        try {
            webSocketCommunicator = new WebSocketCommunicator(remote);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        httpCommunicator = new HttpCommunicator(remote);

    }

    public AuthData register(String username, String password, String email) throws ResponseException {
        return httpCommunicator.register(username, password, email);
    }
    public AuthData login(String username, String password) throws ResponseException {
        return httpCommunicator.login(username,password);
    }
    public void logout(String authToken) throws ResponseException {
        httpCommunicator.logout(authToken);
    }
    public ListGameResponse listGames(String authToken) throws ResponseException {
        return httpCommunicator.listGames(authToken);
    }

    public void joinGame(String playerColor, int gameID, String authToken) throws ResponseException {
        httpCommunicator.joinGame(playerColor, gameID, authToken);
    }

    public int createGame(String gameName, String authToken) throws ResponseException {
        return httpCommunicator.createGame(gameName, authToken);
    }

    public void clearDatabase() throws ResponseException {
        httpCommunicator.clearDatabase();
    }

    public void joinPlayer(String authToken, int gameID, ChessGame.TeamColor playerColor) {
        webSocketCommunicator.joinPlayer(authToken, gameID, playerColor);
    }
    public void joinObserver(String authToken, int gameID) {
        webSocketCommunicator.joinObserver(authToken, gameID);
    }
    public void leave(String authToken, int gameID) {
        webSocketCommunicator.leave(authToken, gameID);
    }
    public void makeMove(String authToken, int gameID, ChessMove move) {
        webSocketCommunicator.makeMove(authToken, gameID, move);
    }
    public void resign(String authToken, int gameID) {
        webSocketCommunicator.resign(authToken, gameID);
    }

}
