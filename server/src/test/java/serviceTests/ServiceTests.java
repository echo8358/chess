package serviceTests;

import chess.ChessGame;
import dataAccess.*;
import model.AuthData;
import model.GameData;
import model.UserData;
import org.junit.jupiter.api.*;
import passoffTests.obfuscatedTestClasses.TestServerFacade;
import passoffTests.testClasses.TestException;
import passoffTests.testClasses.TestModels;
import server.CreateGame.CreateGameRequest;
import server.CreateGame.CreateGameResponse;
import server.JoinGame.JoinGameRequest;
import server.Login.LoginRequest;
import server.Login.LoginResponse;
import server.Logout.LogoutRequest;
import server.Register.RegisterRequest;
import server.Register.RegisterResponse;
import server.Server;
import service.DBService;
import service.GameService;
import service.UserService;

import java.net.HttpURLConnection;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;

@SuppressWarnings("unused")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ServiceTests {

    private static TestModels.TestUser existingUser;

    private static TestModels.TestUser newUser;

    private static TestModels.TestCreateRequest createRequest;

    private static TestServerFacade serverFacade;
    private static Server server;

    private String existingAuth;

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

        userDAO = new MemoryUserDAO();
        gameDAO = new MemoryGameDAO();
        authDAO = new MemoryAuthDAO();
    }


    @BeforeEach
    public void setup() throws TestException {
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
        Assertions.assertEquals(newUser.password(), "thisisagoodpassword");
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
        assertThrows(BadRequestException.class, () -> {
            gameService.joinGame(new JoinGameRequest("WHITE", -1, notEchoResponse.auth().authToken()));
        });

        GameData gameData = gameDAO.getGame(createGameResponse.gameID());
        Assertions.assertEquals(gameData.blackUsername(), "echo");
        Assertions.assertNotEquals(gameData.whiteUsername(), "notecho");

    }

    /*
    @Test
    @Order(10)
    @DisplayName("Invalid Auth Logout")
    public void failLogout() throws TestException {
        //log out user twice
        //second logout should fail
        serverFacade.logout(existingAuth);
        TestModels.TestResult result = serverFacade.logout(existingAuth);

        Assertions.assertEquals(HttpURLConnection.HTTP_UNAUTHORIZED, serverFacade.getStatusCode(),
                "Server response code was not 401 Unauthorized");
        Assertions.assertTrue(result.message.toLowerCase(Locale.ROOT).contains("error"),
                "Response did not return error message");
    }


    @Test
    @Order(11)
    @DisplayName("Valid Creation")
    public void goodCreate() throws TestException {
        TestModels.TestCreateResult createResult = serverFacade.createGame(createRequest, existingAuth);

        Assertions.assertEquals(HttpURLConnection.HTTP_OK, serverFacade.getStatusCode(),
                "Server response code was not 200 OK");
        Assertions.assertFalse(
                createResult.message != null && createResult.message.toLowerCase(Locale.ROOT).contains("error"),
                "Result returned an error message");
        Assertions.assertNotNull(createResult.gameID, "Result did not return a game ID");
        Assertions.assertTrue(createResult.gameID > 0, "Result returned invalid game ID");
    }


    @Test
    @Order(12)
    @DisplayName("Create with Bad Authentication")
    public void badAuthCreate() throws TestException {
        //log out user so auth is invalid
        serverFacade.logout(existingAuth);

        TestModels.TestCreateResult createResult = serverFacade.createGame(createRequest, existingAuth);

        Assertions.assertEquals(HttpURLConnection.HTTP_UNAUTHORIZED, serverFacade.getStatusCode(),
                "Server response code was not 401 Unauthorized");
        Assertions.assertTrue(createResult.message.toLowerCase(Locale.ROOT).contains("error"),
                "Bad result did not return an error message");
        Assertions.assertNull(createResult.gameID, "Bad result returned a game ID");
    }


    @Test
    @Order(13)
    @DisplayName("Watch Game")
    public void goodWatch() throws TestException {
        //create game
        TestModels.TestCreateResult createResult = serverFacade.createGame(createRequest, existingAuth);

        //make watch request
        TestModels.TestJoinRequest watchRequest = new TestModels.TestJoinRequest();
        watchRequest.gameID = createResult.gameID;

        //try watch
        TestModels.TestResult watchResult = serverFacade.verifyJoinPlayer(watchRequest, existingAuth);

        //check succeeded
        Assertions.assertEquals(HttpURLConnection.HTTP_OK, serverFacade.getStatusCode(),
                "Server response code was not 200 OK");
        Assertions.assertFalse(
                watchResult.message != null && watchResult.message.toLowerCase(Locale.ROOT).contains("error"),
                "Result returned an error message");


        TestModels.TestListResult listResult = serverFacade.listGames(existingAuth);
        Assertions.assertEquals(HttpURLConnection.HTTP_OK, serverFacade.getStatusCode(),
                "Server response code was not 200 OK");
        Assertions.assertEquals(1, listResult.games.length, "List Games returned an incorrect number of games");
        Assertions.assertNull(listResult.games[0].whiteUsername, "Player present on a game that no player joined");
        Assertions.assertNull(listResult.games[0].blackUsername, "Player present on a game that no player joined");
    }


    @Test
    @Order(14)
    @DisplayName("Watch Bad Authentication")
    public void badAuthWatch() throws TestException {
        //create game
        TestModels.TestCreateResult createResult = serverFacade.createGame(createRequest, existingAuth);

        //make watch request
        TestModels.TestJoinRequest watchRequest = new TestModels.TestJoinRequest();
        watchRequest.gameID = createResult.gameID;

        //try watch
        TestModels.TestResult watchResult = serverFacade.verifyJoinPlayer(watchRequest, existingAuth + "bad stuff");

        //check failed
        Assertions.assertEquals(HttpURLConnection.HTTP_UNAUTHORIZED, serverFacade.getStatusCode(),
                "Server response code was not 401 Unauthorized");
        Assertions.assertTrue(
                watchResult.message != null && watchResult.message.toLowerCase(Locale.ROOT).contains("error"),
                "Invalid Request didn't return an error message");
    }


    @Test
    @Order(15)
    @DisplayName("Watch Bad Game ID")
    public void badGameIDWatch() throws TestException {
        //create game
        TestModels.TestCreateResult createResult = serverFacade.createGame(createRequest, existingAuth);

        //make watch request
        TestModels.TestJoinRequest watchRequest = new TestModels.TestJoinRequest();
        watchRequest.gameID = 0;

        //try watch
        TestModels.TestResult watchResult = serverFacade.verifyJoinPlayer(watchRequest, existingAuth);

        //check failed
        Assertions.assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, serverFacade.getStatusCode(),
                "Server response code was not 400 Bad Request");
        Assertions.assertTrue(
                watchResult.message != null && watchResult.message.toLowerCase(Locale.ROOT).contains("error"),
                "Invalid Request didn't return an error message");
    }


    @Test
    @Order(16)
    @DisplayName("Many Watchers")
    public void manyWatch() throws TestException {
        //create game
        createRequest = new TestModels.TestCreateRequest();
        createRequest.gameName = "Test Game";
        TestModels.TestCreateResult createResult = serverFacade.createGame(createRequest, existingAuth);

        //make watch request
        TestModels.TestJoinRequest watchRequest = new TestModels.TestJoinRequest();
        watchRequest.gameID = createResult.gameID;

        //try watch
        TestModels.TestResult watchResult = serverFacade.verifyJoinPlayer(watchRequest, existingAuth);

        //check succeeded
        Assertions.assertEquals(HttpURLConnection.HTTP_OK, serverFacade.getStatusCode(),
                "Server response code was not 200 OK");
        Assertions.assertFalse(
                watchResult.message != null && watchResult.message.toLowerCase(Locale.ROOT).contains("error"),
                "Result returned an error message");


        //next watcher
        TestModels.TestRegisterRequest registerRequest = new TestModels.TestRegisterRequest();
        registerRequest.username = "a";
        registerRequest.password = "A";
        registerRequest.email = "a.A";
        TestModels.TestLoginRegisterResult registerResult = serverFacade.register(registerRequest);
        watchResult = serverFacade.verifyJoinPlayer(watchRequest, registerResult.authToken);

        //check succeeded
        Assertions.assertEquals(HttpURLConnection.HTTP_OK, serverFacade.getStatusCode(),
                "Server response code was not 200 OK");
        Assertions.assertFalse(
                watchResult.message != null && watchResult.message.toLowerCase(Locale.ROOT).contains("error"),
                "Result returned an error message");


        //next watcher
        registerRequest = new TestModels.TestRegisterRequest();
        registerRequest.username = "b";
        registerRequest.password = "B";
        registerRequest.email = "b.B";
        registerResult = serverFacade.register(registerRequest);
        watchResult = serverFacade.verifyJoinPlayer(watchRequest, registerResult.authToken);

        //check succeeded
        Assertions.assertEquals(HttpURLConnection.HTTP_OK, serverFacade.getStatusCode(),
                "Server response code was not 200 OK");
        Assertions.assertFalse(
                watchResult.message != null && watchResult.message.toLowerCase(Locale.ROOT).contains("error"),
                "Result returned an error message");


        //next watcher
        registerRequest = new TestModels.TestRegisterRequest();
        registerRequest.username = "c";
        registerRequest.password = "C";
        registerRequest.email = "c.C";
        registerResult = serverFacade.register(registerRequest);
        watchResult = serverFacade.verifyJoinPlayer(watchRequest, registerResult.authToken);

        //check succeeded
        Assertions.assertEquals(HttpURLConnection.HTTP_OK, serverFacade.getStatusCode(),
                "Server response code was not 200 OK");
        Assertions.assertFalse(
                watchResult.message != null && watchResult.message.toLowerCase(Locale.ROOT).contains("error"),
                "Result returned an error message");
    }


    @Test
    @Order(17)
    @DisplayName("Join Created Game")
    public void goodJoin() throws TestException {
        //create game
        TestModels.TestCreateResult createResult = serverFacade.createGame(createRequest, existingAuth);

        //join as white
        TestModels.TestJoinRequest joinRequest = new TestModels.TestJoinRequest();
        joinRequest.gameID = createResult.gameID;
        joinRequest.playerColor = ChessGame.TeamColor.WHITE;

        //try join
        TestModels.TestResult joinResult = serverFacade.verifyJoinPlayer(joinRequest, existingAuth);

        //check
        Assertions.assertEquals(HttpURLConnection.HTTP_OK, serverFacade.getStatusCode(),
                "Server response code was not 200 OK");
        Assertions.assertFalse(
                joinResult.message != null && joinResult.message.toLowerCase(Locale.ROOT).contains("error"),
                "Result returned an error message");

        TestModels.TestListResult listResult = serverFacade.listGames(existingAuth);

        Assertions.assertEquals(1, listResult.games.length);
        Assertions.assertEquals(existingUser.username, listResult.games[0].whiteUsername);
        Assertions.assertNull(listResult.games[0].blackUsername);
    }


    @Test
    @Order(18)
    @DisplayName("Join Bad Authentication")
    public void badAuthJoin() throws TestException {
        //create game
        TestModels.TestCreateResult createResult = serverFacade.createGame(createRequest, existingAuth);

        //join as white
        TestModels.TestJoinRequest joinRequest = new TestModels.TestJoinRequest();
        joinRequest.gameID = createResult.gameID;
        joinRequest.playerColor = ChessGame.TeamColor.WHITE;

        //try join
        TestModels.TestResult joinResult = serverFacade.verifyJoinPlayer(joinRequest, existingAuth + "bad stuff");

        //check
        Assertions.assertEquals(HttpURLConnection.HTTP_UNAUTHORIZED, serverFacade.getStatusCode(),
                "Server response code was not 401 Unauthorized");
        Assertions.assertTrue(
                joinResult.message != null && joinResult.message.toLowerCase(Locale.ROOT).contains("error"),
                "Invalid Request didn't return an error message");
    }


    @Test
    @Order(19)
    @DisplayName("Join Bad Team Color")
    public void badColorJoin() throws TestException {
        //create game
        TestModels.TestCreateResult createResult = serverFacade.createGame(createRequest, existingAuth);

        //add existing user as black
        TestModels.TestJoinRequest joinRequest = new TestModels.TestJoinRequest();
        joinRequest.gameID = createResult.gameID;
        joinRequest.playerColor = ChessGame.TeamColor.BLACK;
        serverFacade.verifyJoinPlayer(joinRequest, existingAuth);

        //register second user
        TestModels.TestRegisterRequest registerRequest = new TestModels.TestRegisterRequest();
        registerRequest.username = newUser.username;
        registerRequest.password = newUser.password;
        registerRequest.email = newUser.email;
        TestModels.TestLoginRegisterResult registerResult = serverFacade.register(registerRequest);

        //join request trying to also join  as black
        joinRequest = new TestModels.TestJoinRequest();
        joinRequest.gameID = createResult.gameID;
        joinRequest.playerColor = ChessGame.TeamColor.BLACK;
        TestModels.TestResult joinResult = serverFacade.verifyJoinPlayer(joinRequest, registerResult.authToken);

        //check failed
        Assertions.assertEquals(HttpURLConnection.HTTP_FORBIDDEN, serverFacade.getStatusCode(),
                "Server response code was not 403 Forbidden");
        Assertions.assertTrue(
                joinResult.message != null && joinResult.message.toLowerCase(Locale.ROOT).contains("error"),
                "Invalid Request didn't return an error message");
    }


    @Test
    @Order(20)
    @DisplayName("Join Bad Game ID")
    public void badGameIDJoin() throws TestException {
        //create game
        createRequest = new TestModels.TestCreateRequest();
        TestModels.TestCreateResult createResult = serverFacade.createGame(createRequest, existingAuth);

        //join as white
        TestModels.TestJoinRequest joinRequest = new TestModels.TestJoinRequest();
        joinRequest.gameID = 0;
        joinRequest.playerColor = ChessGame.TeamColor.WHITE;

        //try join
        TestModels.TestResult joinResult = serverFacade.verifyJoinPlayer(joinRequest, existingAuth);

        //check
        Assertions.assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, serverFacade.getStatusCode(),
                "Server response code was not 400 Bad Request");
        Assertions.assertTrue(
                joinResult.message != null && joinResult.message.toLowerCase(Locale.ROOT).contains("error"),
                "Invalid Request didn't return an error message");
    }


    @Test
    @Order(21)
    @DisplayName("List No Games")
    public void noGamesList() throws TestException {
        TestModels.TestListResult result = serverFacade.listGames(existingAuth);

        Assertions.assertEquals(HttpURLConnection.HTTP_OK, serverFacade.getStatusCode(),
                "Server response code was not 200 OK");
        Assertions.assertTrue(result.games == null || result.games.length == 0,
                "Found games when none should be there");
        Assertions.assertFalse(result.message != null && result.message.toLowerCase(Locale.ROOT).contains("error"),
                "Result returned an error message");
    }


    @Test
    @Order(22)
    @DisplayName("List Multiple Games")
    public void gamesList() throws TestException {
        //register a few users to create games
        TestModels.TestRegisterRequest registerRequest = new TestModels.TestRegisterRequest();
        registerRequest.username = "a";
        registerRequest.password = "A";
        registerRequest.email = "a.A";
        TestModels.TestLoginRegisterResult userA = serverFacade.register(registerRequest);

        registerRequest.username = "b";
        registerRequest.password = "B";
        registerRequest.email = "b.B";
        TestModels.TestLoginRegisterResult userB = serverFacade.register(registerRequest);

        registerRequest.username = "c";
        registerRequest.password = "C";
        registerRequest.email = "c.C";
        TestModels.TestLoginRegisterResult userC = serverFacade.register(registerRequest);

        //create games

        //1 as black from A
        createRequest.gameName = "I'm numbah one!";
        TestModels.TestCreateResult game1 = serverFacade.createGame(createRequest, userA.authToken);

        //1 as white from B
        createRequest.gameName = "Lonely";
        TestModels.TestCreateResult game2 = serverFacade.createGame(createRequest, userB.authToken);

        //1 of each from C
        createRequest.gameName = "GG";
        TestModels.TestCreateResult game3 = serverFacade.createGame(createRequest, userC.authToken);
        createRequest.gameName = "All by myself";
        TestModels.TestCreateResult game4 = serverFacade.createGame(createRequest, userC.authToken);

        //A join game 1 as black
        TestModels.TestJoinRequest joinRequest = new TestModels.TestJoinRequest();
        joinRequest.playerColor = ChessGame.TeamColor.BLACK;
        joinRequest.gameID = game1.gameID;
        serverFacade.verifyJoinPlayer(joinRequest, userA.authToken);

        //B join game 2 as white
        joinRequest.playerColor = ChessGame.TeamColor.WHITE;
        joinRequest.gameID = game2.gameID;
        serverFacade.verifyJoinPlayer(joinRequest, userB.authToken);

        //C join game 3 as white
        joinRequest.playerColor = ChessGame.TeamColor.WHITE;
        joinRequest.gameID = game3.gameID;
        serverFacade.verifyJoinPlayer(joinRequest, userC.authToken);

        //A join game3 as black
        joinRequest.playerColor = ChessGame.TeamColor.BLACK;
        joinRequest.gameID = game3.gameID;
        serverFacade.verifyJoinPlayer(joinRequest, userA.authToken);

        //C play self in game 4
        joinRequest.playerColor = ChessGame.TeamColor.WHITE;
        joinRequest.gameID = game4.gameID;
        serverFacade.verifyJoinPlayer(joinRequest, userC.authToken);
        joinRequest.playerColor = ChessGame.TeamColor.BLACK;
        joinRequest.gameID = game4.gameID;
        serverFacade.verifyJoinPlayer(joinRequest, userC.authToken);

        //create expected entry items
        Collection<TestModels.TestListResult.TestListEntry> expectedList = new HashSet<>();

        //game 1
        TestModels.TestListResult.TestListEntry entry = new TestModels.TestListResult.TestListEntry();
        entry.gameID = game1.gameID;
        entry.gameName = "I'm numbah one!";
        entry.blackUsername = userA.username;
        entry.whiteUsername = null;
        expectedList.add(entry);

        //game 2
        entry = new TestModels.TestListResult.TestListEntry();
        entry.gameID = game2.gameID;
        entry.gameName = "Lonely";
        entry.blackUsername = null;
        entry.whiteUsername = userB.username;
        expectedList.add(entry);

        //game 3
        entry = new TestModels.TestListResult.TestListEntry();
        entry.gameID = game3.gameID;
        entry.gameName = "GG";
        entry.blackUsername = userA.username;
        entry.whiteUsername = userC.username;
        expectedList.add(entry);

        //game 4
        entry = new TestModels.TestListResult.TestListEntry();
        entry.gameID = game4.gameID;
        entry.gameName = "All by myself";
        entry.blackUsername = userC.username;
        entry.whiteUsername = userC.username;
        expectedList.add(entry);

        //list games
        TestModels.TestListResult listResult = serverFacade.listGames(existingAuth);
        Assertions.assertEquals(HttpURLConnection.HTTP_OK, serverFacade.getStatusCode(),
                "Server response code was not 200 OK");
        Collection<TestModels.TestListResult.TestListEntry> returnedList =
                new HashSet<>(Arrays.asList(listResult.games));

        //check
        Assertions.assertEquals(expectedList, returnedList, "Returned Games list was incorrect");
    }


    @Test
    @Order(23)
    @DisplayName("Clear Test")
    public void clearData() throws TestException {
        //create filler games
        createRequest.gameName = "Mr. Meeseeks";
        serverFacade.createGame(createRequest, existingAuth);

        createRequest.gameName = "Awesome game";
        serverFacade.createGame(createRequest, existingAuth);

        //log in new user
        TestModels.TestRegisterRequest registerRequest = new TestModels.TestRegisterRequest();
        registerRequest.username = "Spongebob";
        registerRequest.password = "Squarepants";
        registerRequest.email = "pineapple@under.sea";
        TestModels.TestLoginRegisterResult registerResult = serverFacade.register(registerRequest);

        //create and join game for new user
        createRequest.gameName = "Patrick";
        TestModels.TestCreateResult createResult = serverFacade.createGame(createRequest, registerResult.authToken);

        TestModels.TestJoinRequest joinRequest = new TestModels.TestJoinRequest();
        joinRequest.gameID = createResult.gameID;
        joinRequest.playerColor = ChessGame.TeamColor.WHITE;
        serverFacade.verifyJoinPlayer(joinRequest, registerResult.authToken);

        //do clear
        TestModels.TestResult clearResult = serverFacade.clear();

        //test clear successful
        Assertions.assertEquals(HttpURLConnection.HTTP_OK, serverFacade.getStatusCode(),
                "Server response code was not 200 OK");
        Assertions.assertFalse(
                clearResult.message != null && clearResult.message.toLowerCase(Locale.ROOT).contains("error"),
                "Clear Result returned an error message");

        //make sure neither user can log in
        //first user
        TestModels.TestLoginRequest loginRequest = new TestModels.TestLoginRequest();
        loginRequest.username = existingUser.username;
        loginRequest.password = existingUser.password;
        TestModels.TestLoginRegisterResult loginResult = serverFacade.login(loginRequest);
        Assertions.assertEquals(HttpURLConnection.HTTP_UNAUTHORIZED, serverFacade.getStatusCode(),
                "Server response code was not 401 Unauthorized");

        //second user
        loginRequest.username = "Spongebob";
        loginRequest.password = "Squarepants";
        serverFacade.login(loginRequest);
        Assertions.assertEquals(HttpURLConnection.HTTP_UNAUTHORIZED, serverFacade.getStatusCode(),
                "Server response code was not 401 Unauthorized");

        //try to use old auth token to list games
        serverFacade.listGames(existingAuth);
        Assertions.assertEquals(HttpURLConnection.HTTP_UNAUTHORIZED, serverFacade.getStatusCode(),
                "Server response code was not 401 Unauthorized");

        //log in new user and check that list is empty
        registerResult = serverFacade.register(registerRequest);
        TestModels.TestListResult listResult = serverFacade.listGames(registerResult.authToken);

        //check listResult
        Assertions.assertEquals(HttpURLConnection.HTTP_OK, serverFacade.getStatusCode(),
                "Server response code was not 200 OK");
        Assertions.assertEquals(0, listResult.games.length, "list result did not return 0 games after clear");
    }


    @Test
    @Order(24)
    @DisplayName("Multiple Clears")
    public void multipleClear() throws TestException {

        //clear multiple times
        serverFacade.clear();
        serverFacade.clear();
        TestModels.TestResult result = serverFacade.clear();

        //make sure returned good
        Assertions.assertEquals(HttpURLConnection.HTTP_OK, serverFacade.getStatusCode(),
                "Server response code was not 200 OK");
        Assertions.assertFalse(result.message != null && result.message.toLowerCase(Locale.ROOT).contains("error"),
                "Clear Result returned an error message");
    }

 */

}
