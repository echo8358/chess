package dataAccess;

import dataAccess.Exceptions.AlreadyTakenException;
import dataAccess.Exceptions.DataAccessException;
import model.UserData;

import java.util.ArrayList;

public interface UserDAO {

    void clear() throws DataAccessException;

    void createUser(UserData user) throws DataAccessException;

    UserData getUser(String username) throws DataAccessException;

    ArrayList<UserData> listUsers() throws DataAccessException;
}
