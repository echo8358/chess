package dataAccess;

import model.UserData;

import java.lang.reflect.Array;
import java.util.ArrayList;

public interface UserDAO {

    void clear();

    void createUser(UserData user) throws AlreadyTakenException;

    UserData getUser(String username);

    ArrayList<UserData> listUsers();
}
