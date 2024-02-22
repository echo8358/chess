package dataAccess;

import model.AuthData;

public interface AuthDAO {
    void clear();
    AuthData createAuth(String username);

    AuthData getAuthFromToken(String authToken);
    AuthData getAuthFromUsername(String username);
}
