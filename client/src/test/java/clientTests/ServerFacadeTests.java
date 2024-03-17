package clientTests;

import org.junit.jupiter.api.*;
import server.Server;


public class ServerFacadeTests {

    private static Server server;

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

    @Test
    void registerTestNegative() {
    }

    @Test
    void logoutTestNegative() {
    }

    @Test
    void loginTestNegative() {
    }

    @Test
    void listGamesTestNegative() {
    }

    @Test
    void joinGameTestNegative() {
    }

    @Test
    void createGameTestNegative() {
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
