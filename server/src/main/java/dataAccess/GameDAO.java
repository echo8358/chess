package dataAccess;

import chess.ChessGame;
import dataAccess.Exceptions.AlreadyTakenException;
import dataAccess.Exceptions.DataAccessException;
import model.GameData;

import java.util.ArrayList;

public interface GameDAO {

    void clear() throws dataAccess.Exceptions.DataAccessException;

    int createGame(String gameName) throws DataAccessException;

    GameData getGame(int gameID) throws DataAccessException;

    ArrayList<GameData> listGames() throws DataAccessException;

    void setGameWhite(int gameID, String username) throws DataAccessException;
    void setGameBlack(int gameID, String username) throws DataAccessException;
    void addGameWatcher(int gameID, String username);
    void updateGame(int gameID, ChessGame game) throws DataAccessException;

}
