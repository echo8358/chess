package server.Login;

import dataAccess.AuthDAO;
import model.AuthData;

public record LoginResponse(AuthData auth) { }