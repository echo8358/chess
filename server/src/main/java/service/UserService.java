package service;

import dataAccess.*;
import dataAccess.Exceptions.AlreadyTakenException;
import dataAccess.Exceptions.DataAccessException;
import dataAccess.Exceptions.UnauthorizedException;
import model.AuthData;
import model.UserData;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import server.http.Login.LoginRequest;
import server.http.Login.LoginResponse;
import server.http.Logout.LogoutRequest;
import server.http.Logout.LogoutResponse;
import server.http.Register.RegisterRequest;
import server.http.Register.RegisterResponse;

public class UserService {
    static UserDAO userDAO = new SQLUserDAO();
    static AuthDAO authDAO = new SQLAuthDAO();
    BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    public RegisterResponse register(RegisterRequest registerRequest) throws AlreadyTakenException, DataAccessException {
        AuthData newAuthData;
        String hashedPassword = encoder.encode(registerRequest.password());
        UserData user = new UserData(registerRequest.username(), hashedPassword, registerRequest.email());
        userDAO.createUser(user);
        newAuthData = authDAO.createAuth(user.username());
        return new RegisterResponse(newAuthData);
    }

    public LoginResponse login(LoginRequest loginRequest) throws UnauthorizedException, DataAccessException {
        UserData reqUser = userDAO.getUser(loginRequest.username());
        if (reqUser == null || !encoder.matches(loginRequest.password(),reqUser.password())) {
            throw new UnauthorizedException("Error: unauthorized");
        }
        return new LoginResponse(authDAO.createAuth(loginRequest.username()));
    }
    public LogoutResponse logout(LogoutRequest logoutRequest) throws UnauthorizedException, DataAccessException {
        if (authDAO.getAuthFromToken(logoutRequest.authToken()) == null) {
            throw new UnauthorizedException("Error: unauthorized");
        }
        authDAO.deleteAuth(logoutRequest.authToken());
        return new LogoutResponse();
    }
}
