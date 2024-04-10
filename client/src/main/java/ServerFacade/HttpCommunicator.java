package ServerFacade;

import com.google.gson.Gson;
import model.AuthData;
import server.http.JoinGame.JoinGameResponse;
import server.http.ListGame.ListGameResponse;
import webSocketMessages.userCommands.JoinObserver;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;

public class HttpCommunicator {
    static String serverUrl;
    static HttpURLConnection connection;
    static WebSocketCommunicator webSocketCommunicator;
    static Gson gson = new Gson();
    public HttpCommunicator(String remote) {
        serverUrl = "http://"+remote;
    }

    public AuthData register(String username, String password, String email) throws ResponseException {
        String json = "{ \"username\": \""+username+"\", \"password\": \""+password+"\", \"email\": \""+email+"\" }";
        return this.makeRequest("POST", "/user", json, AuthData.class, null);
    }
    public AuthData login(String username, String password) throws ResponseException {
        String json = "{ \"username\":\""+username+"\", \"password\":\""+password+"\" }";
        return this.makeRequest("POST", "/session", json, AuthData.class, null);
    }
    public void logout(String authToken) throws ResponseException {
        this.makeRequest("DELETE", "/session", null, null, authToken);
    }


    public ListGameResponse listGames(String authToken) throws ResponseException {
        return this.makeRequest("GET", "/game", null, ListGameResponse.class, authToken);
    }

    public void joinGame(String playerColor, int gameID, String authToken) throws ResponseException {
        String json = "{ \"playerColor\":\""+playerColor+"\", \"gameID\": "+String.valueOf(gameID)+" }";
        this.makeRequest("PUT", "/game", json, JoinGameResponse.class, authToken);
    }

    public int createGame(String gameName, String authToken) throws ResponseException {
        String json = "{ \"gameName\":\""+gameName+"\" }";
        return this.makeRequest("POST", "/game", json, GameID.class, authToken).gameID();
    }

    public void clearDatabase() throws ResponseException {
        this.makeRequest("DELETE", "/db", null, null, null);
    }

    public void testWebSocket(String authToken) {
        try {
            webSocketCommunicator.send(gson.toJson((new JoinObserver(authToken,1))));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    /*
    public Pet addPet(Pet pet) throws ResponseException {
        var path = "/pet";
        return this.makeRequest("POST", path, pet, Pet.class);
    }

    public void deletePet(int id) throws ResponseException {
        var path = String.format("/pet/%s", id);
        this.makeRequest("DELETE", path, null, null);
    }

    public void deleteAllPets() throws ResponseException {
        var path = "/pet";
        this.makeRequest("DELETE", path, null, null);
    }

    public Pet[] listPets() throws ResponseException {
        var path = "/pet";
        record listPetResponse(Pet[] pet) {
        }
        var response = this.makeRequest("GET", path, null, listPetResponse.class);
        return response.pet();
    }
    */

    private <T> T makeRequest(String method, String path, String json, Class<T> responseClass, String authToken) throws ResponseException {
        try {
            URL url = (new URI(serverUrl + path)).toURL();
            HttpURLConnection http = (HttpURLConnection) url.openConnection();
            http.setRequestMethod(method);
            if (authToken != null) {
                http.addRequestProperty("Authorization",authToken);
            }
            http.setDoOutput(true);

            writeBody(json, http);
            http.connect();
            throwIfNotSuccessful(http);
            return readBody(http, responseClass);
        } catch (Exception ex) {
            throw new ResponseException(500, ex.getMessage());
        }
    }


    private static void writeBody(String reqData, HttpURLConnection http) throws IOException {
        if (reqData != null) {
            http.addRequestProperty("Content-Type", "application/json");
            //String reqData = new Gson().toJson(request);
            try (OutputStream reqBody = http.getOutputStream()) {
                reqBody.write(reqData.getBytes());
            }
        }
    }

    private void throwIfNotSuccessful(HttpURLConnection http) throws IOException, ResponseException {
        var status = http.getResponseCode();
        if (!isSuccessful(status)) {
            throw new ResponseException(status, "failure: " + status);
        }
    }

    private static <T> T readBody(HttpURLConnection http, Class<T> responseClass) throws IOException {
        T response = null;
        if (http.getContentLength() < 0) {
            try (InputStream respBody = http.getInputStream()) {
                InputStreamReader reader = new InputStreamReader(respBody);
                if (responseClass != null) {
                    response = new Gson().fromJson(reader, responseClass);
                }
            }
        }
        return response;
    }


    private boolean isSuccessful(int status) {
        return status / 100 == 2;
    }
}
