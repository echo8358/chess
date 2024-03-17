package ServerFacade;

import model.AuthData;
import model.GameData;

import java.util.ArrayList;

public class ServerFacade {

    public AuthData register(String username, String password, String email) {
        return null;
    }
    public void logout(String authToken) {
    }

    public AuthData login(String username, String password) {
        return null;
    }

    public ArrayList<GameData> listGames(String authToken) {
        return null;
    }

    public void joinGame(String playerColor, int gameID, String auth) {
    }

    public int createGame(String gameName, String authToken) {
        return 0;
    }

    public void clearDatabase() {

    }
}
