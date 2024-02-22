package dataAccess;

import chess.ChessGame;
import model.GameData;

import java.util.ArrayList;
import java.util.List;

public class MemoryGameDAO implements GameDAO{
    ArrayList<GameData> gameList;
    int nextID = 0;
    @Override
    public void clear() {
        gameList.clear();
    }

    @Override
    public int createGame(String gameName) {
        nextID++;
        gameList.add(new GameData(nextID, null,null, gameName, new ChessGame()));
        return nextID;
    }

    @Override
    public GameData getGame(int gameID) {
        for (GameData game: gameList) {
            if (game.gameID() == gameID) return game;
        }
        return null;
    }

    @Override
    public List<GameData> listGames() {
        //this doesn't copy the elements in the list
        //TODO: Fix this
        return new ArrayList<GameData>(gameList);
    }

    @Override
    public void updateGame(int gameID, ChessGame newGame) {
        for (int i = 0; i < gameList.size(); i++)
        {
            GameData game = gameList.get(i);
            if (game.gameID() == gameID) {
                gameList.set(i, new GameData(game.gameID(),game.whiteUsername(), game.blackUsername(), game.gameName(), newGame));
                break;
            }
        }
    }
}
