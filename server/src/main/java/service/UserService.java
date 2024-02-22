package service;

import dataAccess.*;
import model.AuthData;
import model.UserData;
import server.RegisterResponse;

public class UserService {
    static UserDAO userDAO = new MemoryUserDAO();
    static AuthDAO authDAO = new MemoryAuthDAO();

    public RegisterResponse register(UserData user) {
        AuthData newAuthData;
        try {
            userDAO.createUser(user);
            newAuthData = authDAO.createAuth(user.username());
        } catch (AlreadyTakenException e) {
            return new RegisterResponse(null, 403, e.getMessage());
        }
        return new RegisterResponse(newAuthData, 200, null);
    }
}
