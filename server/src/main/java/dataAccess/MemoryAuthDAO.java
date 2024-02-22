package dataAccess;

import model.AuthData;

import java.util.ArrayList;
import java.util.Objects;
import java.util.UUID;

public class MemoryAuthDAO implements AuthDAO{
    static ArrayList<AuthData> authList = new ArrayList<AuthData>();
    @Override
    public void clear() { authList.clear(); }
    @Override
    public AuthData createAuth(String username) {
        AuthData newAuth = new AuthData(UUID.randomUUID().toString(),username);
        authList.add(newAuth);
        return newAuth;
    }

    @Override
    public AuthData getAuthFromToken(String authToken) {
        for (AuthData auth: authList) {
            if (Objects.equals(auth.authToken(), authToken)) {
                return auth;
            }
        }
        return null;
    }
    public AuthData getAuthFromUsername(String username) {
        for (AuthData auth : authList) {
            if (Objects.equals(auth.username(), username)) {
                return auth;
            }
        }
        return null;
    }

}
