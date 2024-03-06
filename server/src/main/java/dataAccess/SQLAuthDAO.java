package dataAccess;

import dataAccess.Exceptions.AlreadyTakenException;
import model.AuthData;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Objects;
import java.util.UUID;

public class SQLAuthDAO implements AuthDAO{
    @Override
    public void clear() throws DataAccessException {
        try {
            Connection conn = DatabaseManager.getConnection();

            String stmt = "DELETE FROM auth";

            try (var clearStatement = conn.prepareStatement(stmt)) {
                clearStatement.executeUpdate();
            }
        } catch (SQLException e) {
            throw new DataAccessException("SQL exception clearing database");
        } catch (dataAccess.DataAccessException e) {
            throw new DataAccessException("SQL Connection error");
        }
    }
    @Override
    public AuthData createAuth(String username) throws DataAccessException {
        AuthData newAuth = new AuthData(UUID.randomUUID().toString(),username);
        try {
            Connection conn = DatabaseManager.getConnection();

            String stmt = "INSERT INTO auth(username, authToken) VALUES (?, ?);";
            try (var insertAuthStatement = conn.prepareStatement(stmt)) {
                insertAuthStatement.setString(1, username);
                insertAuthStatement.setString(2, UUID.randomUUID().toString());
                insertAuthStatement.executeUpdate();
            }
        } catch (SQLException sqlException) {
            throw new DataAccessException("SQL exception getting user.");
        } catch (DataAccessException e) {
            throw new DataAccessException("SQL Connection error");
        }
        return newAuth;
    }

    @Override
    public AuthData getAuthFromToken(String authToken) {
        for (AuthData auth: listAuth()) {
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
