package service;

import dataAccess.*;
import model.GameData;
import server.CreateGame.CreateGameHandler;
import server.CreateGame.CreateGameRequest;
import server.CreateGame.CreateGameResponse;
import server.ListGame.ListGameHandler;
import server.ListGame.ListGameRequest;
import server.ListGame.ListGameResponse;

public class GameService {

    GameDAO gameDAO = new MemoryGameDAO();
    AuthDAO authDAO = new MemoryAuthDAO();
    public ListGameResponse listGames(ListGameRequest listGameRequest) throws UnauthorizedException{
        if (authDAO.getAuthFromToken(listGameRequest.auth()) == null) {
            throw new UnauthorizedException("Error: unauthorized");
        }
        return new ListGameResponse(gameDAO.listGames());
    }

    public CreateGameResponse createGame(CreateGameRequest createGameRequest) throws UnauthorizedException {
        if (authDAO.getAuthFromToken(createGameRequest.auth()) == null) {
            throw new UnauthorizedException("Error: unauthorized");
        }
        return new CreateGameResponse(gameDAO.createGame(createGameRequest.gameName()));
    }
}
