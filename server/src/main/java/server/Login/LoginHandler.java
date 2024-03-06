package server.Login;

import com.google.gson.Gson;
import dataAccess.Exceptions.DataAccessException;
import dataAccess.Exceptions.UnauthorizedException;
import service.UserService;
import spark.Request;
import spark.Response;

public class LoginHandler {
    public String handle(Request req, Response res){
        Gson gson = new Gson();
        LoginRequest loginRequest = gson.fromJson(req.body(), LoginRequest.class);

        //invalid requests
        if (loginRequest.username() == null || loginRequest.password() == null) {
            res.status(400);
            return "{ \"message\" : \"Error: bad request\" }";
        }

        try {
            LoginResponse loginResponse = (new UserService()).login(loginRequest);
            res.status(200);
            return gson.toJson(loginResponse.auth());
        } catch (UnauthorizedException e) {
            res.status(401);
            return "{ \"message\" : \"Error: unauthorized\" }";
        } catch (DataAccessException e) {
            res.status(500);
            return "{ \"message\" : \"Error: internal server error " + e.getMessage() + "\" }";
        }
    }
}
