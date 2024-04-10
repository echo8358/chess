package service;

import dataAccess.*;
import dataAccess.Exceptions.BadRequestException;
import dataAccess.Exceptions.DataAccessException;
import dataAccess.Exceptions.ForbiddenException;
import dataAccess.Exceptions.UnauthorizedException;
import model.AuthData;
import model.GameData;
import server.http.CreateGame.CreateGameRequest;
import server.http.CreateGame.CreateGameResponse;
import server.http.JoinGame.JoinGameRequest;
import server.http.JoinGame.JoinGameResponse;
import server.http.ListGame.ListGameRequest;
import server.http.ListGame.ListGameResponse;

import java.util.Objects;

public class GameService {

    GameDAO gameDAO = new SQLGameDAO();
    AuthDAO authDAO = new SQLAuthDAO();
    public ListGameResponse listGames(ListGameRequest listGameRequest) throws UnauthorizedException, DataAccessException {
        if (authDAO.getAuthFromToken(listGameRequest.auth()) == null) {
            throw new UnauthorizedException("Error: unauthorized");
        }
        return new ListGameResponse(gameDAO.listGames());
    }

    public CreateGameResponse createGame(CreateGameRequest createGameRequest) throws UnauthorizedException, DataAccessException {
        if (authDAO.getAuthFromToken(createGameRequest.auth()) == null) {
            throw new UnauthorizedException("Error: unauthorized");
        }
        int id = gameDAO.createGame(createGameRequest.gameName());
        return new CreateGameResponse(id);
    }

    public JoinGameResponse joinGame(JoinGameRequest joinGameRequest) throws BadRequestException, UnauthorizedException, ForbiddenException, DataAccessException {
        AuthData userAuth = authDAO.getAuthFromToken(joinGameRequest.auth());
        if (userAuth == null) {
            throw new UnauthorizedException("Error: unauthorized");
        }
        GameData game = gameDAO.getGame(joinGameRequest.gameID());
        if (game == null) {
            throw new BadRequestException("Error: bad request");
        }

        if (Objects.equals(joinGameRequest.playerColor(), "WHITE")) {
            if (!Objects.equals(gameDAO.getGame(joinGameRequest.gameID()).whiteUsername(), null)
                    && !Objects.equals(gameDAO.getGame(joinGameRequest.gameID()).whiteUsername(), userAuth.username())) {
                throw new ForbiddenException("Error: forbidden");
            }
            gameDAO.setGameWhite(joinGameRequest.gameID(),userAuth.username());
        }
        else if (Objects.equals(joinGameRequest.playerColor(), "BLACK")) {
            if (!Objects.equals(gameDAO.getGame(joinGameRequest.gameID()).blackUsername(), null)
                    && !Objects.equals(gameDAO.getGame(joinGameRequest.gameID()).blackUsername(), userAuth.username())) {
                throw new ForbiddenException("Error: forbidden");
            }
            gameDAO.setGameBlack(joinGameRequest.gameID(),userAuth.username());
        }
        else {
            gameDAO.addGameWatcher(joinGameRequest.gameID(),userAuth.username());
        }

        return new JoinGameResponse();
    }

}
