package server.http.Login;

import model.UserData;

public record LoginRequest(String username, String password) {}
