package webSocketMessages.userCommands;

public class JoinObserver extends UserGameCommand {
    int gameID;
    public JoinObserver(String authToken, int gameID) {
        super(authToken);
        this.gameID = gameID;
        this.commandType = CommandType.JOIN_OBSERVER;
    }
}
