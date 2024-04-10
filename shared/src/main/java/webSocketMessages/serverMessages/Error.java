package webSocketMessages.serverMessages;

public class Error extends  ServerMessage {
    String errorMessage;
    public Error(ServerMessageType type, String errorMessage) {
        super(type);
        this.serverMessageType = ServerMessageType.ERROR;
        this.errorMessage = errorMessage;
    }
}
