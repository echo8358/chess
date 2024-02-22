package dataAccess;

import model.AuthData;

public interface AuthDAO {
    AuthData createAuth(String username) throws DataAccessException;

    AuthData getAuthFromToken(String authToken);
    AuthData getAuthFromUsername(String username);
}
