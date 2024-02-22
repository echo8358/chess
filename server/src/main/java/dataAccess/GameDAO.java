package dataAccess;

import chess.ChessGame;
import model.GameData;

import java.util.List;

public interface GameDAO {

    void clear();

    int createGame(String gameName);

    GameData getGame(int gameID);

    List<GameData> listGames();

    void updateGame(int gameID, ChessGame game);



}
