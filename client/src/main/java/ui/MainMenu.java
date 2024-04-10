package ui;

import ServerFacade.HttpCommunicator;
import ServerFacade.ServerFacade;
import chess.ChessGame;
import model.AuthData;
import model.GameData;
import server.http.ListGame.ListGameResponse;

import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import static java.lang.Math.abs;
import static ui.UIUtils.displayGame;
import static ui.UIUtils.input;

public class MainMenu {
    private static ServerFacade serverFacade;
    private static ArrayList<GameData> gamesList = new ArrayList<GameData>() {};
    public MainMenu(ServerFacade sF) {
        serverFacade = sF;
    }
    private static ChessGame.TeamColor boardColor;
    private static ChessGame mostRecentGame;
    private static AuthData auth;
    public void display(AuthData a) {
        auth = a;
        listGames();
        while (true) {
            String line = input("(c)reate game, (l)ist games, (j)oin game, join (o)bserver, (lo)gout or (h)elp:");
            switch (line) {
                case "c" -> {
                    createGame();
                }
                case "l" -> {
                    listGames();
                }
                case "j" -> {
                    joinGame();
                }
                case "o" -> {
                    joinObserver();
                }
                case "lo" -> {
                    logout();
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

    public static void loadGame(ChessGame game) {
        UIUtils.displayGame(game, boardColor);
        mostRecentGame = game;
    }
    public static void reloadGame() {
        if (mostRecentGame != null) {
            UIUtils.displayGame(mostRecentGame, boardColor);
        }
    }

    private static void createGame() {
        String gameName = input("Game Name: ");
        try {
            serverFacade.createGame(gameName, auth.authToken());
        } catch (Exception e) {
            System.out.println("Error: "+e.getMessage());
        }
    }

    private static void listGames() {
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

    private static void joinGame() {
        String color = null;
        int gameID = Integer.parseInt(input("Game ID: "));
        while (!Objects.equals(color, "WHITE") && !Objects.equals(color, "BLACK")) {
            color = input("(WHITE) or (BLACK)?");
        }
        ChessGame.TeamColor trueColor = ChessGame.TeamColor.valueOf(color);
        int trueGameID = gamesList.get(gameID).gameID();
        try {
            serverFacade.joinGame(color, trueGameID, auth.authToken());
            serverFacade.joinPlayer(auth.authToken(), trueGameID, trueColor);
            boardColor = trueColor;
        } catch (Exception e) {
            System.out.println("Error: "+e.getMessage());
        }
        gameLoop(trueGameID);
    }

    private static void joinObserver() {
        String color = null;
        int gameID = Integer.parseInt(input("Game ID: "));
        int trueGameID = gamesList.get(gameID).gameID();
        try {
            serverFacade.joinGame(null, trueGameID, auth.authToken());
            serverFacade.joinObserver(auth.authToken(), trueGameID);
        } catch (Exception e) {
            System.out.println("Error: "+e.getMessage());
        }
        observeLoop(trueGameID);
    }

    private static void logout() {
        try {
            serverFacade.logout(auth.authToken());
        } catch (Exception e) {
            System.out.println("Error: "+e.getMessage());
        }
    }

    private static void gameLoop(int gameID) {
        String userInput;
        boolean inGame = true;
        while (inGame) {
            userInput = UIUtils.input("(h)elp, re(d)raw board, (l)eave, (m)ake move, (r)esign, or h(i)ghlight legal moves");
            switch (userInput) {
                case "h" -> printGameHelp();
                case "d" -> redrawBoard();
                case "l" -> { leave(gameID); inGame = false; }
                case "m" -> makeMove();
                case "r" -> resign();
                case "i" -> highlightLegalMoves();
            }
        }
    }
    private static void observeLoop(int gameID) {
        String userInput;
        boolean inGame = true;
        while (inGame) {
            userInput = UIUtils.input("(h)elp, re(d)raw board, (l)eave");
            switch (userInput) {
                case "h" -> printObserveHelp();
                case "d" -> redrawBoard();
                case "l" -> { leave(gameID); inGame = false; }
            }
        }
    }
    private static void printGameHelp() {}
    private static void printObserveHelp() {}
    private static void redrawBoard() {
        MainMenu.reloadGame();
    }
    private static void leave(int gameID) {
        serverFacade.leave(auth.authToken(), gameID);
    }
    private static void makeMove() {}
    private static void resign() {}
    private static void highlightLegalMoves() {}

}
