package server.Logout;

import dataAccess.Exceptions.UnauthorizedException;
import service.UserService;
import spark.Request;
import spark.Response;

public class LogoutHandler {
    public String handle(Request req, Response res){
        LogoutRequest logoutRequest = new LogoutRequest(req.headers("authorization"));

        //invalid requests
        if (logoutRequest.authToken() == null) {
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
