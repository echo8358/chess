package ui;

import ServerFacade.ServerFacade;
import chess.ChessGame;
import model.AuthData;
import model.GameData;
import server.http.ListGame.ListGameResponse;

import java.util.ArrayList;
import java.util.Objects;

import static java.lang.Math.abs;
import static ui.UIUtils.displayGame;
import static ui.UIUtils.input;

public class MainMenu {
    private static ServerFacade serverFacade;
    private static ArrayList<GameData> gamesList = new ArrayList<GameData>() {};
    public MainMenu(ServerFacade sF) {
        serverFacade = sF;
    }
    public void display(AuthData auth) {
        listGames(auth);
        while (true) {
            String line = input("(c)reate game, (l)ist games, (j)oin game, join (o)bserver, (lo)gout or (h)elp:");
            switch (line) {
                case "c" -> {
                    createGame(auth);
                }
                case "l" -> {
                    listGames(auth);
                }
                case "j" -> {
                    joinGame(auth);
                    ChessGame game = new ChessGame();
                    displayGame(game, "WHITE");
                    displayGame(game, "BLACK");
                }
                case "o" -> {
                    joinObserver(auth);
                    ChessGame game = new ChessGame();
                    displayGame(game, "WHITE");
                    displayGame(game, "BLACK");
                }
                case "lo" -> {
                    logout(auth);
                    return;
                }
                case "h" -> {
                    System.out.println("(c)reate game -> Prompts for game name. Creates a game.");
                    System.out.println("(l)ist games -> Lists all games.");
                    System.out.println("(j)oin game -> Prompts for game id and color. Joins specified game as that color");
                    System.out.println("join (o)bserver -> Prompts for game id. Joins specified game as an observer");
                    System.out.println("(lo)gout -> Logs out your user. Returns to pre-login menu.");
                    System.out.println("(h)elp -> Displays this help message.");
                }
                case null, default -> System.out.println("Invalid option, please try again");
            }
        }
    }

    private static void createGame(AuthData auth) {
        String gameName = input("Game Name: ");
        try {
            serverFacade.createGame(gameName, auth.authToken());
        } catch (Exception e) {
            System.out.println("Error: "+e.getMessage());
        }
    }

    private static void listGames(AuthData auth) {
        try {
            System.out.println("Available games: ");
            ListGameResponse response = serverFacade.listGames(auth.authToken());
            gamesList = response.games();
            GameData game = null;
            for (int i = 0; i < gamesList.size(); i++) {
                game = gamesList.get(i);
                System.out.println("("+Integer.toString(i)+") Name: "+game.gameName()+" White: "+ game.whiteUsername()+" Black: "+ game.blackUsername());
            }
        } catch (Exception e) {
            System.out.println("Error: "+e.getMessage());
        }
    }

    private static void joinGame(AuthData auth) {
        String color = null;
        int gameID = Integer.parseInt(input("Game ID: "));
        while (!Objects.equals(color, "WHITE") && !Objects.equals(color, "BLACK")) {
            color = input("(WHITE) or (BLACK)?");
        }
        try {
            serverFacade.joinGame(color, gamesList.get(gameID).gameID(), auth.authToken());
        } catch (Exception e) {
            System.out.println("Error: "+e.getMessage());
        }
    }

    private static void joinObserver(AuthData auth) {
        String color = null;
        int gameID = Integer.parseInt(input("Game ID: "));
        try {
            serverFacade.joinGame(null, gamesList.get(gameID).gameID(), auth.authToken());
        } catch (Exception e) {
            System.out.println("Error: "+e.getMessage());
        }
    }

    private static void logout(AuthData auth) {
        try {
            serverFacade.logout(auth.authToken());
        } catch (Exception e) {
            System.out.println("Error: "+e.getMessage());
        }
    }

}
