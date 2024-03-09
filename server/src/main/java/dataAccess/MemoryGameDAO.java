package dataAccess;

import chess.ChessGame;
import model.GameData;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MemoryGameDAO implements GameDAO{
    static ArrayList<GameData> gameList = new ArrayList<GameData>();
    static int nextID = 0;
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
    public ArrayList<GameData> listGames() {
        //this doesn't copy the elements in the list
        return new ArrayList<GameData>(gameList);
    }

    @Override
    public void setGameBlack(int gameID, String username) {
        setGameColor(gameID, username, "BLACK");
    }

    @Override
    public void setGameWhite(int gameID, String username) {
        setGameColor(gameID, username, "WHITE");
    }

    private void setGameColor(int gameID, String username, String color) {
        for (int i = 0; i < gameList.size(); i++)
        {
            GameData game = gameList.get(i);
            if (game.gameID() == gameID) {
                if (Objects.equals(color, "WHITE")) {
                    gameList.set(i, new GameData(game.gameID(),username, game.blackUsername(), game.gameName(), game.game()));
                }
                if (Objects.equals(color, "BLACK")) {
                    gameList.set(i, new GameData(game.gameID(),game.whiteUsername(), username, game.gameName(), game.game()));
                }
                break;
            }
        }
    }

    @Override
    public void addGameWatcher(int gameID, String username) {
        //do something next phase
    }

    @Override
    public void updateGame(int gameID, String newGame) {
        for (int i = 0; i < gameList.size(); i++)
        {
            GameData game = gameList.get(i);
            if (game.gameID() == gameID) {
                gameList.set(i, new GameData(game.gameID(),game.whiteUsername(), game.blackUsername(), game.gameName(), new ChessGame()));
                break;
            }
        }
    }
}
