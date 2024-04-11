import ServerFacade.HttpCommunicator;
import ServerFacade.ServerFacade;
import chess.*;
import model.AuthData;
import ui.LoginMenu;
import ui.MainMenu;

import java.util.Scanner;

import static java.lang.Math.abs;

public class Main {
    private static ServerFacade serverFacade;// = new ServerFacade("localhost:3676");
    public static void main(String[] args) {
        serverFacade = new ServerFacade(input("Type server address (ex. chess.echobase.org): "));
        var piece = new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.PAWN);
        System.out.println("♕ 240 Chess Client: " + piece);
        AuthData auth;
        MainMenu mainMenu = new MainMenu(serverFacade);
        LoginMenu loginMenu = new LoginMenu(serverFacade);
        while (true) {
            auth = loginMenu.display();
            mainMenu.display(auth);
        }
    }
    public static String input(String prompt) {
        Scanner scanner = new Scanner(System.in);
        System.out.println(prompt);
        return scanner.nextLine();
    }


}