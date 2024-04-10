package server.http.Register;

import model.AuthData;
import model.UserData;

public record RegisterResponse(AuthData auth) {
}
