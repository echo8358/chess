package service;

import dataAccess.*;
import model.AuthData;
import model.UserData;
import server.Login.LoginRequest;
import server.Login.LoginResponse;
import server.Register.RegisterRequest;
import server.Register.RegisterResponse;

import java.util.Objects;

public class UserService {
    static UserDAO userDAO = new MemoryUserDAO();
    static AuthDAO authDAO = new MemoryAuthDAO();

    public RegisterResponse register(RegisterRequest registerRequest) {
        AuthData newAuthData;
        UserData user = new UserData(registerRequest.username(), registerRequest.password(), registerRequest.email());
        try {
            userDAO.createUser(user);
            newAuthData = authDAO.createAuth(user.username());
        } catch (AlreadyTakenException e) {
            return new RegisterResponse(null, 403, e.getMessage());
        }
        return new RegisterResponse(newAuthData, 200, null);
    }

    public LoginResponse login(LoginRequest loginRequest) {
        AuthData newAuthData;
        UserData reqUser = userDAO.getUser(loginRequest.username());
        if (reqUser == null || !Objects.equals(reqUser.password(), loginRequest.password())) {
           return new LoginResponse(null, 401, "Error: unauthorized");
        }
        return new LoginResponse(authDAO.createAuth(loginRequest.username()), 200, null);
    }
}
