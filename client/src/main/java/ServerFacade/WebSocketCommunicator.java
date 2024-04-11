package ServerFacade;

import chess.ChessGame;
import chess.ChessMove;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import ui.MainMenu;
import ui.UIUtils;
import webSocketMessages.serverMessages.Error;
import webSocketMessages.serverMessages.LoadGame;
import webSocketMessages.serverMessages.Notification;
import webSocketMessages.serverMessages.ServerMessage;
import webSocketMessages.userCommands.*;

import javax.websocket.*;
import java.net.URI;
import java.util.Scanner;

public class WebSocketCommunicator extends Endpoint {

    public Session session;
    private final Gson gson = createSerializer();

    public WebSocketCommunicator(String remote) throws Exception {
        URI uri = new URI("ws://"+remote+"/connect");
        WebSocketContainer container = ContainerProvider.getWebSocketContainer();
        this.session = container.connectToServer(this, uri);

        this.session.addMessageHandler(new MessageHandler.Whole<String>() {
            public void onMessage(String message) {
                ServerMessage serverMessage = gson.fromJson(message, ServerMessage.class);
                switch (serverMessage.getServerMessageType()) {
                    case LOAD_GAME -> loadGame((LoadGame) serverMessage);
                    case ERROR -> error((Error) serverMessage);
                    case NOTIFICATION -> notification((Notification) serverMessage);
                }

            }
        });
    }

    private void loadGame(LoadGame message) {
        MainMenu.loadGame(message.getGame());
    }

    private void error(Error message) {
        System.err.println(message.getErrorMessage());
    }

    private void notification(Notification message) {
        System.out.println(message.getMessage());
    }

    public void joinPlayer(String authToken, int gameID, ChessGame.TeamColor playerColor) {
        try {
            send(gson.toJson(new JoinPlayer(authToken, gameID, playerColor)));
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
    public void joinObserver(String authToken, int gameID) {
        try {
            send(gson.toJson(new JoinObserver(authToken, gameID)));
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    public void leave(String authToken, int gameID) {
        try {
            send(gson.toJson(new Leave(authToken, gameID)));
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    public void makeMove(String authToken, int gameID, ChessMove move) {
        try {
            send(gson.toJson(new MakeMove(authToken, gameID, move)));
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
    public void resign(String authToken, int gameID) {
        try {
            send(gson.toJson(new Resign(authToken, gameID)));
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    public void send(String msg) throws Exception {
        this.session.getBasicRemote().sendText(msg);
    }
    @Override
    public void onOpen(Session session, EndpointConfig endpointConfig) {

    }

    public static Gson createSerializer() {
        GsonBuilder gsonBuilder = new GsonBuilder();

        gsonBuilder.registerTypeAdapter(ServerMessage.class,
                (JsonDeserializer<ServerMessage>) (el, type, ctx) -> {
                    ServerMessage command = null;
                    if (el.isJsonObject()) {
                        String commandType = el.getAsJsonObject().get("serverMessageType").getAsString();
                        switch (ServerMessage.ServerMessageType.valueOf(commandType)) {
                            case LOAD_GAME -> command = ctx.deserialize(el, LoadGame.class);
                            case ERROR -> command = ctx.deserialize(el, Error.class);
                            case NOTIFICATION -> command = ctx.deserialize(el, Notification.class);
                        }
                    }
                    return command;
                });

        return gsonBuilder.create();
    }
}
