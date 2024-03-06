package dataAccess;

import model.AuthData;
import dataAccess.Exceptions.DataAccessException;

import java.util.ArrayList;

public interface AuthDAO {
    void clear() throws DataAccessException;
    AuthData createAuth(String username) throws DataAccessException;
    AuthData getAuthFromToken(String authToken) throws DataAccessException;
    void deleteAuth(String authToken) throws DataAccessException;
    ArrayList<AuthData> listAuth() throws DataAccessException;
}
