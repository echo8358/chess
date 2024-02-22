package dataAccess;

import model.UserData;

public interface UserDAO {

    void clear();

    void createUser(UserData user) throws AlreadyTakenException;

    UserData getUser(String username);
}
