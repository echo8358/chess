package server.ListGame;

import com.google.gson.Gson;
import dataAccess.Exceptions.DataAccessException;
import dataAccess.Exceptions.UnauthorizedException;
import service.GameService;
import spark.Request;
import spark.Response;

public class ListGameHandler {
    public String handle(Request req, Response res) {
        Gson gson = new Gson();

        ListGameRequest listGameRequest = new ListGameRequest(req.headers("authorization"));

        if (listGameRequest.auth() == null) {
            res.status(400);
            return "{ \"message\" : \"Error: bad request\" }";
        }

        try {
            ListGameResponse listGameResponse = (new GameService()).listGames(listGameRequest);
            res.status(200);
            return gson.toJson(listGameResponse);
        } catch (UnauthorizedException e) {
            res.status(401);
            return "{ \"message\" : \"Error: unauthorized\" }";
        } catch (DataAccessException e) {
            res.status(500);
            return "{ \"message\" : \"Error: internal server error "+ e.getMessage()+"\" }";
        }

    }
}
