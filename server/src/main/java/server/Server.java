package server;

import com.google.gson.Gson;
import model.AuthData;
import model.UserData;
import service.DBService;
import service.LoginService;
import service.UserService;
import spark.*;

import static spark.Spark.*;

public class Server {

    public int run(int desiredPort) {
        Spark.port(desiredPort);

        Spark.staticFiles.location("web");

        delete("/db", (req,res) -> {
            res.status(200);
            (new DBService()).clearDB();
            return "";
        });

        // Register your endpoints and handle exceptions here
        post("/user", (req, res) -> {
            Gson gson = new Gson();
            UserData user = gson.fromJson(req.body(), UserData.class);

            //invalid requests
            if (user.username() == null || user.password() == null || user.email() == null) {
                res.status(400);
                return "{ \"message\" : \"Error: bad request\" }";
            }

            RegisterResponse registerResponse = (new UserService()).register(user);
            res.status(registerResponse.statusCode());
            if (registerResponse.auth() != null) {
                return gson.toJson(registerResponse.auth());
            } else {
                return "{ \"message\" : \"" + registerResponse.message() + "\" }";
            }
        });


        Spark.awaitInitialization();
        return Spark.port();
    }

    public void stop() {
        Spark.stop();
        Spark.awaitStop();
    }
}
