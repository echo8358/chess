package server.websocket;

import chess.ChessGame;
import chess.ChessMove;
import chess.InvalidMoveException;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import dataAccess.AuthDAO;
import dataAccess.Exceptions.DataAccessException;
import dataAccess.GameDAO;
import dataAccess.SQLAuthDAO;
import dataAccess.SQLGameDAO;
import model.AuthData;
import model.GameData;
import org.eclipse.jetty.server.Authentication;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import webSocketMessages.serverMessages.Error;
import webSocketMessages.serverMessages.LoadGame;
import webSocketMessages.serverMessages.Notification;
import webSocketMessages.serverMessages.ServerMessage;
import webSocketMessages.userCommands.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;
import java.util.Timer;


@WebSocket
public class WebSocketHandler {

    private final ConnectionManager connections = new ConnectionManager();
    private final Gson gson;
    private final AuthDAO authDAO = new SQLAuthDAO();
    private final GameDAO gameDAO = new SQLGameDAO();

    public WebSocketHandler() {
        gson = createSerializer();
    }

    @OnWebSocketMessage
    public void onMessage(Session session, String message) throws IOException {
        //do type helper stuff
        System.out.println(message);
        UserGameCommand command = gson.fromJson(message, UserGameCommand.class);
        System.out.println(command.getCommandType());
        switch (command.getCommandType()) {
            case JOIN_OBSERVER -> joinObserver((JoinObserver) command, session);
            case JOIN_PLAYER -> joinPlayer((JoinPlayer) command, session);
            case LEAVE -> leave((Leave) command, session);
            case MAKE_MOVE -> makeMove((MakeMove) command, session);
            case RESIGN -> resign((Resign) command, session);
            //case RESIGN -> resign(action.visitorName(), session);
        }

    }

    private void joinObserver(JoinObserver command, Session session) throws IOException {
        AuthData auth = checkAuth(command, session);
        if(auth == null) return;

        GameData game = checkGame(command.getGameID(), session);
        if(game == null) return;

        connections.add(auth.username(),session, command.getGameID());
        session.getRemote().sendString(gson.toJson(new LoadGame(new ChessGame())));
        connections.broadcast(command.getGameID(), auth.username(), new Notification(auth.username()+" has joined as an observer"));
    }
    private void joinPlayer(JoinPlayer command, Session session) throws IOException {
        AuthData auth = checkAuth(command, session);
        if (auth == null) return;

        GameData game = checkGame(command.getGameID(), session);
        if (game == null) return;

        if(command.getPlayerColor() == ChessGame.TeamColor.BLACK && !Objects.equals(game.blackUsername(), auth.username())) {
            session.getRemote().sendString(gson.toJson(new Error("Spot taken or wrong team error")));
            return;
        }
        if(command.getPlayerColor() == ChessGame.TeamColor.WHITE && !Objects.equals(game.whiteUsername(), auth.username())) {
            session.getRemote().sendString(gson.toJson(new Error("Spot taken or wrong team error")));
            return;
        }

        Connection rootConnection = connections.add(auth.username(), session, command.getGameID());
        connections.broadcast(command.getGameID(), auth.username(), (new Notification("Player "+auth.username()+" joined as "+command.getPlayerColor())));
        rootConnection.send(gson.toJson(new LoadGame(game.game())));
    }
    private void leave(Leave command, Session session) throws IOException {
        AuthData auth = checkAuth(command, session);
        if (auth == null) return;

        connections.remove(auth.username());
        connections.broadcast(command.getGameID(), auth.username(), (new Notification("Player "+auth.username()+" has left ")));
    }
    private void makeMove(MakeMove command, Session session) throws IOException {
        AuthData auth = checkAuth(command, session);
        if (auth == null) return;

        GameData game = checkGame(command.getGameID(), session);

        ChessGame.TeamColor teamColor;

        if (game != null) {
            if (Objects.equals(auth.username(), game.blackUsername())) teamColor = ChessGame.TeamColor.BLACK;
            else if (Objects.equals(auth.username(), game.whiteUsername())) teamColor = ChessGame.TeamColor.WHITE;
            else {
                session.getRemote().sendString(gson.toJson(new Error("Invalid move error, please try again")));
                return;
            }

            if (game.game().getBoard().getPiece(command.getMove().getStartPosition()).getTeamColor() != teamColor) {
                session.getRemote().sendString(gson.toJson(new Error("Invalid move error, please try again")));
                return;
            }

            Collection<ChessMove> validMoves = game.game().validMoves(command.getMove().getStartPosition());
            try {
                for (ChessMove move : validMoves) {
                    if (move.equals(command.getMove())) {
                        System.out.println(move.toString());
                        game.game().makeMove(command.getMove());

                        //send new boards
                        connections.broadcast(command.getGameID(), null, (new LoadGame(game.game())));
                        connections.broadcast(command.getGameID(), auth.username(), (new Notification("The move ___ was made")));
                        return;
                    }
                }
            } catch (InvalidMoveException e) { System.out.println(e.getMessage());}
        }
        //notify error
        session.getRemote().sendString(gson.toJson(new Error("Invalid move error, please try again")));

    }
    private void resign(Resign command, Session session) {}
    private AuthData checkAuth(UserGameCommand command, Session session) throws IOException {
        AuthData auth;
        try {
            auth = authDAO.getAuthFromToken(command.getAuthString());
            if(auth == null) {
                session.getRemote().sendString(gson.toJson(new Error("Invalid auth token error")));
                return null;
            }
        } catch (DataAccessException e) {
            session.getRemote().sendString(gson.toJson(new Error("Internal server error")));
            return null;
        }
        return auth;
    }
    private GameData checkGame(int gameID, Session session) throws IOException {
        GameData game;
        try {
            game = gameDAO.getGame(gameID);
            if (game == null) {
                session.getRemote().sendString(gson.toJson(new Error("Invalid game id error")));
                return null;
            }
        } catch (DataAccessException e) {
            session.getRemote().sendString(gson.toJson(new Error("Internal server error")));
            return null;
        }
        return game;

    }
    /*
    private void enter(String visitorName, Session session) throws IOException {
        connections.add(visitorName, session);
        var message = String.format("%s is in the shop", visitorName);
        var notification = new Notification(Notification.Type.ARRIVAL, message);
        connections.broadcast(visitorName, notification);
    }

    private void exit(String visitorName) throws IOException {
        connections.remove(visitorName);
        var message = String.format("%s left the shop", visitorName);
        var notification = new Notification(Notification.Type.DEPARTURE, message);
        connections.broadcast(visitorName, notification);
    }

    public void makeNoise(String petName, String sound) throws ResponseException {
        try {
            var message = String.format("%s says %s", petName, sound);
            var notification = new Notification(Notification.Type.NOISE, message);
            connections.broadcast("", notification);
        } catch (Exception ex) {
            throw new ResponseException(500, ex.getMessage());
        }
    }

     */
    public static Gson createSerializer() {
        GsonBuilder gsonBuilder = new GsonBuilder();

        gsonBuilder.registerTypeAdapter(UserGameCommand.class,
                (JsonDeserializer<UserGameCommand>) (el, type, ctx) -> {
                    UserGameCommand command = null;
                    if (el.isJsonObject()) {
                        String commandType = el.getAsJsonObject().get("commandType").getAsString();
                        switch (UserGameCommand.CommandType.valueOf(commandType)) {
                            case JOIN_OBSERVER -> command = ctx.deserialize(el, JoinObserver.class);
                            case JOIN_PLAYER -> command = ctx.deserialize(el, JoinPlayer.class);
                            case LEAVE -> command = ctx.deserialize(el, Leave.class);
                            case MAKE_MOVE -> command = ctx.deserialize(el, MakeMove.class);
                            case RESIGN -> command = ctx.deserialize(el, Resign.class);
                        }
                    }
                    return command;
                });

        return gsonBuilder.create();
    }
}