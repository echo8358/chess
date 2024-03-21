package clientTests;

import ServerFacade.ServerFacade;
import ServerFacade.ResponseException;
import dataAccess.Exceptions.BadRequestException;
import dataAccess.Exceptions.ForbiddenException;
import dataAccess.Exceptions.UnauthorizedException;
import model.AuthData;
import model.GameData;
import org.junit.jupiter.api.*;
import server.Server;

import java.util.ArrayList;


public class ServerFacadeTests {

    private static Server server;
    private static ServerFacade serverFacade;

    @BeforeAll
    public static void init() {
        server = new Server();
        var port = server.run(3676);
        serverFacade = new ServerFacade("http://localhost:3676");
        System.out.println("Started test HTTP server on " + port);
    }

    @AfterAll
    static void stopServer() {
        server.stop();
    }

    @BeforeEach
    void clearDatabase() throws ResponseException {
        serverFacade.clearDatabase();
    }


    @Test
    void registerTestNegative() throws ResponseException {
        serverFacade.register("echo", "password", "urmom@pm.me");
        Assertions.assertThrows(ResponseException.class, () -> {
            serverFacade.register("echo", "password", "urmom@pm.me");
        });
    }

    @Test
    void logoutTestNegative() {
        Assertions.assertThrows(ResponseException.class, () -> {
            serverFacade.logout("");
        });
    }

    @Test
    void loginTestNegative() throws ResponseException {
        serverFacade.register("echo", "password", "urmom@pm.me");
        Assertions.assertThrows(ResponseException.class, () -> {
            serverFacade.login("echo", "password1");
        });
    }

    @Test
    void listGamesTestNegative() {
        Assertions.assertThrows(ResponseException.class, () -> {
            serverFacade.listGames("");
        });
    }

    @Test
    void joinGameTestNegative() throws ResponseException {
        AuthData authData = serverFacade.register("echo", "password", "urmom@pm.me");
        Assertions.assertThrows(ResponseException.class, () -> {
            serverFacade.joinGame("WHITE", 123456, authData.authToken());
        });
    }

    @Test
    void createGameTestNegative() throws ResponseException {
        AuthData authData = serverFacade.register("echo", "password", "urmom@pm.me");
        Assertions.assertThrows(ResponseException.class, () -> {
            serverFacade.createGame("thebestgame", "");
        });
    }

    @Test
    void registerTestPositive() {
        Assertions.assertDoesNotThrow(() -> {
            serverFacade.register("echo", "password", "urmom@pm.me");
        });
    }

    @Test
    void logoutTestPositive() throws ResponseException {
        AuthData authData = serverFacade.register("echo", "password", "urmom@pm.me");
        Assertions.assertDoesNotThrow(() -> {
            serverFacade.logout(authData.authToken());
        });
    }

    @Test
    void loginTestPositive() throws ResponseException {
        serverFacade.register("echo", "password", "urmom@pm.me");
        Assertions.assertDoesNotThrow(() -> {
            serverFacade.login("echo", "password");
        });
    }

    @Test
    void listGamesTestPositive() throws ResponseException {
        AuthData authData = serverFacade.register("echo", "password", "urmom@pm.me");
        serverFacade.createGame("thebestgame1", authData.authToken());
        serverFacade.createGame("thebestgame2", authData.authToken());
        serverFacade.createGame("thebestgame3", authData.authToken());
        ArrayList<GameData> gameList = serverFacade.listGames(authData.authToken());
        Assertions.assertEquals(gameList.size(), 3);

        boolean assertPass = false;
        for (GameData game: gameList) {
           if (game.gameName() == "thebestgame1") {
               assertPass = true;
               break;
           }
        }
        Assertions.assertTrue(assertPass);
        assertPass = false;
        for (GameData game: gameList) {
            if (game.gameName() == "thebestgame2") {
                assertPass = true;
                break;
            }
        }
        Assertions.assertTrue(assertPass);
        assertPass = false;
        for (GameData game: gameList) {
            if (game.gameName() == "thebestgame3") {
                assertPass = true;
                break;
            }
        }
        Assertions.assertTrue(assertPass);

    }

    @Test
    void joinGameTestPositive() throws ResponseException {
        AuthData authDataEcho = serverFacade.register("echo", "password", "urmom@pm.me");
        AuthData authDataNotEcho = serverFacade.register("notecho", "password", "urmom@pm.me");
        int gameID = serverFacade.createGame("thebestgame1", authDataEcho.authToken());
        serverFacade.joinGame("WHITE", gameID, authDataEcho.authToken());
        serverFacade.joinGame("BLACK", gameID, authDataNotEcho.authToken());
        ArrayList<GameData> gameList = serverFacade.listGames(authDataNotEcho.authToken());
        Assertions.assertEquals(gameList.getFirst().whiteUsername(), "echo");
        Assertions.assertEquals(gameList.getFirst().blackUsername(), "notecho");
    }

    @Test
    void createGameTestPositive() throws ResponseException {
        AuthData authData = serverFacade.register("echo", "password", "urmom@pm.me");
        serverFacade.createGame("game", authData.authToken());
        ArrayList<GameData> gameList = serverFacade.listGames(authData.authToken());
        Assertions.assertEquals(1, gameList.size());
        Assertions.assertEquals("game", gameList.getFirst().gameName());
    }

}
