package server.Login;

import com.google.gson.Gson;
import server.Register.RegisterRequest;
import server.Register.RegisterResponse;
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

        LoginResponse loginResponse = (new UserService()).login(loginRequest);
        res.status(loginResponse.statusCode());
        if (loginResponse.auth() != null) {
            return gson.toJson(loginResponse.auth());
        } else {
            return "{ \"message\" : \"" + loginResponse.message() + "\" }";
        }
    }
}
