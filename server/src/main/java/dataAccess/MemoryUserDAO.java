package dataAccess;

import dataAccess.Exceptions.AlreadyTakenException;
import model.UserData;

import java.util.ArrayList;
import java.util.Objects;

public class MemoryUserDAO implements UserDAO {

    static ArrayList<UserData> userList = new ArrayList<UserData>();
    @Override
    public void clear() {
        userList.clear();
    }

    @Override
    public void createUser(UserData user) throws AlreadyTakenException {
        if (getUser(user.username()) != null) throw new AlreadyTakenException("Error: already taken");
        userList.add(user);
    }

    @Override
    public UserData getUser(String username) {
        for (UserData user: listUsers()) {
            if (Objects.equals(user.username(), username)) return user;
        }
        return null;
    }
    public ArrayList<UserData> listUsers() {
        return new ArrayList<UserData>(userList);
    }
}
