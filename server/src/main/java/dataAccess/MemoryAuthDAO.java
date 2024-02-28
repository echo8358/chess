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
    @Override
    public void deleteAuth(String authToken) {
        for (int i = 0; i < authList.size(); i++) {
            if (Objects.equals(authList.get(i).authToken(), authToken)) {
                authList.remove(i);
                return;
            }
        }
    }

    public ArrayList<AuthData> listAuth() {
        return new ArrayList<AuthData>(authList);
    }

}
