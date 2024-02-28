package dataAccess;

import chess.ChessGame;
import model.GameData;

import java.util.ArrayList;

public interface GameDAO {

    void clear();

    int createGame(String gameName);

    GameData getGame(int gameID);

    ArrayList<GameData> listGames();

    void setGameWhite(int gameID, String username);
    void setGameBlack(int gameID, String username);
    void addGameWatcher(int gameID, String username);

    void updateGame(int gameID, ChessGame game);



}
