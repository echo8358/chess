package dataAccess;

import dataAccess.Exceptions.AlreadyTakenException;
import dataAccess.Exceptions.DataAccessException;
import model.AuthData;
import model.UserData;

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
        } catch (DataAccessException e) {
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
                insertAuthStatement.setString(1, newAuth.username());
                insertAuthStatement.setString(2, newAuth.authToken());
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
    public AuthData getAuthFromToken(String authToken) throws DataAccessException {
        for (AuthData auth: listAuth()) {
            if (Objects.equals(auth.authToken(), authToken)) {
                return auth;
            }
        }
        return null;
    }
    @Override
    public void deleteAuth(String authToken) throws DataAccessException {
        try {
            Connection conn = DatabaseManager.getConnection();

            String stmt = "DELETE FROM auth WHERE authToken = ?";

            try (var clearStatement = conn.prepareStatement(stmt)) {
                clearStatement.setString(1, authToken);
                clearStatement.executeUpdate();
            }
        } catch (SQLException e) {
            throw new DataAccessException("SQL exception clearing database");
        } catch (DataAccessException e) {
            throw new DataAccessException("SQL Connection error");
        }
    }

    public ArrayList<AuthData> listAuth() throws DataAccessException {
        ArrayList<AuthData> authList = new ArrayList<>();
        try {
            Connection conn = DatabaseManager.getConnection();
            var stmt = "SELECT * FROM auth;";
            try (var getUserStatement = conn.prepareStatement(stmt)) {
                try (ResultSet result = getUserStatement.executeQuery()) {
                    while (result.next()) {
                        String authToken = result.getString("authToken");
                        String username = result.getString("username");
                        authList.add(new AuthData(authToken, username));
                    }
                }
            }
        } catch (SQLException sqlException) {
            throw new DataAccessException("SQL exception getting user.");
        } catch (DataAccessException e) {
            throw new DataAccessException("SQL Connection error");
        }
        return authList;
    }

}
