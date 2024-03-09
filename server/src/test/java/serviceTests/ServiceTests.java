package serviceTests;

import dataAccess.*;
import dataAccess.Exceptions.AlreadyTakenException;
import dataAccess.Exceptions.BadRequestException;
import dataAccess.Exceptions.DataAccessException;
import dataAccess.Exceptions.UnauthorizedException;
import model.AuthData;
import model.GameData;
import model.UserData;
import org.junit.jupiter.api.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import passoffTests.obfuscatedTestClasses.TestServerFacade;
import passoffTests.testClasses.TestException;
import passoffTests.testClasses.TestModels;
import server.CreateGame.CreateGameRequest;
import server.CreateGame.CreateGameResponse;
import server.JoinGame.JoinGameRequest;
import server.ListGame.ListGameRequest;
import server.ListGame.ListGameResponse;
import server.Login.LoginRequest;
import server.Login.LoginResponse;
import server.Logout.LogoutRequest;
import server.Register.RegisterRequest;
import server.Register.RegisterResponse;
import server.Server;
import service.DBService;
import service.GameService;
import service.UserService;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@SuppressWarnings("unused")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ServiceTests {

    private static UserService userService;
    private static GameService gameService;
    private static DBService dbService;
    private static UserDAO userDAO;
    private static GameDAO gameDAO;
    private static AuthDAO authDAO;

    @BeforeAll
    public static void init() {

        userService = new UserService();
        gameService = new GameService();
        dbService = new DBService();

        userDAO = new SQLUserDAO();
        gameDAO = new SQLGameDAO();
        authDAO = new SQLAuthDAO();
    }


    @BeforeEach
    public void setup() throws TestException, DataAccessException {
        dbService.clearDB();
    }

    @Test
    @Order(1)
    @DisplayName("Register User")
    public void userServiceRegisterUser() throws Exception {
        RegisterRequest registerRequest = new RegisterRequest("echo", "thisisagoodpassword", "urmom@thebomb.com");
        RegisterResponse registerResponse = userService.register(registerRequest);

        Assertions.assertNotNull(registerResponse.auth());

        //check database
        UserData newUser = userDAO.getUser("echo");
        Assertions.assertNotNull(newUser);
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        Assertions.assertTrue(encoder.matches("thisisagoodpassword", newUser.password()));
        Assertions.assertEquals(newUser.email(), "urmom@thebomb.com");
    }


    @Test
    @Order(2)
    @DisplayName("Register Existing User")
    public void userServiceRegisterExistingUser() throws Exception {
        RegisterRequest registerRequest = new RegisterRequest("echo", "thisisagoodpassword", "urmom@thebomb.com");
        RegisterResponse registerResponse = userService.register(registerRequest);
        Assertions.assertNotNull(registerResponse.auth());

        Assertions.assertThrows(AlreadyTakenException.class, () -> {
            userService.register(registerRequest);
        });
    }


    @Test
    @Order(3)
    @DisplayName("Login Valid User")
    public void userServiceLoginValidUser() throws Exception {
        RegisterRequest registerRequest = new RegisterRequest("echo", "thisisagoodpassword", "urmom@thebomb.com");
        RegisterResponse registerResponse = userService.register(registerRequest);

        LoginRequest loginRequest = new LoginRequest("echo", "thisisagoodpassword");
        LoginResponse loginResponse = userService.login(loginRequest);
        Assertions.assertNotNull(loginResponse.auth());

        //check database
        AuthData authData = authDAO.getAuthFromToken(loginResponse.auth().authToken());
        Assertions.assertEquals(authData.authToken(), loginResponse.auth().authToken());
        Assertions.assertEquals(authData.username(), loginResponse.auth().username());
    }


    @Test
    @Order(4)
    @DisplayName("Login User Does Not Exist")
    public void userServiceLoginNoExist() throws Exception {
        LoginRequest loginRequest = new LoginRequest("echo", "thisisagoodpassword");
        Assertions.assertThrows(UnauthorizedException.class, () -> {
            userService.login(loginRequest);
        });
    }

    @Test
    @Order(5)
    @DisplayName("Logout")
    public void userServiceLogout() throws Exception {
        RegisterRequest registerRequest = new RegisterRequest("echo", "thisisagoodpassword", "urmom@thebomb.com");
        RegisterResponse registerResponse = userService.register(registerRequest);

        LoginRequest loginRequest = new LoginRequest("echo", "thisisagoodpassword");
        LoginResponse loginResponse = userService.login(loginRequest);

        LogoutRequest logoutRequest = new LogoutRequest(loginResponse.auth().authToken());
        userService.logout(logoutRequest);

        Assertions.assertNull(authDAO.getAuthFromToken(loginResponse.auth().authToken()), "Auth token in database after logout.");
    }


    @Test
    @Order(6)
    @DisplayName("Logout Unauthorized")
    public void userServiceLogoutUnauthorized() throws Exception {
        LogoutRequest logoutRequest = new LogoutRequest("");
        assertThrows(UnauthorizedException.class, () -> {
            userService.logout(logoutRequest);
        });
    }


    @Test
    @Order(7)
    @DisplayName("Create Game")
    public void gameServiceCreateGame() throws Exception {
        RegisterRequest registerRequest = new RegisterRequest("echo", "thisisagoodpassword", "urmom@thebomb.com");
        RegisterResponse registerResponse = userService.register(registerRequest);

        CreateGameResponse createGameResponse = gameService.createGame(new CreateGameRequest("game1", registerResponse.auth().authToken() ));

        GameData gameData = gameDAO.getGame(createGameResponse.gameID());
        Assertions.assertNotNull(gameData);
        Assertions.assertEquals(gameData.gameName(), "game1");
    }


    @Test
    @Order(8)
    @DisplayName("Create Game Unauthorized")
    public void gameServiceCreateGameUnauthorized() throws Exception  {
        Assertions.assertThrows(UnauthorizedException.class, () -> {
            gameService.createGame(new CreateGameRequest("game1", "" ));
        });
    }


    @Test
    @Order(9)
    @DisplayName("Join Game")
    public void gameServiceJoinGame() throws Exception {
        RegisterRequest echoRequest = new RegisterRequest("echo", "thisisagoodpassword", "urmom@thebomb.com");
        RegisterResponse echoResponse = userService.register(echoRequest);
        RegisterRequest notEchoRequest = new RegisterRequest("notecho", "thisisagoodpassword", "urmom@thebomb.com");
        RegisterResponse notEchoResponse = userService.register(notEchoRequest);

        CreateGameResponse createGameResponse = gameService.createGame(new CreateGameRequest("game1", echoResponse.auth().authToken() ));

        gameService.joinGame(new JoinGameRequest("BLACK", createGameResponse.gameID(), echoResponse.auth().authToken()));
        gameService.joinGame(new JoinGameRequest("WHITE", createGameResponse.gameID(), notEchoResponse.auth().authToken()));

        GameData gameData = gameDAO.getGame(createGameResponse.gameID());
        Assertions.assertEquals(gameData.blackUsername(), "echo");
        Assertions.assertEquals(gameData.whiteUsername(), "notecho");

    }

    @Test
    @Order(9)
    @DisplayName("Join Game ID No Exist")
    public void gameServiceJoinGameNoExist() throws Exception {
        RegisterRequest echoRequest = new RegisterRequest("echo", "thisisagoodpassword", "urmom@thebomb.com");
        RegisterResponse echoResponse = userService.register(echoRequest);
        RegisterRequest notEchoRequest = new RegisterRequest("notecho", "thisisagoodpassword", "urmom@thebomb.com");
        RegisterResponse notEchoResponse = userService.register(notEchoRequest);

        CreateGameResponse createGameResponse = gameService.createGame(new CreateGameRequest("game1", echoResponse.auth().authToken() ));

        gameService.joinGame(new JoinGameRequest("BLACK", createGameResponse.gameID(), echoResponse.auth().authToken()));
        assertThrows(BadRequestException.class, () -> {
            gameService.joinGame(new JoinGameRequest("WHITE", -1, notEchoResponse.auth().authToken()));
        });

        GameData gameData = gameDAO.getGame(createGameResponse.gameID());
        Assertions.assertEquals(gameData.blackUsername(), "echo");
        Assertions.assertNotEquals(gameData.whiteUsername(), "notecho");

    }
    @Test
    @Order(9)
    @DisplayName("List Games")
    public void gameServiceListGames() throws Exception {
        RegisterRequest echoRequest = new RegisterRequest("echo", "thisisagoodpassword", "urmom@thebomb.com");
        RegisterResponse echoResponse = userService.register(echoRequest);
        RegisterRequest notEchoRequest = new RegisterRequest("notecho", "thisisagoodpassword", "urmom@thebomb.com");
        RegisterResponse notEchoResponse = userService.register(notEchoRequest);

        CreateGameResponse createGameResponse = gameService.createGame(new CreateGameRequest("game1", echoResponse.auth().authToken() ));

        gameService.joinGame(new JoinGameRequest("BLACK", createGameResponse.gameID(), echoResponse.auth().authToken()));
        gameService.joinGame(new JoinGameRequest("WHITE", createGameResponse.gameID(), notEchoResponse.auth().authToken()));

        CreateGameResponse createGameResponse2 = gameService.createGame(new CreateGameRequest("game2", echoResponse.auth().authToken() ));
        CreateGameResponse createGameResponse3 = gameService.createGame(new CreateGameRequest("game3", echoResponse.auth().authToken() ));
        CreateGameResponse createGameResponse4 = gameService.createGame(new CreateGameRequest("game4", echoResponse.auth().authToken() ));

        ListGameResponse listGameResponse = gameService.listGames(new ListGameRequest(echoResponse.auth().authToken()));

        Assertions.assertTrue(checkGame(createGameResponse.gameID(), gameDAO.getGame(createGameResponse.gameID()).gameName(), listGameResponse.games(), "notecho", "echo"));
        Assertions.assertTrue(checkGame(createGameResponse2.gameID(), gameDAO.getGame(createGameResponse2.gameID()).gameName(), listGameResponse.games(), null, null));
        Assertions.assertTrue(checkGame(createGameResponse3.gameID(), gameDAO.getGame(createGameResponse3.gameID()).gameName(), listGameResponse.games(), null, null));
        Assertions.assertTrue(checkGame(createGameResponse4.gameID(), gameDAO.getGame(createGameResponse4.gameID()).gameName(), listGameResponse.games(), null, null));
    }
    private boolean checkGame(int gameID, String gameName, ArrayList<GameData> gameList, String whiteUser, String blackUser) {
        for (GameData game: gameList) {
            if (game.gameID() == gameID && Objects.equals(game.gameName(), gameName) &&
                    Objects.equals(game.whiteUsername(), whiteUser) && Objects.equals(game.blackUsername(), blackUser)) {
                return true;
            }
        }
        return false;
    }

    @Test
    @Order(10)
    @DisplayName("List Games Unauthorized")
    public void gameServiceListGamesUnauthorized() throws Exception {
        RegisterRequest echoRequest = new RegisterRequest("echo", "thisisagoodpassword", "urmom@thebomb.com");
        RegisterResponse echoResponse = userService.register(echoRequest);
        RegisterRequest notEchoRequest = new RegisterRequest("notecho", "thisisagoodpassword", "urmom@thebomb.com");
        RegisterResponse notEchoResponse = userService.register(notEchoRequest);

        CreateGameResponse createGameResponse = gameService.createGame(new CreateGameRequest("game1", echoResponse.auth().authToken() ));

        gameService.joinGame(new JoinGameRequest("BLACK", createGameResponse.gameID(), echoResponse.auth().authToken()));
        gameService.joinGame(new JoinGameRequest("WHITE", createGameResponse.gameID(), notEchoResponse.auth().authToken()));

        CreateGameResponse createGameResponse2 = gameService.createGame(new CreateGameRequest("game2", echoResponse.auth().authToken() ));
        CreateGameResponse createGameResponse3 = gameService.createGame(new CreateGameRequest("game3", echoResponse.auth().authToken() ));
        CreateGameResponse createGameResponse4 = gameService.createGame(new CreateGameRequest("game4", echoResponse.auth().authToken() ));

        Assertions.assertThrows(UnauthorizedException.class, () -> {
            gameService.listGames(new ListGameRequest(""));
        });
    }

    @Test
    @Order(11)
    @DisplayName("Clear Database")
    public void databaseServiceClear() throws Exception {
        RegisterRequest echoRequest = new RegisterRequest("echo", "thisisagoodpassword", "urmom@thebomb.com");
        RegisterResponse echoResponse = userService.register(echoRequest);
        RegisterRequest notEchoRequest = new RegisterRequest("notecho", "thisisagoodpassword", "urmom@thebomb.com");
        RegisterResponse notEchoResponse = userService.register(notEchoRequest);

        CreateGameResponse createGameResponse = gameService.createGame(new CreateGameRequest("game1", echoResponse.auth().authToken() ));

        gameService.joinGame(new JoinGameRequest("BLACK", createGameResponse.gameID(), echoResponse.auth().authToken()));
        gameService.joinGame(new JoinGameRequest("WHITE", createGameResponse.gameID(), notEchoResponse.auth().authToken()));

        CreateGameResponse createGameResponse2 = gameService.createGame(new CreateGameRequest("game2", echoResponse.auth().authToken() ));
        CreateGameResponse createGameResponse3 = gameService.createGame(new CreateGameRequest("game3", echoResponse.auth().authToken() ));
        CreateGameResponse createGameResponse4 = gameService.createGame(new CreateGameRequest("game4", echoResponse.auth().authToken() ));

        dbService.clearDB();

        Assertions.assertEquals(gameDAO.listGames(), new ArrayList<>());
        Assertions.assertEquals(authDAO.listAuth(), new ArrayList<>());
        Assertions.assertEquals(userDAO.listUsers(), new ArrayList<>());
    }

}
