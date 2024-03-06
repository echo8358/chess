package server.CreateGame;

import com.google.gson.Gson;
import dataAccess.Exceptions.DataAccessException;
import dataAccess.Exceptions.UnauthorizedException;
import service.GameService;
import spark.Request;
import spark.Response;

public class CreateGameHandler {
    public String handle(Request req, Response res) {
        Gson gson = new Gson();

        CreateGameRequest createGameRequest = gson.fromJson(req.body(), CreateGameRequest.class);
        createGameRequest = createGameRequest.withAuth(req.headers("authorization"));

        if (createGameRequest.auth() == null) {
            res.status(400);
            return "{ \"message\" : \"Error: bad request\" }";
        }

        try {
            CreateGameResponse createGameResponse = (new GameService()).createGame(createGameRequest);
            res.status(200);
            return gson.toJson(createGameResponse);
        } catch (UnauthorizedException e) {
            res.status(401);
            return "{ \"message\" : \"Error: unauthorized\" }";
        } catch (DataAccessException e) {
            res.status(500);
            return "{ \"message\" : \"Error: internal server error "+ e.getMessage()+"\" }";
        }

    }
}
