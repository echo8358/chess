package dataAccessTests;

import dataAccess.*;
import dataAccess.Exceptions.DataAccessException;
import model.UserData;
import org.junit.jupiter.api.*;
import passoffTests.testClasses.TestException;
import server.Register.RegisterRequest;
import server.Register.RegisterResponse;
import service.DBService;
import service.GameService;
import service.UserService;

public class DataAccessTests {
    private static UserDAO userDAO;
    private static GameDAO gameDAO;
    private static AuthDAO authDAO;
    private static DBService dbService;

    @BeforeAll
    public static void init() {

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

        //check database
        UserData newUser = userDAO.getUser("echo");
        Assertions.assertNotNull(newUser);
        Assertions.assertEquals(newUser.password(), "thisisagoodpassword");
        Assertions.assertEquals(newUser.email(), "urmom@thebomb.com");
    }
}
