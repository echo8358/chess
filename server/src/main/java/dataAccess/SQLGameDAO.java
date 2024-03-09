package dataAccess;

import chess.ChessGame;
import dataAccess.Exceptions.AlreadyTakenException;
import dataAccess.Exceptions.DataAccessException;
import model.GameData;
import model.UserData;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Objects;

public class SQLGameDAO implements GameDAO{

    public void clear() throws DataAccessException{
        try {
            Connection conn = DatabaseManager.getConnection();

            String stmt = "DELETE FROM game;";

            try (var clearStatement = conn.prepareStatement(stmt)) {
               clearStatement.executeUpdate();
            }
        } catch (SQLException e) {
            throw new DataAccessException("SQL exception clearing database");
        } catch (DataAccessException e) {
            throw new DataAccessException("SQL Connection error" + e.getMessage());
        }

    }

    @Override
    public int createGame(String gameName) throws DataAccessException {
        try {
            Connection conn = DatabaseManager.getConnection();

            String stmt = "INSERT INTO game (whiteUsername, blackUsername, name, game) VALUES (?, ?, ?, ?);";
            try (var insertGameStatement = conn.prepareStatement(stmt, Statement.RETURN_GENERATED_KEYS)) {
                insertGameStatement.setString(1, null);
                insertGameStatement.setString(2,  null);
                insertGameStatement.setString(3, gameName);
                insertGameStatement.setString(4, "");

                if(insertGameStatement.executeUpdate() == 1) {
                    try(ResultSet generatedKeys = insertGameStatement.getGeneratedKeys()) {
                        generatedKeys.next();
                        return generatedKeys.getInt(1);
                    }
                } else {
                    throw new SQLException("Execute update failed");
                }
            }

        } catch (AlreadyTakenException e) {
            throw e;
        } catch (SQLException sqlException) {
            throw new DataAccessException("SQL exception " + sqlException.getMessage());
        } catch (DataAccessException e) {
            throw new DataAccessException("SQL Connection error "+e.getMessage());
        }
    }

    @Override
    public void updateGame(int gameID, String game) throws DataAccessException {
        try {
            Connection conn = DatabaseManager.getConnection();

            String stmt = "UPDATE game SET game = ? WHERE id = ?;";
            try (var updateGameStatement = conn.prepareStatement(stmt)) {
                updateGameStatement.setString(1, game);
                updateGameStatement.setInt(2,  gameID);

                updateGameStatement.executeUpdate();
            }

        } catch (AlreadyTakenException e) {
            throw e;
        } catch (SQLException sqlException) {
            throw new DataAccessException("SQL exception " + sqlException.getMessage());
        } catch (DataAccessException e) {
            throw new DataAccessException("SQL Connection error "+e.getMessage());
        }
    }

    @Override
    public GameData getGame(int gameID) throws DataAccessException {
        try {
            Connection conn = DatabaseManager.getConnection();
            var stmt = "SELECT * FROM game WHERE id = ?;";
            try (var getUserStatement = conn.prepareStatement(stmt)) {
                getUserStatement.setInt(1, gameID);
                try (ResultSet result = getUserStatement.executeQuery()) {
                    if (result.next()) {
                        String whiteUsername = result.getString("whiteUsername");
                        String blackUsername = result.getString("blackUsername");
                        String name = result.getString("name");
                        String game = result.getString("game");
                        return new GameData(gameID, whiteUsername, blackUsername, name, game);
                        //String stmt = "INSERT INTO game (whiteUsername, blackUsername, name, game) VALUES (?, ?, ?, ?);";
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

    @Override
    public ArrayList<GameData> listGames() throws DataAccessException {
        ArrayList<GameData> gameList = new ArrayList<>();
        try {
            Connection conn = DatabaseManager.getConnection();
            var stmt = "SELECT * FROM game;";
            try (var getGameStatement = conn.prepareStatement(stmt)) {
                try (ResultSet result = getGameStatement.executeQuery()) {
                    while (result.next()) {
                        int id = result.getInt("id");
                        String whiteUsername = result.getString("whiteUsername");
                        String blackUsername = result.getString("blackUsername");
                        String name = result.getString("name");
                        String game = result.getString("game");
                        gameList.add(new GameData(id, whiteUsername, blackUsername, name, game));
                    }
                }
            }
        } catch (SQLException sqlException) {
            throw new DataAccessException("SQL exception getting user.");
        } catch (DataAccessException e) {
            throw new DataAccessException("SQL Connection error");
        }
        return gameList;
    }

    @Override
    public void setGameWhite(int gameID, String username) throws DataAccessException {
        try {
            Connection conn = DatabaseManager.getConnection();
            String stmt = "UPDATE game SET whiteUsername = ? WHERE id = ?;";
            try (var updateGameStatement = conn.prepareStatement(stmt)) {
                updateGameStatement.setString(1, username);
                updateGameStatement.setInt(2,  gameID);

                updateGameStatement.executeUpdate();
            }
        } catch (SQLException sqlException) {
            throw new DataAccessException("SQL exception getting user.");
        } catch (DataAccessException e) {
            throw new DataAccessException("SQL Connection error");
        }
    }

    @Override
    public void setGameBlack(int gameID, String username) throws DataAccessException {
        try {
            Connection conn = DatabaseManager.getConnection();
            String stmt = "UPDATE game SET blackUsername = ? WHERE id = ?;";
            try (var updateGameStatement = conn.prepareStatement(stmt)) {
                updateGameStatement.setString(1, username);
                updateGameStatement.setInt(2,  gameID);

                updateGameStatement.executeUpdate();
            }
        } catch (SQLException sqlException) {
            throw new DataAccessException("SQL exception getting user.");
        } catch (DataAccessException e) {
            throw new DataAccessException("SQL Connection error");
        }
    }

    @Override
    public void addGameWatcher(int gameID, String username) {
        ;
    }
}
