package dataAccessTests;

import chess.ChessGame;
import chess.ChessMove;
import chess.ChessPosition;
import chess.InvalidMoveException;
import dataAccess.AuthDAO;
import dataAccess.Exceptions.DataAccessException;
import dataAccess.GameDAO;
import dataAccess.SQLAuthDAO;
import dataAccess.SQLGameDAO;
import model.GameData;
import model.UserData;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import passoffTests.testClasses.TestException;
import service.DBService;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SQLGameDAOTest {

    private static final GameDAO gameDAO = new SQLGameDAO();
    private static final DBService dbService = new DBService();
    private static final UserData testUserData0 = new UserData("echo", "password", "urmom@pm.me");
    private static final UserData testUserData1 = new UserData("notecho", "password123", "urmom@pm.me");
    private static final UserData testUserData2 = new UserData("urmom", "Pa$$word", "urmom@pm.me");
    private static final UserData testUserData3 = new UserData("dave", "afexrimandrapuse", "urmom@pm.me");
    private static final ArrayList<UserData> testUserDataArray = new ArrayList<UserData>();

    @BeforeAll
    public static void init() {
        testUserDataArray.add(testUserData0);
        testUserDataArray.add(testUserData1);
        testUserDataArray.add(testUserData2);
        testUserDataArray.add(testUserData3);
    }
    @BeforeEach
    public void setup() throws TestException, DataAccessException {
        dbService.clearDB();
    }

    @Test
    void createGameNegative() {
        assertThrows(DataAccessException.class, () -> {
            gameDAO.createGame("gaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaame0");
            });
    }

    @Test
    void updateGameNegative() throws DataAccessException, InvalidMoveException {
        int id = gameDAO.createGame("game");
        ArrayList<GameData> gameList = gameDAO.listGames();

        ChessGame tempGame = new ChessGame();
        tempGame.makeMove(new ChessMove(new ChessPosition(2, 1), new ChessPosition(3, 1), null));

        assertThrows(DataAccessException.class, () -> {
            gameDAO.updateGame(3676, tempGame);
        });

        GameData updatedGame = gameDAO.getGame(id);

        assertEquals(new ChessGame(), updatedGame.game());

    }

    @Test
    void getGameNegative() throws DataAccessException {
        assertNull(gameDAO.getGame(gameDAO.listGames().size()+1));
    }

    @Test
    void listGamesNegative() throws DataAccessException {
        assertEquals(gameDAO.listGames().size(), 0);
    }

    @Test
    void setGameWhiteNegative() throws DataAccessException {
        int id = gameDAO.createGame("game0");
        assertThrows(DataAccessException.class, () -> {
            gameDAO.setGameWhite(id, "eeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeecho");
        });
        GameData game = gameDAO.getGame(id);

        assertEquals(game.gameName(), "game0");
        assertNull(game.whiteUsername());
    }

    @Test
    void setGameBlackNegative() throws DataAccessException {
        int id = gameDAO.createGame("game0");
        assertThrows(DataAccessException.class, () -> {
            gameDAO.setGameBlack(id, "eeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeecho");
        });
        GameData game = gameDAO.getGame(id);

        assertEquals(game.gameName(), "game0");
        assertNull(game.blackUsername());
    }

    @Test
    void addGameWatcherNegative() {
        assert(true); //not implemented yet.
    }
    @Test
    void clearPositive() throws DataAccessException {
        gameDAO.createGame("game");
        assertEquals(gameDAO.listGames().size(), 1);
        gameDAO.clear();
        assertEquals(gameDAO.listGames().size(), 0);
    }

    @Test
    void createGamePositive() throws DataAccessException {
        int id = gameDAO.createGame("game0");
        GameData game = gameDAO.getGame(id);

        assertEquals(game.gameName(), "game0");
        assertNull(game.whiteUsername());
        assertNull(game.blackUsername());
        assertEquals(new ChessGame(), game.game() );
    }

    @Test
    void updateGamePositive() throws DataAccessException, InvalidMoveException {
        int id = gameDAO.createGame("game0");
        GameData old_game = gameDAO.getGame(id);

        ChessGame tempGame = new ChessGame();
        tempGame.makeMove(new ChessMove(new ChessPosition(2, 1), new ChessPosition(3, 1), null));

        gameDAO.updateGame(id, tempGame);

        GameData updatedGame = gameDAO.getGame(id);

        assertEquals(tempGame, updatedGame.game());
    }

    @Test
    void getGamePositive() throws DataAccessException {
        int id = gameDAO.createGame("game0");
        GameData game = gameDAO.getGame(id);

        assertEquals(game.gameName(), "game0");
    }

    @Test
    void listGamesPositive() throws DataAccessException, InvalidMoveException {
        int id0 = gameDAO.createGame("game0");
        gameDAO.setGameWhite(id0, "echo");

        int id1 = gameDAO.createGame("game1");
        gameDAO.setGameBlack(id1, "notecho");

        int id2 = gameDAO.createGame("game2");
        gameDAO.setGameWhite(id2, "echo");
        gameDAO.setGameBlack(id2, "notecho");

        int id3 = gameDAO.createGame("game3");
        gameDAO.setGameWhite(id3, "echo");
        gameDAO.setGameBlack(id3, "notecho");

        ChessGame tempGame = new ChessGame();
        tempGame.makeMove(new ChessMove(new ChessPosition(2, 1), new ChessPosition(3, 1), null));

        gameDAO.updateGame(id3, tempGame);

        ArrayList<GameData> gameList = gameDAO.listGames();

        assertEquals(gameList.get(0).gameName(), "game0");
        assertEquals(gameList.get(0).whiteUsername(), "echo");

        assertEquals(gameList.get(1).gameName(), "game1");
        assertEquals(gameList.get(1).blackUsername(), "notecho");

        assertEquals(gameList.get(2).gameName(), "game2");
        assertEquals(gameList.get(2).whiteUsername(), "echo");
        assertEquals(gameList.get(2).blackUsername(), "notecho");

        assertEquals(gameList.get(3).gameName(), "game3");
        assertEquals(gameList.get(3).whiteUsername(), "echo");
        assertEquals(gameList.get(3).blackUsername(), "notecho");
        assertEquals(gameList.get(3).game(), tempGame);
    }

    @Test
    void setGameWhitePositive() throws DataAccessException {
        int id = gameDAO.createGame("game0");
        gameDAO.setGameWhite(id, "echo");
        GameData game = gameDAO.getGame(id);

        assertEquals(game.gameName(), "game0");
        assertEquals(game.whiteUsername(), "echo");
    }

    @Test
    void setGameBlackPositive() throws DataAccessException {
        int id = gameDAO.createGame("game0");
        gameDAO.setGameBlack(id, "echo");
        GameData game = gameDAO.getGame(id);

        assertEquals(game.gameName(), "game0");
        assertEquals(game.blackUsername(), "echo");
    }

    @Test
    void addGameWatcherPositive() {
        assert(true); //yet to be implemented
    }
}