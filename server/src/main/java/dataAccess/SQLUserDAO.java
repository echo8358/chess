package dataAccess;

import dataAccess.Exceptions.AlreadyTakenException;
import dataAccess.Exceptions.DataAccessException;
import model.UserData;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Objects;

public class SQLUserDAO implements UserDAO{

    public void clear() throws DataAccessException{
        try {
            Connection conn = DatabaseManager.getConnection();

            String stmt = "DELETE FROM user";

            try (var clearStatement = conn.prepareStatement(stmt)) {
               clearStatement.executeUpdate();
            }
        } catch (SQLException e) {
            throw new DataAccessException("SQL exception clearing database");
        } catch (DataAccessException e) {
            throw new DataAccessException("SQL Connection error" + e.getMessage());
        }

    }

    public void createUser(UserData user) throws DataAccessException {
        try {
            Connection conn = DatabaseManager.getConnection();

            String stmt = "SELECT username FROM user WHERE username=?;";

            try (var getUserStatement = conn.prepareStatement(stmt)) {
                getUserStatement.setString(1, user.username());
                try (ResultSet result = getUserStatement.executeQuery()) {
                    if (result.next()) {
                        if (Objects.equals(result.getString("username"), user.username())) {
                            throw new AlreadyTakenException("Username already taken.");
                        }
                    }
                }
            }

            stmt = "INSERT INTO user (username, email, password) VALUES (?, ?, ?);";
            try (var insertUserStatement = conn.prepareStatement(stmt)) {
                insertUserStatement.setString(1, user.username());
                insertUserStatement.setString(2, user.email());
                insertUserStatement.setString(3, user.password());

                insertUserStatement.executeUpdate();
            }
        } catch (AlreadyTakenException e) {
            throw e;
        } catch (SQLException sqlException) {
            throw new DataAccessException("SQL exception getting user.");
        } catch (DataAccessException e) {
            throw new DataAccessException("SQL Connection error");
        }
    }

    public UserData getUser(String username) throws DataAccessException{
        try {
            Connection conn = DatabaseManager.getConnection();
            var stmt = "SELECT * FROM user WHERE username = ?;";
            try (var getUserStatement = conn.prepareStatement(stmt)) {
                getUserStatement.setString(1, username);
                try (ResultSet result = getUserStatement.executeQuery()) {
                    if (result.next()) {
                        String newUsername = result.getString("username");
                        String password = result.getString("password");
                        String email = result.getString("email");
                        return new UserData(newUsername, password, email);
                    }
                }
            }
        } catch (SQLException sqlException) {
            throw new DataAccessException("SQL exception getting user." + sqlException.getMessage());
        } catch (DataAccessException e) {
            throw new DataAccessException("SQL Connection error" + e.getMessage());
        }
        return null;
    }

    public ArrayList<UserData> listUsers() throws DataAccessException{
        ArrayList<UserData> userList = new ArrayList<>();
        try {
            Connection conn = DatabaseManager.getConnection();
            var stmt = "SELECT * FROM user;";
            try (var getUserStatement = conn.prepareStatement(stmt)) {
                try (ResultSet result = getUserStatement.executeQuery()) {
                    while (result.next()) {
                        String newUsername = result.getString("username");
                        String password = result.getString("password");
                        String email = result.getString("email");
                        userList.add(new UserData(newUsername, password, email));
                    }
                }
            }
        } catch (SQLException sqlException) {
            throw new DataAccessException("SQL exception getting user.");
        } catch (DataAccessException e) {
            throw new DataAccessException("SQL Connection error");
        }
        return userList;

    }
}
