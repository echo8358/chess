package server.Register;

import com.google.gson.Gson;
import dataAccess.AlreadyTakenException;
import service.UserService;
import spark.Request;
import spark.Response;

public class RegisterHandler {
    public String handle(Request req, Response res){
        Gson gson = new Gson();
        RegisterRequest registerRequest = gson.fromJson(req.body(), RegisterRequest.class);

        //invalid requests
        if (registerRequest.username() == null || registerRequest.password() == null || registerRequest.email() == null) {
            res.status(400);
            return "{ \"message\" : \"Error: bad request\" }";
        }

        try {
            RegisterResponse registerResponse = (new UserService()).register(registerRequest);
            res.status(200);
            return gson.toJson(registerResponse.auth());
        } catch (AlreadyTakenException e) {
            res.status(403);
            return "{ \"message\" : \"Error: already taken\" }";
        }
    }
}
