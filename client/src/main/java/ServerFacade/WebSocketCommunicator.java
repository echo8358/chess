package ServerFacade;

import javax.websocket.*;
import java.net.URI;
import java.util.Scanner;

public class WebSocketCommunicator extends Endpoint {
    public static void main(String[] args) throws Exception {
        var ws = new WebSocketCommunicator();
        Scanner scanner = new Scanner(System.in);

        System.out.println("Enter a message you want to echo");
        while (true) ws.send(scanner.nextLine());
    }

    public Session session;

    public WebSocketCommunicator() throws Exception {
        URI uri = new URI("ws://localhost:3676/connect");
        WebSocketContainer container = ContainerProvider.getWebSocketContainer();
        this.session = container.connectToServer(this, uri);

        this.session.addMessageHandler(new MessageHandler.Whole<String>() {
            public void onMessage(String message) {
                System.out.println(message);
            }
        });
    }

    public void send(String msg) throws Exception {
        this.session.getBasicRemote().sendText(msg);
    }
    @Override
    public void onOpen(Session session, EndpointConfig endpointConfig) {

    }
}
