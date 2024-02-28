package dataAccess;

import model.AuthData;

import java.util.ArrayList;

public interface AuthDAO {
    void clear();
    AuthData createAuth(String username);
    AuthData getAuthFromToken(String authToken);
    void deleteAuth(String authToken);
    ArrayList<AuthData> listAuth();
}
