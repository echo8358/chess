package server.http.JoinGame;

import com.google.gson.Gson;
import dataAccess.Exceptions.BadRequestException;
import dataAccess.Exceptions.DataAccessException;
import dataAccess.Exceptions.ForbiddenException;
import dataAccess.Exceptions.UnauthorizedException;
import service.GameService;
import spark.Request;
import spark.Response;

public class JoinGameHandler {
    public String handle(Request req, Response res) {
        Gson gson = new Gson();

        JoinGameRequest joinGameRequest = gson.fromJson(req.body(), JoinGameRequest.class);
        joinGameRequest = joinGameRequest.withAuth(req.headers("authorization"));

        if (joinGameRequest.auth() == null) {
            res.status(400);
            return "{ \"message\" : \"Error: bad request\" }";
        }

        try {
            JoinGameResponse joinGameResponse = (new GameService()).joinGame(joinGameRequest);
            res.status(200);
            return gson.toJson(joinGameResponse);
        } catch ( BadRequestException e) {
            res.status(400);
            return "{ \"message\" : \"Error: bad request\" }";
        } catch ( UnauthorizedException e) {
            res.status(401);
            return "{ \"message\" : \"Error: unauthorized\" }";
        } catch ( ForbiddenException e) {
            res.status(403);
            return "{ \"message\" : \"Error: forbidden\" }";
        } catch (DataAccessException e) {
            res.status(500);
            return "{ \"message\" : \"Error: internal server error "+ e.getMessage()+"\" }";
        }
    }
}
