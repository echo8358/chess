package dataAccess;

import model.AuthData;

import java.util.ArrayList;

public interface AuthDAO {
    void clear() throws DataAccessException;
    AuthData createAuth(String username) throws DataAccessException;
    AuthData getAuthFromToken(String authToken);
    void deleteAuth(String authToken);
    ArrayList<AuthData> listAuth();
}
