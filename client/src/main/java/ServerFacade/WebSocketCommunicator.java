package ServerFacade;

import chess.ChessGame;
import com.google.gson.Gson;
import webSocketMessages.userCommands.JoinPlayer;

import javax.websocket.*;
import java.net.URI;
import java.util.Scanner;

public class WebSocketCommunicator extends Endpoint {

    public Session session;
    private Gson gson = new Gson();

    public WebSocketCommunicator(String remote) throws Exception {
        URI uri = new URI("ws://"+remote+"/connect");
        WebSocketContainer container = ContainerProvider.getWebSocketContainer();
        this.session = container.connectToServer(this, uri);

        this.session.addMessageHandler(new MessageHandler.Whole<String>() {
            public void onMessage(String message) {
                System.out.println(message);
            }
        });
    }

    public void joinPlayer(String authToken, int gameID, ChessGame.TeamColor playerColor) {
        try {
            send(gson.toJson(new JoinPlayer(authToken, gameID, playerColor)));
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
}
