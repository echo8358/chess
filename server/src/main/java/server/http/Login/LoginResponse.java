package server.http.Login;

import dataAccess.AuthDAO;
import model.AuthData;

public record LoginResponse(AuthData auth) { }