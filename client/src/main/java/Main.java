import ServerFacade.ServerFacade;
import chess.*;
import model.AuthData;
import model.GameData;
import server.ListGame.ListGameResponse;
import ui.EscapeSequences;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;
import java.util.Scanner;

import static java.lang.Math.abs;
import static java.lang.System.exit;
import static ui.EscapeSequences.*;

public class Main {
    private static final ServerFacade serverFacade = new ServerFacade("http://localhost:3676");
    private static ArrayList<GameData> gamesList = new ArrayList<GameData>() {};
    public static void main(String[] args) {
        var piece = new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.PAWN);
        System.out.println("♕ 240 Chess Client: " + piece);
        while (true) {
            AuthData auth = loginMenu();
            mainMenu(auth);
        }
    }

    private static AuthData loginMenu() {
        boolean loggedIn = false;
        AuthData authData = null;
        while (!loggedIn) {
            String line = input("Would you like to (l)ogin, (r)egister, or (q)uit? Type (h) for help.");
            switch (line) {
                case "l" -> {
                    String username = input("Username: ");
                    String password = input("Password: ");
                    try {
                        authData = serverFacade.login(username, password);
                        loggedIn = true;
                    } catch (Exception e) {
                        System.out.println("Error: " + e.getMessage());
                    }
                }
                case "r" -> {
                    String username = input("Username: ");
                    String password = input("Password: ");
                    String email = input("Email: ");
                    try {
                        authData = serverFacade.register(username, password, email);
                        loggedIn = true;
                    } catch (Exception e) {
                        System.out.println("Error: " + e.getMessage());
                    }
                }
                case "h" -> {
                    System.out.println("(r)egister -> Prompts for a username, password, and email. Registers your account with the server.");
                    System.out.println("(l)ogin -> Prompts for a username and password. Logs you in to the server.");
                    System.out.println("(q)uit -> Quits the chess client.");
                    System.out.println("(h)elp -> Displays this help message.");
                }
                case "q" -> exit(0);
                case null, default -> System.out.println("Invalid option, please try again");
            }
        }
        return authData;
    }

    private static void mainMenu(AuthData auth) {
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
            /*
            for (GameData game: response.games()) {
                System.out.println(game);
            }
             */
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

    private static String input(String prompt) {
        Scanner scanner = new Scanner(System.in);
        System.out.println(prompt);
        return scanner.nextLine();
    }

    private static void displayGame(ChessGame game, String color) {

        ChessBoard board = game.getBoard();
        String squareColor = SET_BG_COLOR_WHITE;
        ChessPiece target = null;
        int yInt = 8;
        int yDir = -1;
        if (Objects.equals(color, "BLACK")) { yInt = 1; yDir = 1; }

        System.out.println(SET_TEXT_COLOR_BLACK+SET_BG_COLOR_LIGHT_GREY);
        if (color == "WHITE") System.out.println("  h  g  f  e  d  c  b  a  ");
        else System.out.println("  a  b  c  d  e  f  g  h  ");

        for (int y = yInt; abs(y-yInt) < 8; y+=yDir){
            System.out.print(SET_BG_COLOR_LIGHT_GREY + SET_TEXT_COLOR_BLACK + Integer.toString(9-y));
            for (int x = 1; x < 9; x++) {
                target = board.getPiece(new ChessPosition(y,x));

                if (target != null) {
                    System.out.print(squareColor+getTextColorFromPieceColor(target.getTeamColor()));
                    System.out.print(" "+getCharFromPieceType(target.getPieceType())+" ");
                }
                if (target == null) System.out.print(squareColor+"   ");

                if (squareColor.equals(SET_BG_COLOR_WHITE)) {
                    squareColor = SET_BG_COLOR_DARK_GREY;
                } else if (squareColor.equals(SET_BG_COLOR_DARK_GREY)) {
                    squareColor = SET_BG_COLOR_WHITE;
                }
            }
            System.out.println(SET_BG_COLOR_LIGHT_GREY + SET_TEXT_COLOR_BLACK + Integer.toString(9-y));
            if (squareColor.equals(SET_BG_COLOR_WHITE)) {
                squareColor = SET_BG_COLOR_DARK_GREY;
            } else if (squareColor.equals(SET_BG_COLOR_DARK_GREY)) {
                squareColor = SET_BG_COLOR_WHITE;
            }
        }
        if (color == "WHITE") System.out.println("  h  g  f  e  d  c  b  a  ");
        else System.out.println("  a  b  c  d  e  f  g  h  ");

    }

    private static String getCharFromPieceType(ChessPiece.PieceType type){
        HashMap<ChessPiece.PieceType, String> charMap = new HashMap<>();
        charMap.put(ChessPiece.PieceType.PAWN, "P");
        charMap.put(ChessPiece.PieceType.ROOK, "R");
        charMap.put(ChessPiece.PieceType.KNIGHT, "N");
        charMap.put(ChessPiece.PieceType.BISHOP, "B");
        charMap.put(ChessPiece.PieceType.KING, "K");
        charMap.put(ChessPiece.PieceType.QUEEN, "Q");
        return charMap.get(type);
    }

    private static String getTextColorFromPieceColor(ChessGame.TeamColor color) {
        if (color == ChessGame.TeamColor.BLACK) return SET_TEXT_COLOR_RED;
        if (color == ChessGame.TeamColor.WHITE) return SET_TEXT_COLOR_BLUE;
        else return SET_BG_COLOR_BLACK;
    }
}