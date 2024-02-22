package dataAccess;

import model.UserData;

public interface UserDAO {

    void clear();

    void createUser(String username, String password, String email) throws DataAccessException;

    UserData getUser(String username);
}
