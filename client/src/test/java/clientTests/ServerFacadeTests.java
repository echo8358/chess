package clientTests;

import ServerFacade.ServerFacade;
import dataAccess.Exceptions.BadRequestException;
import dataAccess.Exceptions.ForbiddenException;
import dataAccess.Exceptions.UnauthorizedException;
import model.AuthData;
import org.junit.jupiter.api.*;
import server.Server;


public class ServerFacadeTests {

    private static Server server;
    private static ServerFacade serverFacade;

    @BeforeAll
    public static void init() {
        server = new Server();
        var port = server.run(0);
        System.out.println("Started test HTTP server on " + port);
    }

    @AfterAll
    static void stopServer() {
        server.stop();
    }

    @BeforeEach
    void clearDatabase() {
        serverFacade.clearDatabase();
    }


    @Test
    void registerTestNegative() {
        serverFacade.register("echo", "password", "urmom@pm.me");
        Assertions.assertThrows(ForbiddenException.class, () -> {
            serverFacade.register("echo", "password", "urmom@pm.me");
        });
    }

    @Test
    void logoutTestNegative() {
        Assertions.assertThrows(UnauthorizedException.class, () -> {
            serverFacade.logout("");
        });
    }

    @Test
    void loginTestNegative() {
        serverFacade.register("echo", "password", "urmom@pm.me");
        Assertions.assertThrows(UnauthorizedException.class, () -> {
            serverFacade.login("echo", "password1");
        });
    }

    @Test
    void listGamesTestNegative() {
        Assertions.assertThrows(UnauthorizedException.class, () -> {
            serverFacade.listGames("");
        });
    }

    @Test
    void joinGameTestNegative() {
        AuthData authData = serverFacade.register("echo", "password", "urmom@pm.me");
        Assertions.assertThrows(BadRequestException.class, () -> {
            serverFacade.joinGame("WHITE", 123456, authData.authToken());
        });
    }

    @Test
    void createGameTestNegative() {
        AuthData authData = serverFacade.register("echo", "password", "urmom@pm.me");
        Assertions.assertThrows(UnauthorizedException.class, () -> {
            serverFacade.createGame("thebestgame", "");
        });
    }

    @Test
    void registerTestPositive() {
    }

    @Test
    void logoutTestPositive() {
    }

    @Test
    void loginTestPositive() {
    }

    @Test
    void listGamesTestPositive() {
    }

    @Test
    void joinGameTestPositive() {
    }

    @Test
    void createGameTestPositive() {
    }

}
