package server.Register;

import com.google.gson.Gson;
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

        RegisterResponse registerResponse = (new UserService()).register(registerRequest);
        res.status(registerResponse.statusCode());
        if (registerResponse.auth() != null) {
            return gson.toJson(registerResponse.auth());
        } else {
            return "{ \"message\" : \"" + registerResponse.message() + "\" }";
        }
    }
}
