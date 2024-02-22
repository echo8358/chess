package server;

import model.AuthData;
import model.UserData;

public record RegisterResponse(AuthData auth, int statusCode, String message) {
}
