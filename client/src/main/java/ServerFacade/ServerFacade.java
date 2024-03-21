package ServerFacade;

import com.google.gson.Gson;
import model.AuthData;
import model.GameData;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;

public class ServerFacade {
    static String serverUrl;
    static HttpURLConnection connection;
    public ServerFacade(String remote){
        serverUrl = remote;
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


    public ArrayList<GameData> listGames(String authToken) {
        return null;
    }

    public void joinGame(String playerColor, int gameID, String auth) {
    }

    public int createGame(String gameName, String authToken) {
        return 0;
    }

    public void clearDatabase() throws ResponseException {
        this.makeRequest("DELETE", "/db", null, null, null);
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
