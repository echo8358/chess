package server.websocket;

import chess.ChessGame;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import dataAccess.AuthDAO;
import dataAccess.Exceptions.DataAccessException;
import dataAccess.SQLAuthDAO;
import model.AuthData;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import webSocketMessages.serverMessages.LoadGame;
import webSocketMessages.serverMessages.Notification;
import webSocketMessages.serverMessages.ServerMessage;
import webSocketMessages.userCommands.*;

import java.io.IOException;
import java.util.Timer;


@WebSocket
public class WebSocketHandler {

    private final ConnectionManager connections = new ConnectionManager();
    private final Gson gson;
    private final AuthDAO authDAO = new SQLAuthDAO();

    public WebSocketHandler() {
        gson = createSerializer();
    }

    @OnWebSocketMessage
    public void onMessage(Session session, String message) throws IOException {
        //do type helper stuff
        System.out.println(message);
        UserGameCommand command = gson.fromJson(message, UserGameCommand.class);
        System.out.println(command.getCommandType());
        switch (command.getCommandType()) {
            case JOIN_OBSERVER -> joinObserver((JoinObserver) command, session);
            case JOIN_PLAYER -> joinPlayer((JoinPlayer) command, session);
            case LEAVE -> leave((Leave) command, session);
            case MAKE_MOVE -> makeMove((MakeMove) command, session);
            case RESIGN -> resign((Resign) command, session);
            //case RESIGN -> resign(action.visitorName(), session);
        }

    }

    private void joinObserver(JoinObserver command, Session session) throws IOException {
        connections.add("username",session, 1);
        connections.broadcast(1, "", new Notification(command.toString()));
    }
    private void joinPlayer(JoinPlayer command, Session session) throws IOException {
        AuthData auth;
        try {
            auth = authDAO.getAuthFromToken(command.getAuthString());
        } catch (DataAccessException e) {
            throw new RuntimeException(e);
        }
        Connection rootConnection = connections.add(auth.username(), session, command.getGameID());
        connections.broadcast(command.getGameID(), auth.username(), (new Notification("Player "+auth.username()+" joined as "+command.getPlayerColor())));
        rootConnection.send(gson.toJson(new LoadGame(new ChessGame())));
    }
    private void leave(Leave command, Session session) {}
    private void makeMove(MakeMove command, Session session) {}
    private void resign(Resign command, Session session) {}
    /*
    private void enter(String visitorName, Session session) throws IOException {
        connections.add(visitorName, session);
        var message = String.format("%s is in the shop", visitorName);
        var notification = new Notification(Notification.Type.ARRIVAL, message);
        connections.broadcast(visitorName, notification);
    }

    private void exit(String visitorName) throws IOException {
        connections.remove(visitorName);
        var message = String.format("%s left the shop", visitorName);
        var notification = new Notification(Notification.Type.DEPARTURE, message);
        connections.broadcast(visitorName, notification);
    }

    public void makeNoise(String petName, String sound) throws ResponseException {
        try {
            var message = String.format("%s says %s", petName, sound);
            var notification = new Notification(Notification.Type.NOISE, message);
            connections.broadcast("", notification);
        } catch (Exception ex) {
            throw new ResponseException(500, ex.getMessage());
        }
    }

     */
    public static Gson createSerializer() {
        GsonBuilder gsonBuilder = new GsonBuilder();

        gsonBuilder.registerTypeAdapter(UserGameCommand.class,
                (JsonDeserializer<UserGameCommand>) (el, type, ctx) -> {
                    UserGameCommand command = null;
                    if (el.isJsonObject()) {
                        String commandType = el.getAsJsonObject().get("commandType").getAsString();
                        switch (UserGameCommand.CommandType.valueOf(commandType)) {
                            case JOIN_OBSERVER -> command = ctx.deserialize(el, JoinObserver.class);
                            case JOIN_PLAYER -> command = ctx.deserialize(el, JoinPlayer.class);
                            case LEAVE -> command = ctx.deserialize(el, Leave.class);
                            case MAKE_MOVE -> command = ctx.deserialize(el, MakeMove.class);
                            case RESIGN -> command = ctx.deserialize(el, Resign.class);
                        }
                    }
                    return command;
                });

        return gsonBuilder.create();
    }
}