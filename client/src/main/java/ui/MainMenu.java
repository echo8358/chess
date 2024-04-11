package ui;

import ServerFacade.HttpCommunicator;
import ServerFacade.ServerFacade;
import chess.ChessGame;
import chess.ChessMove;
import chess.ChessPiece;
import chess.ChessPosition;
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
        UIUtils.displayGame(game, boardColor, null);
        mostRecentGame = game;
    }
    public static void reloadGame() {
        if (mostRecentGame != null) {
            UIUtils.displayGame(mostRecentGame, boardColor, null);
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
                case "m" -> makeMove(gameID);
                case "r" -> resign(gameID);
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
    private static void printGameHelp() {
        //userInput = UIUtils.input("(h)elp, re(d)raw board, (l)eave, (m)ake move, (r)esign, or h(i)ghlight legal moves");
        System.out.println("(h)elp -> prints this dialog.");
        System.out.println("re(d)raw board -> redraws the current board.");
        System.out.println("(l)eave -> leaves the current game. Does not resign.");
        System.out.println("(m)ake move -> prompts for a move. If valid, make the move on the board.");
        System.out.println("(r)esign -> forfeits current game.");
        System.out.println("h(i)ghlight legal moves -> highlights legal moves. Say something about color here.");
    }
    private static void printObserveHelp() {
        System.out.println("(h)elp -> prints this dialog.");
        System.out.println("re(d)raw board -> redraws the current board.");
        System.out.println("(l)eave -> leaves the current game.");
    }
    private static void redrawBoard() {
        MainMenu.reloadGame();
    }
    private static void leave(int gameID) {
        serverFacade.leave(auth.authToken(), gameID);
    }
    private static void makeMove(int gameID) {
        String moveString;
        while (true) {
            moveString = input("Input move: ");
            if (validateMove(moveString)) {
                break;
            }
            System.out.println("Invalid move, please try again");
        }
        ChessMove move = parseMove(moveString);
        if (boardColor == ChessGame.TeamColor.WHITE && move.getEndPosition().getRow() == 8 || boardColor == ChessGame.TeamColor.BLACK && move.getEndPosition().getRow() == 1) {
            String promotionSelection;
            while (move.getPromotionPiece() == null) {
                promotionSelection = input("select your promotion piece: (r)ook, (k)night, (b)ishop, or (q)ueen");
                switch (promotionSelection) {
                    case "r" -> move = new ChessMove(move.getStartPosition(), move.getEndPosition(), ChessPiece.PieceType.ROOK);
                    case "k" -> move = new ChessMove(move.getStartPosition(), move.getEndPosition(), ChessPiece.PieceType.KNIGHT);
                    case "b" -> move = new ChessMove(move.getStartPosition(), move.getEndPosition(), ChessPiece.PieceType.BISHOP);
                    case "q" -> move = new ChessMove(move.getStartPosition(), move.getEndPosition(), ChessPiece.PieceType.QUEEN);
                }
            }
        }
        serverFacade.makeMove(auth.authToken(), gameID, move);
    }

    private static ChessMove parseMove(String moveString) {
        int startX = 8-(moveString.charAt(0)-'a');
        int startY = 8-(moveString.charAt(1)-'1');
        int endX = 8-(moveString.charAt(2)-'a');
        int endY = 8-(moveString.charAt(3)-'1');

        ChessMove move = new ChessMove(new ChessPosition(startY, startX), new ChessPosition(endY, endX), null);
        System.out.println(move);
        return move;
    }

    private static boolean validateMove(String moveString) {
        if (moveString.length() == 4) {
            if (moveString.charAt(0) >= 'a' && moveString.charAt(0) <= 'h') {
                if (moveString.charAt(1) >= '1' && moveString.charAt(1) <= '8') {
                    if (moveString.charAt(2) >= 'a' && moveString.charAt(2) <= 'h') {
                        if (moveString.charAt(3) >= '1' && moveString.charAt(3) <= '8') {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    private static void resign(int gameID) {
        serverFacade.resign(auth.authToken(), gameID);
    }
    private static void highlightLegalMoves() {
        String validMovePos;
        while (true) {
            validMovePos = input("square to display valid moves for: ");
            if (validMovePos.length() == 2) {
                if (validMovePos.charAt(0) >= 'a' && validMovePos.charAt(0) <= 'h') {
                    if (validMovePos.charAt(1) >= '1' && validMovePos.charAt(1) <= '8') {
                        break;
                    }
                }
            }
            System.out.println("Invalid position. Please try again");
        }
        int posX = 8-(validMovePos.charAt(0)-'a');
        int posY = 8-(validMovePos.charAt(1)-'1');

        if (mostRecentGame != null) {
            UIUtils.displayGame(mostRecentGame, boardColor, new ChessPosition(posY,posX));
        }
    }

}
