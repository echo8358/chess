package service;

import dataAccess.*;
import model.AuthData;
import model.UserData;
import server.Login.LoginRequest;
import server.Login.LoginResponse;
import server.Logout.LogoutRequest;
import server.Logout.LogoutResponse;
import server.Register.RegisterRequest;
import server.Register.RegisterResponse;

import java.util.Objects;

public class UserService {
    static UserDAO userDAO = new MemoryUserDAO();
    static AuthDAO authDAO = new MemoryAuthDAO();

    public RegisterResponse register(RegisterRequest registerRequest) throws AlreadyTakenException {
        AuthData newAuthData;
        UserData user = new UserData(registerRequest.username(), registerRequest.password(), registerRequest.email());
        userDAO.createUser(user);
        newAuthData = authDAO.createAuth(user.username());
        return new RegisterResponse(newAuthData);
    }

    public LoginResponse login(LoginRequest loginRequest) throws UnauthorizedException{
        UserData reqUser = userDAO.getUser(loginRequest.username());
        if (reqUser == null || !Objects.equals(reqUser.password(), loginRequest.password())) {
            throw new UnauthorizedException("Error: unauthorized");
        }
        return new LoginResponse(authDAO.createAuth(loginRequest.username()));
    }
    public LogoutResponse logout(LogoutRequest logoutRequest) throws UnauthorizedException {
        if (authDAO.getAuthFromToken(logoutRequest.AuthToken()) == null) {
            throw new UnauthorizedException("Error: unauthorized");
        }
        authDAO.deleteAuth(logoutRequest.AuthToken());
        return new LogoutResponse();
    }
}
