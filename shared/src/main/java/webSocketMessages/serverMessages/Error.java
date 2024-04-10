package webSocketMessages.serverMessages;

public class Error extends  ServerMessage {
    String errorMessage;
    public Error(String errorMessage) {
        super(ServerMessageType.ERROR);
        this.serverMessageType = ServerMessageType.ERROR;
        this.errorMessage = errorMessage;
    }
}
