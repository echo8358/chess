package server.Logout;

import com.google.gson.Gson;
import dataAccess.UnauthorizedException;
import server.Login.LoginRequest;
import server.Login.LoginResponse;
import service.UserService;
import spark.Request;
import spark.Response;

public class LogoutHandler {
    public String handle(Request req, Response res){
        LogoutRequest logoutRequest = new LogoutRequest(req.headers("authorization"));

        //invalid requests
        if (logoutRequest.AuthToken() == null) {
            res.status(400);
            return "{ \"message\" : \"Error: bad request\" }";
        }

        try {
            LogoutResponse logoutResponse = (new UserService()).logout(logoutRequest);
            res.status(200);
            return "";
        } catch (UnauthorizedException e){
            res.status(401);
            return "{ \"message\" : \"Error: unauthorized\" }";
        }
    }
}
