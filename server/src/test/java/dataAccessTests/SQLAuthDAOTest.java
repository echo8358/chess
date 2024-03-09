package dataAccessTests;

import dataAccess.AuthDAO;
import dataAccess.Exceptions.DataAccessException;
import dataAccess.SQLAuthDAO;
import dataAccess.SQLUserDAO;
import dataAccess.UserDAO;
import model.AuthData;
import model.UserData;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import passoffTests.testClasses.TestException;
import service.DBService;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

class SQLAuthDAOTest {

    private static final AuthDAO authDAO = new SQLAuthDAO();
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
    void createAuthNegative() {
        //255 character limit
        assertThrows(DataAccessException.class, () -> {
            authDAO.createAuth("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA");
        });

    }

    @Test
    void getAuthFromTokenNegative() throws DataAccessException {
        assertNull(authDAO.getAuthFromToken(""));
    }

    @Test
    void deleteAuthNegative() throws DataAccessException {
        authDAO.createAuth(testUserData0.username());
        ArrayList<AuthData> authList = authDAO.listAuth();
        authDAO.deleteAuth("");
        assertEquals(authList, authDAO.listAuth());
    }

    @Test
    void listAuthNegative() throws DataAccessException {
        assertEquals(authDAO.listAuth().size(), 0);
    }
    @Test
    void clearPositive() throws DataAccessException {
        authDAO.createAuth(testUserData0.username());
        assertEquals(authDAO.listAuth().size(), 1);
        authDAO.clear();
        assertEquals(authDAO.listAuth().size(), 0);
    }

    @Test
    void createAuthPositive() throws DataAccessException {
        AuthData authData = authDAO.createAuth(testUserData0.username());
        assertNotNull(authData);
        assertEquals(testUserData0.username(), authData.username());
        assertEquals(36, authData.authToken().length());
    }

    @Test
    void getAuthFromTokenPositive() throws DataAccessException {
        String authToken = authDAO.createAuth(testUserData0.username()).authToken();

        AuthData authData = authDAO.getAuthFromToken(authToken);

        assertNotNull(authData);
        assertEquals(testUserData0.username(), authData.username());
        assertEquals(36, authData.authToken().length());
    }

    @Test
    void deleteAuthPositive() throws DataAccessException {
        String authToken = authDAO.createAuth(testUserData0.username()).authToken();
        authDAO.deleteAuth(authToken);

        assertNull(authDAO.getAuthFromToken(authToken));
    }

    @Test
    void listAuthPositive() throws DataAccessException {
        authDAO.createAuth(testUserData0.username());
        authDAO.createAuth(testUserData1.username());
        authDAO.createAuth(testUserData2.username());
        authDAO.createAuth(testUserData3.username());

        ArrayList<AuthData> authList = authDAO.listAuth();

        for (int i = 0; i < authList.size(); i++) {
            assertEquals(authList.get(i).username(),testUserDataArray.get(i).username());
            assertEquals(authDAO.getAuthFromToken(authList.get(i).authToken()).username(),testUserDataArray.get(i).username());
        }
    }
}