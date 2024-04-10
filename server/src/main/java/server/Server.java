package server;

import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import server.http.CreateGame.CreateGameHandler;
import server.http.JoinGame.JoinGameHandler;
import server.http.ListGame.ListGameHandler;
import server.http.Login.LoginHandler;
import server.http.Logout.LogoutHandler;
import server.http.Register.RegisterHandler;
import server.websocket.WebSocketHandler;
import service.DBService;
import spark.*;
import org.eclipse.jetty.websocket.api.annotations.*;
import org.eclipse.jetty.websocket.api.*;

import static spark.Spark.*;

public class Server {

    public int run(int desiredPort) {
        Spark.port(desiredPort);

        Spark.webSocket("/connect", WebSocketHandler.class);

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
        get("/game", (req, res) ->  (new ListGameHandler()).handle(req, res) );
        post("/game", (req, res) ->  (new CreateGameHandler()).handle(req, res) );
        put("/game", (req, res) ->  (new JoinGameHandler()).handle(req, res) );

        //WSServer wsServer = new WSServer();

        Spark.awaitInitialization();
        return Spark.port();
    }

    public void stop() {
        Spark.stop();
        Spark.awaitStop();
    }

}
