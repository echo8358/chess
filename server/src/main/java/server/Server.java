package server;

import server.Login.LoginHandler;
import server.Logout.LogoutHandler;
import server.Register.RegisterHandler;
import service.DBService;
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
        post("/user", (req, res) ->  (new RegisterHandler()).handle(req, res) );
        post("/session", (req, res) ->  (new LoginHandler()).handle(req, res) );
        delete("/session", (req, res) ->  (new LogoutHandler()).handle(req, res) );


        Spark.awaitInitialization();
        return Spark.port();
    }

    public void stop() {
        Spark.stop();
        Spark.awaitStop();
    }
}
