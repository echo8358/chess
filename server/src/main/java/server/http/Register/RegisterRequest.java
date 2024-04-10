package server.http.Register;

import model.AuthData;
import model.UserData;

public record RegisterRequest(String username, String password, String email) { }
