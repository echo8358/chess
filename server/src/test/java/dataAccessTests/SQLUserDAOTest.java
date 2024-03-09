package dataAccessTests;

import dataAccess.Exceptions.AlreadyTakenException;
import dataAccess.Exceptions.DataAccessException;
import dataAccess.SQLUserDAO;
import dataAccess.UserDAO;
import model.UserData;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opentest4j.TestAbortedException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import passoffTests.testClasses.TestException;
import server.Register.RegisterRequest;
import server.Register.RegisterResponse;
import service.DBService;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

class SQLUserDAOTest {

    private static final UserDAO userDAO = new SQLUserDAO();
    private static final DBService dbService = new DBService();
    private static final UserData testUserData0 = new UserData("echo", "password", "urmom@pm.me");
    private static final UserData testUserData1 = new UserData("notecho", "password123", "urmom@pm.me");
    private static final UserData testUserData2 = new UserData("urmom", "Pa$$word", "urmom@pm.me");
    private static final UserData testUserData3 = new UserData("dave", "afexrimandrapuse", "urmom@pm.me");
    private static final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
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
    void createUserNegative() throws DataAccessException {
        userDAO.createUser(testUserData0);

        Assertions.assertThrows(AlreadyTakenException.class, () -> {
            userDAO.createUser(testUserData0);
        });
    }

    @Test
    void getUserNegative() throws DataAccessException {
        //get user that does not exist
        Assertions.assertNull(userDAO.getUser("echo"));
    }

    @Test
    void listUsersNegative() throws DataAccessException {
        assertEquals(userDAO.listUsers().size(), 0);
    }
    @Test
    void clearPositive() throws DataAccessException {
        userDAO.createUser(testUserData0);
        assertEquals(userDAO.listUsers().size(), 1);
        userDAO.clear();
        assertEquals(userDAO.listUsers().size(), 0);
    }

    @Test
    void createUserPositive() throws DataAccessException {
        userDAO.createUser(testUserDataArray.getFirst());
        ArrayList<UserData> userList = userDAO.listUsers();

        assertEquals(userList.get(0).username(), testUserDataArray.get(0).username());
        assertEquals(testUserDataArray.get(0).password(), userList.get(0).password());
        assertEquals(userList.get(0).email(), testUserDataArray.get(0).email());
    }

    @Test
    void getUserPositive() throws DataAccessException {
        userDAO.createUser(testUserData0);
        UserData user = userDAO.getUser(testUserData0.username());
        assertEquals(user.username(),testUserData0.username());
        assertEquals(user.password(),testUserData0.password());
        assertEquals(user.email(),testUserData0.email());
    }

    @Test
    void listUsersPositive() throws DataAccessException {
        userDAO.createUser(testUserData0);
        userDAO.createUser(testUserData1);
        userDAO.createUser(testUserData2);
        userDAO.createUser(testUserData3);
        ArrayList<UserData> userList = userDAO.listUsers();

        System.out.println(userList);

        for (int i = 0; i < userList.size(); i++) {
            assertEquals(userList.get(i).username(), testUserDataArray.get(i).username());
            assertEquals(testUserDataArray.get(i).password(), userList.get(i).password());
            assertEquals(userList.get(i).email(), testUserDataArray.get(i).email());
        }
    }
}