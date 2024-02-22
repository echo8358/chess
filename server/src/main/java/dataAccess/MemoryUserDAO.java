package dataAccess;

import model.UserData;

import java.util.ArrayList;
import java.util.Objects;

public class MemoryUserDAO implements UserDAO {

    ArrayList<UserData> userList;
    @Override
    public void clear() {
        userList.clear();
    }

    @Override
    public void createUser(String username, String password, String email) throws DataAccessException {
        if (getUser(username) != null) { throw new DataAccessException("User already exists."); }
        userList.add(new UserData(username, password, email));
    }

    @Override
    public UserData getUser(String username) {
        for (UserData user: userList) {
            if (Objects.equals(user.username(), username)) return user;
        }
        return null;
    }
}
