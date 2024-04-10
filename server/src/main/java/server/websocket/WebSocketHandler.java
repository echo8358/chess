package server.websocket;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import webSocketMessages.serverMessages.Notification;
import webSocketMessages.serverMessages.ServerMessage;
import webSocketMessages.userCommands.*;

import java.io.IOException;
import java.util.Timer;


@WebSocket
public class WebSocketHandler {

    private final ConnectionManager connections = new ConnectionManager();
    private final Gson gson;

    public WebSocketHandler() {
        gson = createSerializer();
    }

    @OnWebSocketMessage
    public void onMessage(Session session, String message) throws IOException {
        //do type helper stuff
        System.out.println(message);
        UserGameCommand command = gson.fromJson(message, UserGameCommand.class);
        System.out.println(command.getCommandType());
        connections.add("username",session, 1);
        connections.broadcast(1, "", new Notification(message));
        UserGameCommand action = new Gson().fromJson(message, UserGameCommand.class);
        switch (action.getCommandType()) {
            case JOIN_OBSERVER -> joinObserver((JoinObserver) command, session);
            case JOIN_PLAYER -> joinPlayer();
            case LEAVE -> leave();
            case MAKE_MOVE -> makeMove();
            case RESIGN -> resign();
            //case RESIGN -> resign(action.visitorName(), session);
        }

    }

    private void joinObserver(JoinObserver command, Session session) {}
    private void joinPlayer() {}
    private void leave() {}
    private void makeMove() {}
    private void resign() {}
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